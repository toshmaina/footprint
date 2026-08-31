package ke.co.skyworld.internship.repository;

import ke.co.skyworld.internship.domain.beans.ProductResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepository {

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null && !value.isBlank()) {
            ps.setString(index, value);
        } else {
            ps.setNull(index, Types.VARCHAR);
        }
    }

    private static ProductResponse mapRow(ResultSet rs) throws SQLException {
        return new ProductResponse(
                rs.getLong("product_id"),
                rs.getString("product_sku"),
                rs.getString("product_name"),
                rs.getString("product_category"),
                rs.getInt("product_reorder_threshold"),
                rs.getString("product_classification"),
                rs.getBoolean("product_is_active"),
                rs.getTimestamp("date_created"),
                rs.getTimestamp("date_modified")
        );
    }

    public boolean skuExists(String sku) throws SQLException {
        String sql = "SELECT 1 FROM products WHERE product_sku = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public long create(String sku, String name, String category, Integer reorderThreshold,
                       String classification) throws SQLException {
        String sql = """
                INSERT INTO products (product_sku, product_name, product_category,
                                       product_reorder_threshold, product_classification)
                VALUES (?, ?, ?, ?, ?::product_classification)
                RETURNING product_id
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            ps.setString(2, name);
            setNullableString(ps, 3, category);
            ps.setInt(4, reorderThreshold != null ? reorderThreshold : 0);
            setNullableString(ps, 5, classification);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("product_id");
            }
        }
    }

    public Optional<ProductResponse> findById(long productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public PageResult<ProductResponse> list(int page, int pageSize, boolean includeInactive) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        String whereClause = includeInactive ? "" : "WHERE product_is_active = TRUE";

        String countSql = "SELECT COUNT(*) FROM products " + whereClause;
        String listSql = "SELECT * FROM products " + whereClause
                + " ORDER BY product_id LIMIT ? OFFSET ?";

        List<ProductResponse> items = new ArrayList<>();
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
                        items.add(mapRow(rs));
                    }
                }
            }
        }

        return new PageResult<>(items, total);
    }

    /**
     * Returns false if no row with that id existed to update - caller should
     * respond 404, not silently succeed.
     */
    public boolean update(long productId, String name, String category, Integer reorderThreshold,
                          String classification) throws SQLException {
        String sql = """
                UPDATE products
                SET product_name = ?, product_category = ?, product_reorder_threshold = ?,
                    product_classification = ?::product_classification
                WHERE product_id = ?
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            setNullableString(ps, 2, category);
            ps.setInt(3, reorderThreshold != null ? reorderThreshold : 0);
            setNullableString(ps, 4, classification);
            ps.setLong(5, productId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Soft-delete - never a hard DELETE, see migration 06's header comment.
     * Returns false if no row with that id existed.
     */
    public boolean deactivate(long productId) throws SQLException {
        String sql = "UPDATE products SET product_is_active = FALSE WHERE product_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            return ps.executeUpdate() > 0;
        }
    }
}