package ke.co.skyworld.internship.repository;



import ke.co.skyworld.internship.domain.beans.goodsreceipt.GoodsReceiptRequest;
import ke.co.skyworld.internship.domain.beans.goodsreceipt.GoodsReceiptResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GoodsReceiptRepository {

    /**
     * Creates the receipt header, every line, an auto-detected discrepancy
     * row for any line where quantityCounted != quantityExpected, and a
     * GOODS_RECEIPT ledger entry per line - all in one transaction. The
     * ledger entry is audit-trail only: GOODS_RECEIPT is deliberately
     * excluded from available_stock()'s counted movement types (phase 1's
     * whole point - nothing is "available" until PUTAWAY confirms it).
     */
    public long createWithLines(Long asnId, Long dockAppointmentId, long warehouseId, String receivedBy,
                                List<GoodsReceiptRequest.Line> lines) throws SQLException {
        String headerSql = """
                INSERT INTO goods_receipts (advance_shipping_notice_id, dock_appointment_id, warehouse_id, goods_receipt_received_by)
                VALUES (?, ?, ?, ?)
                RETURNING goods_receipt_id
                """;
        String lineSql = """
                INSERT INTO goods_receipt_lines
                    (goods_receipt_id, purchase_order_line_id, product_id, goods_receipt_line_quantity_counted,
                     goods_receipt_line_quantity_expected, goods_receipt_line_lot_number, goods_receipt_line_condition)
                VALUES (?, ?, ?, ?, ?, ?, ?::goods_receipt_line_condition)
                RETURNING goods_receipt_line_id
                """;
        String discrepancySql = """
                INSERT INTO goods_receipt_discrepancies
                    (goods_receipt_line_id, goods_receipt_discrepancy_type, goods_receipt_discrepancy_quantity_difference)
                VALUES (?, ?::goods_receipt_discrepancy_type, ?)
                """;
        String ledgerSql = """
                INSERT INTO stock_movements
                    (product_id, warehouse_id, stock_movement_type, stock_movement_quantity_delta,
                     stock_movement_reference_table, stock_movement_reference_id, stock_movement_created_by)
                VALUES (?, ?, 'GOODS_RECEIPT', ?, 'goods_receipt_lines', ?, ?)
                """;

        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                long goodsReceiptId;
                try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                    if (asnId != null) ps.setLong(1, asnId); else ps.setNull(1, Types.BIGINT);
                    if (dockAppointmentId != null) ps.setLong(2, dockAppointmentId); else ps.setNull(2, Types.BIGINT);
                    ps.setLong(3, warehouseId);
                    ps.setString(4, receivedBy);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        goodsReceiptId = rs.getLong("goods_receipt_id");
                    }
                }

                for (GoodsReceiptRequest.Line line : lines) {
                    long lineId;
                    try (PreparedStatement ps = conn.prepareStatement(lineSql)) {
                        ps.setLong(1, goodsReceiptId);
                        if (line.getPurchaseOrderLineId() != null) ps.setLong(2, line.getPurchaseOrderLineId());
                        else ps.setNull(2, Types.BIGINT);
                        ps.setLong(3, line.getProductId());
                        ps.setInt(4, line.getQuantityCounted());
                        if (line.getQuantityExpected() != null) ps.setInt(5, line.getQuantityExpected());
                        else ps.setNull(5, Types.INTEGER);
                        if (line.getLotNumber() != null && !line.getLotNumber().isBlank()) ps.setString(6, line.getLotNumber());
                        else ps.setNull(6, Types.VARCHAR);
                        String condition = (line.getConditionFlag() != null && !line.getConditionFlag().isBlank())
                                ? line.getConditionFlag() : "unverified";
                        ps.setString(7, condition);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            lineId = rs.getLong("goods_receipt_line_id");
                        }
                    }

                    // Auto-detect discrepancy: only possible if an expected
                    // quantity was actually given (unlinked receipts have none).
                    if (line.getQuantityExpected() != null
                            && !line.getQuantityExpected().equals(line.getQuantityCounted())) {
                        int diff = line.getQuantityCounted() - line.getQuantityExpected();
                        String type = diff > 0 ? "over_received" : "under_received";
                        try (PreparedStatement ps = conn.prepareStatement(discrepancySql)) {
                            ps.setLong(1, lineId);
                            ps.setString(2, type);
                            ps.setInt(3, diff);
                            ps.executeUpdate();
                        }
                    }

                    // Audit-trail ledger entry - does NOT make stock available.
                    try (PreparedStatement ps = conn.prepareStatement(ledgerSql)) {
                        ps.setLong(1, line.getProductId());
                        ps.setLong(2, warehouseId);
                        ps.setInt(3, line.getQuantityCounted());
                        ps.setLong(4, lineId);
                        ps.setString(5, receivedBy);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return goodsReceiptId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<GoodsReceiptResponse> findByIdWithLines(long goodsReceiptId) throws SQLException {
        String headerSql = "SELECT * FROM goods_receipts WHERE goods_receipt_id = ?";
        String linesSql = """
                SELECT grl.*, (d.goods_receipt_discrepancy_id IS NOT NULL) AS has_discrepancy
                FROM goods_receipt_lines grl
                LEFT JOIN goods_receipt_discrepancies d ON d.goods_receipt_line_id = grl.goods_receipt_line_id
                WHERE grl.goods_receipt_id = ?
                ORDER BY grl.goods_receipt_line_id
                """;

        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            GoodsReceiptResponse header;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, goodsReceiptId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    long asnId = rs.getLong("advance_shipping_notice_id");
                    Long asn = rs.wasNull() ? null : asnId;
                    header = new GoodsReceiptResponse(
                            rs.getLong("goods_receipt_id"), asn, rs.getLong("warehouse_id"),
                            rs.getString("goods_receipt_received_by"), rs.getTimestamp("goods_receipt_received_at"),
                            rs.getString("goods_receipt_status"), new ArrayList<>(),
                            rs.getTimestamp("date_created"), rs.getTimestamp("date_modified"));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(linesSql)) {
                ps.setLong(1, goodsReceiptId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int expected = rs.getInt("goods_receipt_line_quantity_expected");
                        Integer expectedObj = rs.wasNull() ? null : expected;
                        header.getLines().add(new GoodsReceiptResponse.Line(
                                rs.getLong("goods_receipt_line_id"), rs.getLong("product_id"),
                                rs.getInt("goods_receipt_line_quantity_counted"), expectedObj,
                                rs.getString("goods_receipt_line_lot_number"),
                                rs.getString("goods_receipt_line_condition"),
                                rs.getBoolean("has_discrepancy")));
                    }
                }
            }
            return Optional.of(header);
        }
    }

    public PageResult<GoodsReceiptResponse> list(int page, int pageSize) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        String countSql = "SELECT COUNT(*) FROM goods_receipts";
        String listSql = "SELECT * FROM goods_receipts ORDER BY goods_receipt_id DESC LIMIT ? OFFSET ?";

        List<GoodsReceiptResponse> items = new ArrayList<>();
        long total;
        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(countSql); ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(listSql)) {
                ps.setInt(1, pageSize);
                ps.setInt(2, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long asnId = rs.getLong("advance_shipping_notice_id");
                        Long asn = rs.wasNull() ? null : asnId;
                        items.add(new GoodsReceiptResponse(
                                rs.getLong("goods_receipt_id"), asn, rs.getLong("warehouse_id"),
                                rs.getString("goods_receipt_received_by"), rs.getTimestamp("goods_receipt_received_at"),
                                rs.getString("goods_receipt_status"), null,
                                rs.getTimestamp("date_created"), rs.getTimestamp("date_modified")));
                    }
                }
            }
        }
        return new PageResult<>(items, total);
    }
}