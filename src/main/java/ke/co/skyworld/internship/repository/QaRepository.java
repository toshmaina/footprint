package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.util.List;

public class QaRepository {

    private static void updateStatus(Connection conn, String sql, long licensePlateId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, licensePlateId);
            ps.executeUpdate();
        }
    }

    private static void writeLedger(Connection conn, String sql, long productId, long warehouseId, String type,
                                    int delta, long licensePlateId, String createdBy) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, warehouseId);
            ps.setString(3, type);
            ps.setInt(4, delta);
            ps.setLong(5, licensePlateId);
            ps.setLong(6, licensePlateId);
            ps.setString(7, createdBy);
            ps.executeUpdate();
        }
    }

    private static long insertChild(Connection conn, String sql, String code, Long lineId, long productId,
                                    int quantity, long warehouseId, String status, long parentId,
                                    String splitBy, long resultId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            if (lineId != null) ps.setLong(2, lineId);
            else ps.setNull(2, Types.BIGINT);
            ps.setLong(3, productId);
            ps.setInt(4, quantity);
            ps.setLong(5, warehouseId);
            ps.setString(6, status);
            ps.setLong(7, parentId);
            ps.setString(8, splitBy);
            ps.setLong(9, resultId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("license_plate_id");
            }
        }
    }

    /**
     * Records the inspection and its disposition together (see the API-level
     * consolidation note - two tables underneath, one call from the client).
     * Locks the license_plate row for the duration of the transaction; on
     * 'partial' disposition, splits it into two children using the lineage
     * columns and conservation trigger from the phase-1 LPN-splitting work.
     */
    public QaOutcome recordInspectionWithResult(long licensePlateId, String inspector, Integer sampleSize,
                                                String method, String disposition, Integer failedQuantity,
                                                String notes) throws SQLException {
        String lockLpnSql = """
                SELECT license_plate_code, goods_receipt_line_id, product_id, license_plate_quantity,
                       warehouse_id, license_plate_status
                FROM license_plates
                WHERE license_plate_id = ?
                FOR UPDATE
                """;
        String insertInspectionSql = """
                INSERT INTO quality_assurance_inspections
                    (goods_receipt_line_id, license_plate_id, quality_assurance_inspection_inspector,
                     quality_assurance_inspection_sample_size, quality_assurance_inspection_method)
                VALUES (?, ?, ?, ?, ?::quality_assurance_inspection_method)
                RETURNING quality_assurance_inspection_id
                """;
        String insertResultSql = """
                INSERT INTO quality_assurance_inspection_results
                    (quality_assurance_inspection_id, quality_assurance_inspection_result_disposition,
                     quality_assurance_inspection_result_failed_quantity, quality_assurance_inspection_result_notes)
                VALUES (?, ?::quality_assurance_inspection_disposition, ?, ?)
                RETURNING quality_assurance_inspection_result_id
                """;
        String updateLpnStatusSql = "UPDATE license_plates SET license_plate_status = ?::license_plate_status WHERE license_plate_id = ?";
        String insertChildLpnSql = """
                INSERT INTO license_plates
                    (license_plate_code, goods_receipt_line_id, product_id, license_plate_quantity, warehouse_id,
                     license_plate_status, parent_license_plate_id, license_plate_split_reason,
                     license_plate_split_performed_by, license_plate_split_performed_at,
                     quality_assurance_inspection_result_id)
                VALUES (?, ?, ?, ?, ?, ?::license_plate_status, ?, 'qa_partial_disposition', ?, NOW(), ?)
                RETURNING license_plate_id
                """;
        String ledgerSql = """
                INSERT INTO stock_movements
                    (product_id, warehouse_id, stock_movement_type, stock_movement_quantity_delta,
                     license_plate_id, stock_movement_reference_table, stock_movement_reference_id, stock_movement_created_by)
                VALUES (?, ?, ?::stock_movement_type, ?, ?, 'license_plates', ?, ?)
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                String code;
                Long lineId;
                long productId;
                int quantity;
                long warehouseId;
                String currentStatus;
                try (PreparedStatement ps = conn.prepareStatement(lockLpnSql)) {
                    ps.setLong(1, licensePlateId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new InvalidDispositionException("License plate not found: " + licensePlateId);
                        }
                        code = rs.getString("license_plate_code");
                        long lid = rs.getLong("goods_receipt_line_id");
                        lineId = rs.wasNull() ? null : lid;
                        productId = rs.getLong("product_id");
                        quantity = rs.getInt("license_plate_quantity");
                        warehouseId = rs.getLong("warehouse_id");
                        currentStatus = rs.getString("license_plate_status");
                    }
                }

                if (!"receiving".equals(currentStatus) && !"qa_hold".equals(currentStatus)) {
                    throw new InvalidDispositionException(
                            "License plate is in status '" + currentStatus + "' and cannot be inspected");
                }

                long inspectionId;
                try (PreparedStatement ps = conn.prepareStatement(insertInspectionSql)) {
                    if (lineId != null) ps.setLong(1, lineId);
                    else ps.setNull(1, Types.BIGINT);
                    ps.setLong(2, licensePlateId);
                    ps.setString(3, inspector);
                    if (sampleSize != null) ps.setInt(4, sampleSize);
                    else ps.setNull(4, Types.INTEGER);
                    ps.setString(5, (method != null && !method.isBlank()) ? method : "visual");
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        inspectionId = rs.getLong("quality_assurance_inspection_id");
                    }
                }

                int recordedFailedQuantity = switch (disposition) {
                    case "pass" -> 0;
                    case "fail" -> quantity;
                    case "partial" -> failedQuantity;
                    default -> throw new InvalidDispositionException("disposition must be pass, fail, or partial");
                };

                long resultId;
                try (PreparedStatement ps = conn.prepareStatement(insertResultSql)) {
                    ps.setLong(1, inspectionId);
                    ps.setString(2, disposition);
                    ps.setInt(3, recordedFailedQuantity);
                    if (notes != null && !notes.isBlank()) ps.setString(4, notes);
                    else ps.setNull(4, Types.VARCHAR);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        resultId = rs.getLong("quality_assurance_inspection_result_id");
                    }
                }

                List<Long> resultingIds;

                switch (disposition) {
                    case "pass" -> {
                        updateStatus(conn, updateLpnStatusSql, licensePlateId, "putaway_pending");
                        writeLedger(conn, ledgerSql, productId, warehouseId, "QA_RELEASE", quantity, licensePlateId, inspector);
                        resultingIds = List.of(licensePlateId);
                    }
                    case "fail" -> {
                        updateStatus(conn, updateLpnStatusSql, licensePlateId, "quarantined");
                        writeLedger(conn, ledgerSql, productId, warehouseId, "QA_REJECT", -quantity, licensePlateId, inspector);
                        resultingIds = List.of(licensePlateId);
                    }
                    case "partial" -> {
                        if (failedQuantity == null || failedQuantity <= 0 || failedQuantity >= quantity) {
                            throw new InvalidDispositionException(
                                    "For a partial disposition, failedQuantity must be strictly between 0 and the LPN's quantity (" + quantity + ")");
                        }
                        int passedQuantity = quantity - failedQuantity;

                        long passChildId = insertChild(conn, insertChildLpnSql, code + "-P", lineId, productId,
                                passedQuantity, warehouseId, "putaway_pending", licensePlateId, inspector, resultId);
                        long failChildId = insertChild(conn, insertChildLpnSql, code + "-F", lineId, productId,
                                failedQuantity, warehouseId, "quarantined", licensePlateId, inspector, resultId);

                        updateStatus(conn, updateLpnStatusSql, licensePlateId, "consumed");

                        writeLedger(conn, ledgerSql, productId, warehouseId, "QA_RELEASE", passedQuantity, passChildId, inspector);
                        writeLedger(conn, ledgerSql, productId, warehouseId, "QA_REJECT", -failedQuantity, failChildId, inspector);

                        resultingIds = List.of(passChildId, failChildId);
                    }
                    default -> throw new InvalidDispositionException("disposition must be pass, fail, or partial");
                }

                conn.commit();
                return new QaOutcome(inspectionId, resultId, resultingIds);
            } catch (SQLException | InvalidDispositionException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public record QaOutcome(long inspectionId, long resultId, List<Long> resultingLicensePlateIds) {
    }

    public static class InvalidDispositionException extends RuntimeException {
        public InvalidDispositionException(String message) {
            super(message);
        }
    }
}
