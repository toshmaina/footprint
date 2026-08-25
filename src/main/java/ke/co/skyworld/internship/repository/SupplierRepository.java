package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.supplier.SupplierResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SupplierRepository {

    public long create(String name, String contactEmail, String contactPhone, Integer leadTimeDays) throws SQLException {
        String sql = """
                INSERT INTO suppliers (supplier_name, supplier_contact_email, supplier_contact_phone,
                                        supplier_default_lead_time_days)
                VALUES (?, ?, ?, ?)
                RETURNING supplier_id
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            setNullableString(ps, 2, contactEmail);
            setNullableString(ps, 3, contactPhone);
            if (leadTimeDays != null) {
                ps.setInt(4, leadTimeDays);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("supplier_id");
            }
        }
    }

    public Optional<SupplierResponse> findById(long supplierId) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public PageResult<SupplierResponse> list(int page, int pageSize, boolean includeInactive) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        String whereClause = includeInactive ? "" : "WHERE supplier_is_active = TRUE";
        String countSql = "SELECT COUNT(*) FROM suppliers " + whereClause;
        String listSql = "SELECT * FROM suppliers " + whereClause + " ORDER BY supplier_id LIMIT ? OFFSET ?";

        List<SupplierResponse> items = new ArrayList<>();
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
                    while (rs.next()) items.add(mapRow(rs));
                }
            }
        }
        return new PageResult<>(items, total);
    }

    public boolean update(long supplierId, String name, String contactEmail, String contactPhone,
                          Integer leadTimeDays) throws SQLException {
        String sql = """
                UPDATE suppliers
                SET supplier_name = ?, supplier_contact_email = ?, supplier_contact_phone = ?,
                    supplier_default_lead_time_days = ?
                WHERE supplier_id = ?
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            setNullableString(ps, 2, contactEmail);
            setNullableString(ps, 3, contactPhone);
            if (leadTimeDays != null) {
                ps.setInt(4, leadTimeDays);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setLong(5, supplierId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deactivate(long supplierId) throws SQLException {
        String sql = "UPDATE suppliers SET supplier_is_active = FALSE WHERE supplier_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, supplierId);
            return ps.executeUpdate() > 0;
        }
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null && !value.isBlank()) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }

    private static SupplierResponse mapRow(ResultSet rs) throws SQLException {
        int leadTime = rs.getInt("supplier_default_lead_time_days");
        Integer leadTimeObj = rs.wasNull() ? null : leadTime;
        return new SupplierResponse(
                rs.getLong("supplier_id"),
                rs.getString("supplier_name"),
                rs.getString("supplier_contact_email"),
                rs.getString("supplier_contact_phone"),
                leadTimeObj,
                rs.getBoolean("supplier_is_active"),
                rs.getTimestamp("date_created"),
                rs.getTimestamp("date_modified")
        );
    }
}