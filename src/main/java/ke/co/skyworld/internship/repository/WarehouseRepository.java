package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.warehouse.WarehouseResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WarehouseRepository {

    public boolean codeExists(String code) throws SQLException {
        String sql = "SELECT 1 FROM warehouses WHERE warehouse_code = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long create(String code, String name, String address) throws SQLException {
        String sql = """
                INSERT INTO warehouses (warehouse_code, warehouse_name, warehouse_address)
                VALUES (?, ?, ?)
                RETURNING warehouse_id
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setString(2, name);
            setNullableString(ps, 3, address);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("warehouse_id");
            }
        }
    }

    public Optional<WarehouseResponse> findById(long warehouseId) throws SQLException {
        String sql = "SELECT * FROM warehouses WHERE warehouse_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public PageResult<WarehouseResponse> list(int page, int pageSize, boolean includeInactive) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        String whereClause = includeInactive ? "" : "WHERE warehouse_is_active = TRUE";
        String countSql = "SELECT COUNT(*) FROM warehouses " + whereClause;
        String listSql = "SELECT * FROM warehouses " + whereClause + " ORDER BY warehouse_id LIMIT ? OFFSET ?";

        List<WarehouseResponse> items = new ArrayList<>();
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

    public boolean update(long warehouseId, String name, String address) throws SQLException {
        String sql = "UPDATE warehouses SET warehouse_name = ?, warehouse_address = ? WHERE warehouse_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            setNullableString(ps, 2, address);
            ps.setLong(3, warehouseId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deactivate(long warehouseId) throws SQLException {
        String sql = "UPDATE warehouses SET warehouse_is_active = FALSE WHERE warehouse_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
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

    private static WarehouseResponse mapRow(ResultSet rs) throws SQLException {
        return new WarehouseResponse(
                rs.getLong("warehouse_id"),
                rs.getString("warehouse_code"),
                rs.getString("warehouse_name"),
                rs.getString("warehouse_address"),
                rs.getBoolean("warehouse_is_active"),
                rs.getTimestamp("date_created"),
                rs.getTimestamp("date_modified")
        );
    }
}