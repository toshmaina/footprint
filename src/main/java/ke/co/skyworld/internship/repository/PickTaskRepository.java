package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;


public class PickTaskRepository {

    /**
     * Locks both the order_line and the LPN for the duration of the check,
     * to prevent two concurrent task-creation calls from both grabbing the
     * same LPN, or both trying to task the same order line twice.
     * Enforces the "one task = one whole LPN, quantity must match exactly"
     * simplification documented at the top of this feature.
     */
    public long createTask(long pickWaveId, long orderLineId, long licensePlateId, String assignedTo)
            throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long orderLineProductId;
                int reservedQuantity;
                long reservationId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT ol.product_id, ol.order_line_status, sr.stock_reservation_id, sr.stock_reservation_quantity
                        FROM order_lines ol
                        JOIN stock_reservations sr ON sr.order_line_id = ol.order_line_id AND sr.stock_reservation_status = 'active'
                        WHERE ol.order_line_id = ?
                        FOR UPDATE OF ol
                        """)) {
                    ps.setLong(1, orderLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new InvalidStateException(
                                    "Order line not found, or has no active reservation: " + orderLineId);
                        }
                        if (!"reserved".equals(rs.getString("order_line_status"))) {
                            throw new InvalidStateException("Order line is not in 'reserved' status");
                        }
                        orderLineProductId = rs.getLong("product_id");
                        reservedQuantity = rs.getInt("stock_reservation_quantity");
                        reservationId = rs.getLong("stock_reservation_id");
                    }
                }

                long lpnProductId;
                long storageLocationId;
                int lpnQuantity;
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT product_id, license_plate_status, current_storage_location_id, license_plate_quantity
                        FROM license_plates
                        WHERE license_plate_id = ?
                        FOR UPDATE
                        """)) {
                    ps.setLong(1, licensePlateId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new InvalidStateException("License plate not found: " + licensePlateId);
                        }
                        if (!"stored".equals(rs.getString("license_plate_status"))) {
                            throw new InvalidStateException("License plate is not in 'stored' status");
                        }
                        lpnProductId = rs.getLong("product_id");
                        long locId = rs.getLong("current_storage_location_id");
                        if (rs.wasNull()) {
                            throw new InvalidStateException("License plate has no storage location on record");
                        }
                        storageLocationId = locId;
                        lpnQuantity = rs.getInt("license_plate_quantity");
                    }
                }

                if (lpnProductId != orderLineProductId) {
                    throw new InvalidStateException("License plate's product does not match the order line's product");
                }
                if (lpnQuantity != reservedQuantity) {
                    throw new InvalidStateException(
                            "License plate quantity (" + lpnQuantity + ") must exactly match the reserved quantity ("
                                    + reservedQuantity + ") - partial-LPN picking isn't supported in this version");
                }

                // Guard against the same LPN being double-tasked
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT 1 FROM pick_tasks WHERE license_plate_id = ? AND pick_task_status != 'cancelled'")) {
                    ps.setLong(1, licensePlateId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new InvalidStateException("This license plate already has an active pick task");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO pick_wave_assignments (pick_wave_id, order_line_id, stock_reservation_id)
                        VALUES (?, ?, ?)
                        """)) {
                    ps.setLong(1, pickWaveId);
                    ps.setLong(2, orderLineId);
                    ps.setLong(3, reservationId);
                    ps.executeUpdate();
                }

                long taskId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO pick_tasks
                            (pick_wave_id, storage_location_id, product_id, pick_task_quantity_requested,
                             pick_task_assigned_to, pick_task_status, license_plate_id, order_line_id)
                        VALUES (?, ?, ?, ?, ?, ?::pick_task_status, ?, ?)
                        RETURNING pick_task_id
                        """)) {
                    ps.setLong(1, pickWaveId);
                    ps.setLong(2, storageLocationId);
                    ps.setLong(3, orderLineProductId);
                    ps.setInt(4, reservedQuantity);
                    if (assignedTo != null && !assignedTo.isBlank()) {
                        ps.setString(5, assignedTo);
                        ps.setString(6, "assigned");
                    } else {
                        ps.setNull(5, Types.VARCHAR);
                        ps.setString(6, "pending");
                    }
                    ps.setLong(7, licensePlateId);
                    ps.setLong(8, orderLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        taskId = rs.getLong("pick_task_id");
                    }
                }

                conn.commit();
                return taskId;
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * THE concurrency-critical confirm - same double-guard pattern as
     * ConfirmPutawayHandler (row lock + UNIQUE(pick_task_id) backstop).
     * Branches on full vs. short pick: a short pick reduces the order
     * line's allocation to what was actually picked and creates a
     * backorder for the shortfall (source_type='short_pick'), rather than
     * pretending the full reserved quantity shipped.
     */
    public void confirmTask(long pickTaskId, int quantityPicked, String confirmedBy) throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                String status;
                long licensePlateId, orderLineId, productId, warehouseId, storageLocationId, reservationId;
                int requested;
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT pt.pick_task_status, pt.license_plate_id, pt.order_line_id, pt.product_id,
                               pt.storage_location_id, pt.pick_task_quantity_requested, lp.warehouse_id,
                               sr.stock_reservation_id
                        FROM pick_tasks pt
                        JOIN license_plates lp ON lp.license_plate_id = pt.license_plate_id
                        JOIN stock_reservations sr ON sr.order_line_id = pt.order_line_id AND sr.stock_reservation_status = 'active'
                        WHERE pt.pick_task_id = ?
                        FOR UPDATE OF pt
                        """)) {
                    ps.setLong(1, pickTaskId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new InvalidStateException("Pick task not found, or its reservation is no longer active: " + pickTaskId);
                        }
                        status = rs.getString("pick_task_status");
                        licensePlateId = rs.getLong("license_plate_id");
                        orderLineId = rs.getLong("order_line_id");
                        productId = rs.getLong("product_id");
                        storageLocationId = rs.getLong("storage_location_id");
                        requested = rs.getInt("pick_task_quantity_requested");
                        warehouseId = rs.getLong("warehouse_id");
                        reservationId = rs.getLong("stock_reservation_id");
                    }
                }

                if ("completed".equals(status)) {
                    throw new InvalidStateException("Pick task " + pickTaskId + " was already confirmed");
                }
                if ("cancelled".equals(status)) {
                    throw new InvalidStateException("Pick task " + pickTaskId + " is cancelled and cannot be confirmed");
                }
                if (quantityPicked > requested) {
                    throw new InvalidStateException("quantityPicked cannot exceed the requested quantity (" + requested + ")");
                }

                boolean shortPick = quantityPicked < requested;
                int shortfall = requested - quantityPicked;

                long confirmationId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO pick_confirmations
                            (pick_task_id, pick_confirmation_quantity_picked, pick_confirmation_picked_by,
                             pick_confirmation_short_pick_flag, pick_confirmation_short_pick_quantity)
                        VALUES (?, ?, ?, ?, ?)
                        RETURNING pick_confirmation_id
                        """)) {
                    ps.setLong(1, pickTaskId);
                    ps.setInt(2, quantityPicked);
                    ps.setString(3, confirmedBy);
                    ps.setBoolean(4, shortPick);
                    ps.setInt(5, shortPick ? shortfall : 0);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        confirmationId = rs.getLong("pick_confirmation_id");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE pick_tasks SET pick_task_status = 'completed' WHERE pick_task_id = ?")) {
                    ps.setLong(1, pickTaskId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE license_plates SET license_plate_status = 'consumed' WHERE license_plate_id = ?")) {
                    ps.setLong(1, licensePlateId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE stock_reservations SET stock_reservation_status = 'committed' WHERE stock_reservation_id = ?")) {
                    ps.setLong(1, reservationId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE order_lines
                        SET order_line_quantity_allocated = ?, order_line_status = 'picked'
                        WHERE order_line_id = ?
                        """)) {
                    ps.setInt(1, quantityPicked);
                    ps.setLong(2, orderLineId);
                    ps.executeUpdate();
                }

                // PICK ledger entry - only for what was ACTUALLY picked, not
                // the originally requested quantity. This is the row that
                // finally removes the stock from available_stock()'s count.
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO stock_movements
                            (product_id, warehouse_id, stock_movement_type, stock_movement_quantity_delta,
                             license_plate_id, storage_location_id, stock_movement_reference_table,
                             stock_movement_reference_id, stock_movement_created_by)
                        VALUES (?, ?, 'PICK', ?, ?, ?, 'pick_confirmations', ?, ?)
                        """)) {
                    ps.setLong(1, productId);
                    ps.setLong(2, warehouseId);
                    ps.setInt(3, -quantityPicked);
                    ps.setLong(4, licensePlateId);
                    ps.setLong(5, storageLocationId);
                    ps.setLong(6, confirmationId);
                    ps.setString(7, confirmedBy);
                    ps.executeUpdate();
                }

                if (shortPick) {
                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO backorders (order_line_id, backorder_quantity, backorder_status,
                                                     backorder_source_type, backorder_source_reference_id)
                            VALUES (?, ?, 'waiting', 'short_pick', ?)
                            """)) {
                        ps.setLong(1, orderLineId);
                        ps.setInt(2, shortfall);
                        ps.setLong(3, confirmationId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public static class InvalidStateException extends RuntimeException {
        public InvalidStateException(String message) {
            super(message);
        }
    }
}
