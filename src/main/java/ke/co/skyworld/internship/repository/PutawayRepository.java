package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;

public class PutawayRepository {

    /**
     * Only allowed while the LPN is 'putaway_pending' (QA-passed, not yet
     * stored). Locks the license_plate row for the check to avoid a race
     * against a concurrent putaway confirmation flipping its status
     * underneath this call.
     */
    public long createTask(long licensePlateId, long suggestedStorageLocationId, String assignedTo) throws SQLException {
        String lockLpnSql = "SELECT license_plate_status FROM license_plates WHERE license_plate_id = ? FOR UPDATE";
        String insertTaskSql = """
                INSERT INTO putaway_tasks (license_plate_id, suggested_storage_location_id, putaway_task_assigned_to, putaway_task_status)
                VALUES (?, ?, ?, ?::putaway_task_status)
                RETURNING putaway_task_id
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                String status;
                try (PreparedStatement ps = conn.prepareStatement(lockLpnSql)) {
                    ps.setLong(1, licensePlateId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new InvalidStateException("License plate not found: " + licensePlateId);
                        }
                        status = rs.getString("license_plate_status");
                    }
                }
                if (!"putaway_pending".equals(status)) {
                    throw new InvalidStateException(
                            "License plate is in status '" + status + "', not 'putaway_pending' - cannot create a putaway task");
                }

                long taskId;
                try (PreparedStatement ps = conn.prepareStatement(insertTaskSql)) {
                    ps.setLong(1, licensePlateId);
                    ps.setLong(2, suggestedStorageLocationId);
                    if (assignedTo != null && !assignedTo.isBlank()) {
                        ps.setString(3, assignedTo);
                        ps.setString(4, "assigned");
                    } else {
                        ps.setNull(3, Types.VARCHAR);
                        ps.setString(4, "pending");
                    }
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        taskId = rs.getLong("putaway_task_id");
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
     * THE concurrency-critical operation this entire schema's double-putaway
     * guard exists for. Two layers of protection, deliberately redundant:
     * 1. Row lock on putaway_tasks (SELECT ... FOR UPDATE) - the first
     * concurrent caller to reach this wins the lock; the second blocks
     * until the first commits, then sees the task already confirmed
     * and exits via the status check below.
     * 2. UNIQUE(putaway_task_id) on putaway_confirmations - the real
     * backstop if, for whatever reason, two processes ever bypass the
     * row lock (e.g. different connection pools, a future refactor
     * that forgets the lock). The INSERT itself will throw 23505.
     * Belt and suspenders is deliberate here, not redundant - this is the
     * exact failure mode ("phantom stock") the whole project opened with.
     */
    public void confirmTask(long putawayTaskId, long actualStorageLocationId, String confirmedBy) throws SQLException {
        String lockTaskSql = """
                SELECT pt.putaway_task_status, pt.license_plate_id, lp.product_id, lp.warehouse_id, lp.license_plate_quantity
                FROM putaway_tasks pt
                JOIN license_plates lp ON lp.license_plate_id = pt.license_plate_id
                WHERE pt.putaway_task_id = ?
                FOR UPDATE OF pt
                """;
        String insertConfirmationSql = """
                INSERT INTO putaway_confirmations (putaway_task_id, actual_storage_location_id, putaway_confirmation_confirmed_by)
                VALUES (?, ?, ?)
                """;
        String updateTaskSql = "UPDATE putaway_tasks SET putaway_task_status = 'confirmed' WHERE putaway_task_id = ?";
        String updateLpnSql = """
                UPDATE license_plates
                SET current_storage_location_id = ?, license_plate_status = 'stored'
                WHERE license_plate_id = ?
                """;
        String ledgerSql = """
                INSERT INTO stock_movements
                    (product_id, warehouse_id, stock_movement_type, stock_movement_quantity_delta,
                     license_plate_id, storage_location_id, stock_movement_reference_table,
                     stock_movement_reference_id, stock_movement_created_by)
                VALUES (?, ?, 'PUTAWAY', ?, ?, ?, 'putaway_confirmations', ?, ?)
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                String status;
                long licensePlateId;
                long productId;
                long warehouseId;
                int quantity;
                try (PreparedStatement ps = conn.prepareStatement(lockTaskSql)) {
                    ps.setLong(1, putawayTaskId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new InvalidStateException("Putaway task not found: " + putawayTaskId);
                        }
                        status = rs.getString("putaway_task_status");
                        licensePlateId = rs.getLong("license_plate_id");
                        productId = rs.getLong("product_id");
                        warehouseId = rs.getLong("warehouse_id");
                        quantity = rs.getInt("license_plate_quantity");
                    }
                }

                if ("confirmed".equals(status)) {
                    throw new InvalidStateException("Putaway task " + putawayTaskId + " was already confirmed");
                }
                if ("cancelled".equals(status)) {
                    throw new InvalidStateException("Putaway task " + putawayTaskId + " is cancelled and cannot be confirmed");
                }

                long confirmationId;
                try (PreparedStatement ps = conn.prepareStatement(insertConfirmationSql)) {
                    ps.setLong(1, putawayTaskId);
                    ps.setLong(2, actualStorageLocationId);
                    ps.setString(3, confirmedBy);
                    ps.executeUpdate();
                }
                // Fetch the id we just inserted for the ledger reference -
                // simplest reliable way given the table's PK is identity-generated
                // and we didn't use RETURNING above for a plain executeUpdate.
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT putaway_confirmation_id FROM putaway_confirmations WHERE putaway_task_id = ?")) {
                    ps.setLong(1, putawayTaskId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        confirmationId = rs.getLong("putaway_confirmation_id");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(updateTaskSql)) {
                    ps.setLong(1, putawayTaskId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(updateLpnSql)) {
                    ps.setLong(1, actualStorageLocationId);
                    ps.setLong(2, licensePlateId);
                    ps.executeUpdate();
                }

                // The row that finally makes this stock "available" per
                // available_stock()'s counted movement types.
                try (PreparedStatement ps = conn.prepareStatement(ledgerSql)) {
                    ps.setLong(1, productId);
                    ps.setLong(2, warehouseId);
                    ps.setInt(3, quantity);
                    ps.setLong(4, licensePlateId);
                    ps.setLong(5, actualStorageLocationId);
                    ps.setLong(6, confirmationId);
                    ps.setString(7, confirmedBy);
                    ps.executeUpdate();
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