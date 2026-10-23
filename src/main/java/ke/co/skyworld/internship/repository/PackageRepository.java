package ke.co.skyworld.internship.repository;

import ke.co.skyworld.internship.domain.beans.packages.PackageResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PackageRepository {

    public static class InvalidStateException extends RuntimeException {
        public InvalidStateException(String message) {
            super(message);
        }
    }

    public long create(long orderId) throws SQLException {
        String sql = "INSERT INTO packages (order_id, package_status) VALUES (?, 'packing') RETURNING package_id";
        try (Connection conn = ConnectionPool.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("package_id");
            }
        }
    }

    /**
     * Locks the order_line row to compute "how much of this line is already
     * packed across ALL packages" and validate the new quantity doesn't
     * push the total past what was actually picked
     * (order_line_quantity_allocated) - same conservation pattern as LPN
     * splitting and license plate creation earlier in this project.
     * Also verifies the order line actually belongs to this package's order.
     */
    public long addLine(long packageId, long orderLineId, int quantity) throws SQLException {
        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                long packageOrderId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT order_id, package_status FROM packages WHERE package_id = ? FOR UPDATE")) {
                    ps.setLong(1, packageId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InvalidStateException("Package not found: " + packageId);
                        if (!"packing".equals(rs.getString("package_status"))) {
                            throw new InvalidStateException("Package is not in 'packing' status - cannot add lines");
                        }
                        packageOrderId = rs.getLong("order_id");
                    }
                }

                long orderLineOrderId;
                int allocated;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT order_id, order_line_quantity_allocated FROM order_lines WHERE order_line_id = ? FOR UPDATE")) {
                    ps.setLong(1, orderLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InvalidStateException("Order line not found: " + orderLineId);
                        orderLineOrderId = rs.getLong("order_id");
                        allocated = rs.getInt("order_line_quantity_allocated");
                    }
                }

                if (orderLineOrderId != packageOrderId) {
                    throw new InvalidStateException("This order line does not belong to this package's order");
                }

                int alreadyPacked;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT COALESCE(SUM(package_line_quantity), 0) AS total FROM package_lines WHERE order_line_id = ?")) {
                    ps.setLong(1, orderLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        alreadyPacked = rs.getInt("total");
                    }
                }

                if (alreadyPacked + quantity > allocated) {
                    throw new InvalidStateException(
                            "Packing " + quantity + " more would exceed what was actually picked for this line ("
                                    + allocated + " allocated, " + alreadyPacked + " already packed)");
                }

                long lineId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO package_lines (package_id, order_line_id, package_line_quantity)
                        VALUES (?, ?, ?)
                        RETURNING package_line_id
                        """)) {
                    ps.setLong(1, packageId);
                    ps.setLong(2, orderLineId);
                    ps.setInt(3, quantity);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        lineId = rs.getLong("package_line_id");
                    }
                }

                conn.commit();
                return lineId;
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean confirmPack(long packageId, BigDecimal weight) throws SQLException {
        String sql = "UPDATE packages SET package_weight = ?, package_status = 'packed' " +
                "WHERE package_id = ? AND package_status = 'packing'";
        try (Connection conn = ConnectionPool.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (weight != null) ps.setBigDecimal(1, weight); else ps.setNull(1, Types.NUMERIC);
            ps.setLong(2, packageId);
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<PackageResponse> findByIdWithLines(long packageId) throws SQLException {
        String headerSql = "SELECT * FROM packages WHERE package_id = ?";
        String linesSql = "SELECT * FROM package_lines WHERE package_id = ? ORDER BY package_line_id";

        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            long orderId;
            BigDecimal weight;
            String status;
            java.sql.Timestamp created;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, packageId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    orderId = rs.getLong("order_id");
                    weight = rs.getBigDecimal("package_weight");
                    status = rs.getString("package_status");
                    created = rs.getTimestamp("date_created");
                }
            }

            List<PackageResponse.Line> lines = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(linesSql)) {
                ps.setLong(1, packageId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lines.add(new PackageResponse.Line(rs.getLong("package_line_id"),
                                rs.getLong("order_line_id"), rs.getInt("package_line_quantity")));
                    }
                }
            }

            return Optional.of(new PackageResponse(packageId, orderId, weight, status, lines, created));
        }
    }
}
