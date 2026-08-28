package ke.co.skyworld.internship.repository;

import ke.co.skyworld.internship.domain.beans.licenseplates.CreateLicensePlatesRequest;
import ke.co.skyworld.internship.domain.beans.licenseplates.LicensePlateResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LicensePlateRepository {

    /**
     * Splits a received line's counted quantity across one or more physical
     * LPNs (pallets). Locks the goods_receipt_line row with SELECT ... FOR
     * UPDATE for the duration of the transaction - without it, two
     * concurrent calls creating LPNs against the same line could both read
     * "quantity so far: 0" and together create more LPN quantity than the
     * line actually received, the same class of race the putaway/reservation
     * guards exist to prevent elsewhere in this project.
     */
    public List<Long> createFromReceiptLine(long goodsReceiptLineId, List<CreateLicensePlatesRequest.Plate> plates)
            throws SQLException {
        String lockLineSql = """
                SELECT grl.product_id, grl.goods_receipt_line_quantity_counted, gr.warehouse_id
                FROM goods_receipt_lines grl
                JOIN goods_receipts gr ON gr.goods_receipt_id = grl.goods_receipt_id
                WHERE grl.goods_receipt_line_id = ?
                FOR UPDATE OF grl
                """;
        String existingSumSql = """
                SELECT COALESCE(SUM(license_plate_quantity), 0) AS existing_total
                FROM license_plates
                WHERE goods_receipt_line_id = ?
                """;
        String insertSql = """
                INSERT INTO license_plates
                    (license_plate_code, goods_receipt_line_id, product_id, license_plate_quantity,
                     license_plate_lot_number, warehouse_id, license_plate_status)
                VALUES (?, ?, ?, ?, ?, ?, 'receiving')
                RETURNING license_plate_id
                """;

        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                long productId;
                int lineQuantity;
                long warehouseId;
                try (PreparedStatement ps = conn.prepareStatement(lockLineSql)) {
                    ps.setLong(1, goodsReceiptLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Goods receipt line not found: " + goodsReceiptLineId);
                        }
                        productId = rs.getLong("product_id");
                        lineQuantity = rs.getInt("goods_receipt_line_quantity_counted");
                        warehouseId = rs.getLong("warehouse_id");
                    }
                }

                int existingTotal;
                try (PreparedStatement ps = conn.prepareStatement(existingSumSql)) {
                    ps.setLong(1, goodsReceiptLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        existingTotal = rs.getInt("existing_total");
                    }
                }

                int requestedTotal = plates.stream().mapToInt(CreateLicensePlatesRequest.Plate::getQuantity).sum();
                if (existingTotal + requestedTotal > lineQuantity) {
                    throw new LicensePlateQuantityExceededException(
                            "License plate quantities (" + (existingTotal + requestedTotal)
                                    + ") would exceed the line's received quantity (" + lineQuantity + ")");
                }

                List<Long> createdIds = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    for (CreateLicensePlatesRequest.Plate plate : plates) {
                        ps.setString(1, plate.getCode());
                        ps.setLong(2, goodsReceiptLineId);
                        ps.setLong(3, productId);
                        ps.setInt(4, plate.getQuantity());
                        if (plate.getLotNumber() != null && !plate.getLotNumber().isBlank()) {
                            ps.setString(5, plate.getLotNumber());
                        } else {
                            ps.setNull(5, Types.VARCHAR);
                        }
                        ps.setLong(6, warehouseId);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            createdIds.add(rs.getLong("license_plate_id"));
                        }
                    }
                }

                conn.commit();
                return createdIds;
            } catch (SQLException | LicensePlateQuantityExceededException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Thrown when the requested license plate quantities would exceed what
     * was actually received on the line - a business-rule violation, not a
     * database error, so it's kept separate from SQLException rather than
     * faked through with a made-up SQLState.
     */
    public static class LicensePlateQuantityExceededException extends RuntimeException {
        public LicensePlateQuantityExceededException(String message) {
            super(message);
        }
    }

    public Optional<LicensePlateResponse> findById(long licensePlateId) throws SQLException {
        String sql = "SELECT * FROM license_plates WHERE license_plate_id = ?";
        try (Connection conn = ConnectionPool.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, licensePlateId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    static LicensePlateResponse mapRow(ResultSet rs) throws SQLException {
        long lineId = rs.getLong("goods_receipt_line_id");
        Long lineIdObj = rs.wasNull() ? null : lineId;
        long locId = rs.getLong("current_storage_location_id");
        Long locIdObj = rs.wasNull() ? null : locId;
        long parentId = rs.getLong("parent_license_plate_id");
        Long parentIdObj = rs.wasNull() ? null : parentId;
        return new LicensePlateResponse(
                rs.getLong("license_plate_id"), rs.getString("license_plate_code"), lineIdObj,
                rs.getLong("product_id"), rs.getInt("license_plate_quantity"), rs.getString("license_plate_lot_number"),
                rs.getLong("warehouse_id"), locIdObj, rs.getString("license_plate_status"), parentIdObj,
                rs.getTimestamp("date_created"), rs.getTimestamp("date_modified"));
    }
}