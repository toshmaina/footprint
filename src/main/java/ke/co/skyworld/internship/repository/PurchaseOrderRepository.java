package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.purchaseorder.PurchaseOrderRequest;
import ke.co.skyworld.internship.domain.beans.purchaseorder.PurchaseOrderResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PurchaseOrderRepository {

    private static PurchaseOrderResponse mapHeader(ResultSet rs, List<PurchaseOrderResponse.Line> lines) throws SQLException {
        Timestamp expected = rs.getTimestamp("purchase_order_expected_date");
        return new PurchaseOrderResponse(
                rs.getLong("purchase_order_id"),
                rs.getLong("supplier_id"),
                rs.getLong("warehouse_id"),
                rs.getString("purchase_order_status"),
                expected,
                rs.getString("purchase_order_created_by"),
                lines,
                rs.getTimestamp("date_created"),
                rs.getTimestamp("date_modified")
        );
    }

    private static PurchaseOrderResponse.Line mapLine(ResultSet rs) throws SQLException {
        return new PurchaseOrderResponse.Line(
                rs.getLong("purchase_order_line_id"),
                rs.getLong("product_id"),
                rs.getInt("purchase_order_line_quantity_ordered"),
                rs.getBigDecimal("purchase_order_line_unit_cost"),
                rs.getString("purchase_order_line_status")
        );
    }

    /**
     * Creates the header and all lines in one transaction - either the
     * whole PO exists with every line, or none of it does. A partially
     * inserted PO (header committed, some lines missing because one failed)
     * would be a real data-integrity problem, so this is not optional.
     */
    public long createWithLines(long supplierId, long warehouseId, java.util.Date expectedDate,
                                String createdBy, List<PurchaseOrderRequest.Line> lines) throws SQLException {
        String headerSql = """
                INSERT INTO purchase_orders (supplier_id, warehouse_id, purchase_order_expected_date, purchase_order_created_by)
                VALUES (?, ?, ?, ?)
                RETURNING purchase_order_id
                """;
        String lineSql = """
                INSERT INTO purchase_order_lines (purchase_order_id, product_id, purchase_order_line_quantity_ordered, purchase_order_line_unit_cost)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long purchaseOrderId;
                try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                    ps.setLong(1, supplierId);
                    ps.setLong(2, warehouseId);
                    if (expectedDate != null) {
                        ps.setDate(3, new java.sql.Date(expectedDate.getTime()));
                    } else {
                        ps.setNull(3, Types.DATE);
                    }
                    ps.setString(4, createdBy);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        purchaseOrderId = rs.getLong("purchase_order_id");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(lineSql)) {
                    for (PurchaseOrderRequest.Line line : lines) {
                        ps.setLong(1, purchaseOrderId);
                        ps.setLong(2, line.getProductId());
                        ps.setInt(3, line.getQuantityOrdered());
                        ps.setBigDecimal(4, line.getUnitCost());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return purchaseOrderId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<PurchaseOrderResponse> findByIdWithLines(long purchaseOrderId) throws SQLException {
        String headerSql = "SELECT * FROM purchase_orders WHERE purchase_order_id = ?";
        String linesSql = "SELECT * FROM purchase_order_lines WHERE purchase_order_id = ? ORDER BY purchase_order_line_id";

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            PurchaseOrderResponse header;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, purchaseOrderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    header = mapHeader(rs, new ArrayList<>());
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(linesSql)) {
                ps.setLong(1, purchaseOrderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        header.getLines().add(mapLine(rs));
                    }
                }
            }

            return Optional.of(header);
        }
    }

    public PageResult<PurchaseOrderResponse> list(int page, int pageSize) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        String countSql = "SELECT COUNT(*) FROM purchase_orders";
        String listSql = "SELECT * FROM purchase_orders ORDER BY purchase_order_id DESC LIMIT ? OFFSET ?";

        List<PurchaseOrderResponse> items = new ArrayList<>();
        long total;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            try (PreparedStatement countPs = conn.prepareStatement(countSql);
                 ResultSet countRs = countPs.executeQuery()) {
                countRs.next();
                total = countRs.getLong(1);
            }
            // List view intentionally omits lines - keeps the list endpoint
            // cheap. Fetch individual POs via GET /purchase-orders/{id} for lines.
            try (PreparedStatement listPs = conn.prepareStatement(listSql)) {
                listPs.setInt(1, pageSize);
                listPs.setInt(2, offset);
                try (ResultSet rs = listPs.executeQuery()) {
                    while (rs.next()) {
                        items.add(mapHeader(rs, null));
                    }
                }
            }
        }
        return new PageResult<>(items, total);
    }

    /**
     * Only allowed from 'draft' or 'sent' - once anything has been received
     * against a PO, cancelling it would contradict physical reality (stock
     * already arrived). Returns false if the PO doesn't exist OR is in a
     * status that can't be cancelled - caller distinguishes via a follow-up
     * findByIdWithLines() call if it needs to know which.
     */
    public boolean cancel(long purchaseOrderId) throws SQLException {
        String sql = """
                UPDATE purchase_orders
                SET purchase_order_status = 'cancelled'
                WHERE purchase_order_id = ?
                  AND purchase_order_status IN ('draft', 'sent')
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, purchaseOrderId);
            return ps.executeUpdate() > 0;
        }
    }
}