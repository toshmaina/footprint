package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.storagelocation.StorageLocationResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StorageLocationRepository {

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null && !value.isBlank()) ps.setString(index, value);
        else ps.setNull(index, Types.VARCHAR);
    }

    private static StorageLocationResponse mapRow(ResultSet rs) throws SQLException {
        return new StorageLocationResponse(
                rs.getLong("storage_location_id"), rs.getLong("warehouse_id"),
                rs.getString("storage_location_zone"), rs.getString("storage_location_aisle"),
                rs.getString("storage_location_rack"), rs.getString("storage_location_bin_code"),
                rs.getString("storage_location_type"), rs.getBoolean("storage_location_is_active"),
                rs.getTimestamp("date_created"), rs.getTimestamp("date_modified"));
    }

    public boolean binCodeExists(long warehouseId, String binCode) throws SQLException {
        String sql = "SELECT 1 FROM storage_locations WHERE warehouse_id = ? AND storage_location_bin_code = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
            ps.setString(2, binCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long create(long warehouseId, String zone, String aisle, String rack, String binCode, String locationType)
            throws SQLException {
        String sql = """
                INSERT INTO storage_locations
                    (warehouse_id, storage_location_zone, storage_location_aisle, storage_location_rack,
                     storage_location_bin_code, storage_location_type)
                VALUES (?, ?, ?, ?, ?, ?::storage_location_type)
                RETURNING storage_location_id
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
            setNullableString(ps, 2, zone);
            setNullableString(ps, 3, aisle);
            setNullableString(ps, 4, rack);
            ps.setString(5, binCode);
            ps.setString(6, locationType);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("storage_location_id");
            }
        }
    }

    public Optional<StorageLocationResponse> findById(long storageLocationId) throws SQLException {
        String sql = "SELECT * FROM storage_locations WHERE storage_location_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, storageLocationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public PageResult<StorageLocationResponse> list(int page, int pageSize, Long warehouseId, boolean includeInactive)
            throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        StringBuilder where = new StringBuilder();
        if (!includeInactive) where.append("WHERE storage_location_is_active = TRUE");
        if (warehouseId != null) {
            where.append(where.isEmpty() ? "WHERE " : " AND ").append("warehouse_id = ").append(warehouseId);
        }
        String countSql = "SELECT COUNT(*) FROM storage_locations " + where;
        String listSql = "SELECT * FROM storage_locations " + where + " ORDER BY storage_location_id LIMIT ? OFFSET ?";

        List<StorageLocationResponse> items = new ArrayList<>();
        long total;
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            try (PreparedStatement ps = conn.prepareStatement(countSql); ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(listSql)) {
                ps.setInt(1, pageSize);
                ps.setInt(2, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(mapRow(rs));
                }
            }
        }
        return new PageResult<>(items, total);
    }

    public boolean update(long storageLocationId, String zone, String aisle, String rack, String locationType)
            throws SQLException {
        String sql = """
                UPDATE storage_locations
                SET storage_location_zone = ?, storage_location_aisle = ?, storage_location_rack = ?,
                    storage_location_type = ?::storage_location_type
                WHERE storage_location_id = ?
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setNullableString(ps, 1, zone);
            setNullableString(ps, 2, aisle);
            setNullableString(ps, 3, rack);
            ps.setString(4, locationType);
            ps.setLong(5, storageLocationId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deactivate(long storageLocationId) throws SQLException {
        String sql = "UPDATE storage_locations SET storage_location_is_active = FALSE WHERE storage_location_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, storageLocationId);
            return ps.executeUpdate() > 0;
        }
    }
}