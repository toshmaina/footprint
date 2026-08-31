package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.advanceshippingnotice.AdvanceShippingNoticeRequest;
import ke.co.skyworld.internship.domain.beans.advanceshippingnotice.AdvanceShippingNoticeResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvanceShippingNoticeRepository {

    private static AdvanceShippingNoticeResponse mapHeader(ResultSet rs, List<AdvanceShippingNoticeResponse.Line> lines)
            throws SQLException {
        long poId = rs.getLong("purchase_order_id");
        Long purchaseOrderId = rs.wasNull() ? null : poId;
        return new AdvanceShippingNoticeResponse(
                rs.getLong("advance_shipping_notice_id"),
                purchaseOrderId,
                rs.getLong("supplier_id"),
                rs.getLong("warehouse_id"),
                rs.getString("advance_shipping_notice_carrier"),
                rs.getTimestamp("advance_shipping_notice_expected_arrival"),
                rs.getString("advance_shipping_notice_status"),
                lines,
                rs.getTimestamp("date_created"),
                rs.getTimestamp("date_modified")
        );
    }

    private static AdvanceShippingNoticeResponse.Line mapLine(ResultSet rs) throws SQLException {
        return new AdvanceShippingNoticeResponse.Line(
                rs.getLong("advance_shipping_notice_line_id"),
                rs.getLong("product_id"),
                rs.getInt("advance_shipping_notice_line_quantity_expected"),
                rs.getString("advance_shipping_notice_line_lot_number"),
                rs.getString("advance_shipping_notice_line_packaging_type")
        );
    }

    public long createWithLines(Long purchaseOrderId, long supplierId, long warehouseId, String carrier,
                                java.util.Date expectedArrival, List<AdvanceShippingNoticeRequest.Line> lines)
            throws SQLException {
        String headerSql = """
                INSERT INTO advance_shipping_notices
                    (purchase_order_id, supplier_id, warehouse_id, advance_shipping_notice_carrier, advance_shipping_notice_expected_arrival)
                VALUES (?, ?, ?, ?, ?)
                RETURNING advance_shipping_notice_id
                """;
        String lineSql = """
                INSERT INTO advance_shipping_notice_lines
                    (advance_shipping_notice_id, product_id, advance_shipping_notice_line_quantity_expected,
                     advance_shipping_notice_line_lot_number, advance_shipping_notice_line_packaging_type)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long asnId;
                try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                    if (purchaseOrderId != null) {
                        ps.setLong(1, purchaseOrderId);
                    } else {
                        ps.setNull(1, Types.BIGINT);
                    }
                    ps.setLong(2, supplierId);
                    ps.setLong(3, warehouseId);
                    if (carrier != null && !carrier.isBlank()) {
                        ps.setString(4, carrier);
                    } else {
                        ps.setNull(4, Types.VARCHAR);
                    }
                    ps.setTimestamp(5, new Timestamp(expectedArrival.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        asnId = rs.getLong("advance_shipping_notice_id");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(lineSql)) {
                    for (AdvanceShippingNoticeRequest.Line line : lines) {
                        ps.setLong(1, asnId);
                        ps.setLong(2, line.getProductId());
                        ps.setInt(3, line.getQuantityExpected());
                        if (line.getLotNumber() != null && !line.getLotNumber().isBlank()) {
                            ps.setString(4, line.getLotNumber());
                        } else {
                            ps.setNull(4, Types.VARCHAR);
                        }
                        if (line.getPackagingType() != null && !line.getPackagingType().isBlank()) {
                            ps.setString(5, line.getPackagingType());
                        } else {
                            ps.setNull(5, Types.VARCHAR);
                        }
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                return asnId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<AdvanceShippingNoticeResponse> findByIdWithLines(long asnId) throws SQLException {
        String headerSql = "SELECT * FROM advance_shipping_notices WHERE advance_shipping_notice_id = ?";
        String linesSql = "SELECT * FROM advance_shipping_notice_lines WHERE advance_shipping_notice_id = ? " +
                "ORDER BY advance_shipping_notice_line_id";

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            AdvanceShippingNoticeResponse header;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, asnId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return Optional.empty();
                    }
                    header = mapHeader(rs, new ArrayList<>());
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(linesSql)) {
                ps.setLong(1, asnId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        header.getLines().add(mapLine(rs));
                    }
                }
            }
            return Optional.of(header);
        }
    }

    public PageResult<AdvanceShippingNoticeResponse> list(int page, int pageSize) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        String countSql = "SELECT COUNT(*) FROM advance_shipping_notices";
        String listSql = "SELECT * FROM advance_shipping_notices ORDER BY advance_shipping_notice_id DESC LIMIT ? OFFSET ?";

        List<AdvanceShippingNoticeResponse> items = new ArrayList<>();
        long total;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            try (PreparedStatement countPs = conn.prepareStatement(countSql);
                 ResultSet countRs = countPs.executeQuery()) {
                countRs.next();
                total = countRs.getLong(1);
            }
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
}