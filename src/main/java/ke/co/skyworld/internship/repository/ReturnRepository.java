package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.returns.ReturnRequest;
import ke.co.skyworld.internship.domain.beans.returns.ReturnResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReturnRepository {

    public static class InvalidStateException extends RuntimeException {
        public InvalidStateException(String message) {
            super(message);
        }
    }

    /**
     * "Received" happens at creation - see the design note at the top of
     * this feature: a return is logged once the unit has physically
     * arrived, mirroring goods receipt's intake semantics. Each line
     * writes a RETURN ledger entry immediately (audit only, excluded from
     * available_stock()'s counted types - same treatment as GOODS_RECEIPT)
     * since nothing is sellable yet until a human sets the condition flag.
     */
    public long createWithLines(long orderId, long warehouseId, List<ReturnRequest.Line> lines, String requestedBy)
            throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long customerId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT customer_id FROM orders WHERE order_id = ?")) {
                    ps.setLong(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InvalidStateException("Order not found: " + orderId);
                        customerId = rs.getLong("customer_id");
                    }
                }

                long returnId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO returns (order_id, customer_id, warehouse_id, return_requested_by)
                        VALUES (?, ?, ?, ?)
                        RETURNING return_id
                        """)) {
                    ps.setLong(1, orderId);
                    ps.setLong(2, customerId);
                    ps.setLong(3, warehouseId);
                    ps.setString(4, requestedBy);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        returnId = rs.getLong("return_id");
                    }
                }

                for (ReturnRequest.Line line : lines) {
                    long orderLineOrderId;
                    String orderLineStatus;
                    long productId;
                    try (PreparedStatement ps = conn.prepareStatement("""
                            SELECT order_id, order_line_status, product_id
                            FROM order_lines WHERE order_line_id = ? FOR UPDATE
                            """)) {
                        ps.setLong(1, line.getOrderLineId());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new InvalidStateException("Order line not found: " + line.getOrderLineId());
                            orderLineOrderId = rs.getLong("order_id");
                            orderLineStatus = rs.getString("order_line_status");
                            productId = rs.getLong("product_id");
                        }
                    }
                    if (orderLineOrderId != orderId) {
                        throw new InvalidStateException("Order line " + line.getOrderLineId() + " does not belong to order " + orderId);
                    }
                    if (!"shipped".equals(orderLineStatus)) {
                        throw new InvalidStateException("Order line " + line.getOrderLineId() + " is not 'shipped' - cannot return something that wasn't shipped");
                    }

                    long returnLineId;
                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO return_lines (return_id, order_line_id, product_id, return_line_quantity)
                            VALUES (?, ?, ?, ?)
                            RETURNING return_line_id
                            """)) {
                        ps.setLong(1, returnId);
                        ps.setLong(2, line.getOrderLineId());
                        ps.setLong(3, productId);
                        ps.setInt(4, line.getQuantity());
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            returnLineId = rs.getLong("return_line_id");
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO stock_movements
                                (product_id, warehouse_id, stock_movement_type, stock_movement_quantity_delta,
                                 stock_movement_reference_table, stock_movement_reference_id, stock_movement_created_by)
                            VALUES (?, ?, 'RETURN', ?, 'return_lines', ?, ?)
                            """)) {
                        ps.setLong(1, productId);
                        ps.setLong(2, warehouseId);
                        ps.setInt(3, line.getQuantity());
                        ps.setLong(4, returnLineId);
                        ps.setString(5, requestedBy);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return returnId;
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * THE gap-closing step. 'resellable' produces an LPN in
     * 'putaway_pending' status - identical to a QA-passed LPN, so the
     * existing putaway flow (task creation, confirmation, PUTAWAY ledger
     * write, BackorderAllocator trigger) works on it unmodified. 'damaged'
     * produces a 'quarantined' LPN and a QUARANTINE ledger entry (reusing
     * the existing type from phase 1) - never touches available stock.
     */
    public long processLine(long returnLineId, String condition, String licensePlateCode, String processedBy)
            throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long returnId, productId;
                int quantity;
                String lineStatus;
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT return_id, product_id, return_line_quantity, return_line_status
                        FROM return_lines WHERE return_line_id = ? FOR UPDATE
                        """)) {
                    ps.setLong(1, returnLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InvalidStateException("Return line not found: " + returnLineId);
                        returnId = rs.getLong("return_id");
                        productId = rs.getLong("product_id");
                        quantity = rs.getInt("return_line_quantity");
                        lineStatus = rs.getString("return_line_status");
                    }
                }
                if (!"pending_inspection".equals(lineStatus)) {
                    throw new InvalidStateException("Return line " + returnLineId + " has already been processed");
                }

                long warehouseId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT warehouse_id FROM returns WHERE return_id = ?")) {
                    ps.setLong(1, returnId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        warehouseId = rs.getLong("warehouse_id");
                    }
                }

                String lpnStatus = "resellable".equals(condition) ? "putaway_pending" : "quarantined";
                String code = (licensePlateCode != null && !licensePlateCode.isBlank())
                        ? licensePlateCode : "RET-" + returnLineId;

                long licensePlateId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO license_plates
                            (license_plate_code, product_id, license_plate_quantity, warehouse_id, license_plate_status)
                        VALUES (?, ?, ?, ?, ?::license_plate_status)
                        RETURNING license_plate_id
                        """)) {
                    ps.setString(1, code);
                    ps.setLong(2, productId);
                    ps.setInt(3, quantity);
                    ps.setLong(4, warehouseId);
                    ps.setString(5, lpnStatus);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        licensePlateId = rs.getLong("license_plate_id");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE return_lines
                        SET return_line_condition = ?::return_line_condition, return_line_status = 'processed',
                            license_plate_id = ?
                        WHERE return_line_id = ?
                        """)) {
                    ps.setString(1, condition);
                    ps.setLong(2, licensePlateId);
                    ps.setLong(3, returnLineId);
                    ps.executeUpdate();
                }

                if ("damaged".equals(condition)) {
                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO stock_movements
                                (product_id, warehouse_id, stock_movement_type, stock_movement_quantity_delta,
                                 license_plate_id, stock_movement_reference_table, stock_movement_reference_id,
                                 stock_movement_created_by)
                            VALUES (?, ?, 'QUARANTINE', ?, ?, 'license_plates', ?, ?)
                            """)) {
                        ps.setLong(1, productId);
                        ps.setLong(2, warehouseId);
                        ps.setInt(3, -quantity);
                        ps.setLong(4, licensePlateId);
                        ps.setLong(5, licensePlateId);
                        ps.setString(6, processedBy);
                        ps.executeUpdate();
                    }
                }
                // 'resellable' writes no extra ledger row here - the eventual
                // PUTAWAY entry via the reused putaway confirm flow is what
                // actually grants availability, consistent with how QA-pass
                // doesn't grant availability on its own either.

                // Roll up: mark the whole return 'completed' once every line
                // has been processed.
                boolean allProcessed;
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT bool_and(return_line_status = 'processed') AS all_processed
                        FROM return_lines WHERE return_id = ?
                        """)) {
                    ps.setLong(1, returnId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        allProcessed = rs.getBoolean("all_processed");
                    }
                }
                if (allProcessed) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE returns SET return_status = 'completed' WHERE return_id = ?")) {
                        ps.setLong(1, returnId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return licensePlateId;
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<ReturnResponse> findByIdWithLines(long returnId) throws SQLException {
        String headerSql = "SELECT * FROM returns WHERE return_id = ?";
        String linesSql = "SELECT * FROM return_lines WHERE return_id = ? ORDER BY return_line_id";

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            long orderId, customerId, warehouseId;
            String status;
            java.sql.Timestamp created;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, returnId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    orderId = rs.getLong("order_id");
                    customerId = rs.getLong("customer_id");
                    warehouseId = rs.getLong("warehouse_id");
                    status = rs.getString("return_status");
                    created = rs.getTimestamp("date_created");
                }
            }

            List<ReturnResponse.Line> lines = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(linesSql)) {
                ps.setLong(1, returnId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long lpnId = rs.getLong("license_plate_id");
                        Long lpnIdObj = rs.wasNull() ? null : lpnId;
                        lines.add(new ReturnResponse.Line(
                                rs.getLong("return_line_id"), rs.getLong("order_line_id"), rs.getLong("product_id"),
                                rs.getInt("return_line_quantity"), rs.getString("return_line_condition"),
                                rs.getString("return_line_status"), lpnIdObj));
                    }
                }
            }

            return Optional.of(new ReturnResponse(returnId, orderId, customerId, warehouseId, status, lines, created));
        }
    }
}
