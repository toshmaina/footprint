package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.util.Optional;

public class StockReportRepository {

    /**
     * Self-contained SKU/warehouse lookups rather than depending on
     * ProductRepository/WarehouseRepository directly - avoids coupling to
     * their current shape while the project is mid-repackage.
     */
    public Optional<ProductLookup> findProductIdBySku(String sku) throws SQLException {
        String sql = "SELECT product_id, product_sku FROM products WHERE product_sku = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new ProductLookup(rs.getLong("product_id"), rs.getString("product_sku")));
            }
        }
    }

    public boolean warehouseExists(long warehouseId) throws SQLException {
        String sql = "SELECT 1 FROM warehouses WHERE warehouse_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Calls the available_stock() SQL function directly (built back in the
     * outbound-phase concurrency work) - one definition of "available,"
     * used by both this report and reserve_stock(), rather than
     * reimplementing the movement-type filtering logic here in Java.
     * A timestamp before any movements exist returns 0 via the function's
     * own COALESCE(SUM(...), 0), not an error - matches acceptance test 6.
     */
    public int getAvailableStock(long productId, long warehouseId, Timestamp asOf) throws SQLException {
        String sql = "SELECT available_stock(?, ?, ?) AS available";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, productId);
            ps.setLong(2, warehouseId);
            ps.setTimestamp(3, asOf);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("available");
            }
        }
    }

    public record ProductLookup(long productId, String sku) {
    }
}