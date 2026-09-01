package ke.co.skyworld.internship.repository;

import ke.co.skyworld.internship.domain.beans.shipment.ShipmentResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ShipmentRepository {

    public static class InvalidStateException extends RuntimeException {
        public InvalidStateException(String message) {
            super(message);
        }
    }

    public long create(long warehouseId, String carrier, String trackingNumber) throws SQLException {
        String sql = """
                INSERT INTO shipments (warehouse_id, shipment_carrier, shipment_tracking_number, shipment_manifested_at)
                VALUES (?, ?, ?, NOW())
                RETURNING shipment_id
                """;
        try (Connection conn = ConnectionPool.getDataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
            setNullableString(ps, 2, carrier);
            setNullableString(ps, 3, trackingNumber);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("shipment_id");
            }
        }
    }

    /**
     * Only accepts packages already fully 'packed' - a package still being
     * assembled can't be handed to a shipment.
     */
    public void attachPackage(long shipmentId, long packageId) throws SQLException {
        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                String packageStatus;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT package_status FROM packages WHERE package_id = ? FOR UPDATE")) {
                    ps.setLong(1, packageId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InvalidStateException("Package not found: " + packageId);
                        packageStatus = rs.getString("package_status");
                    }
                }
                if (!"packed".equals(packageStatus)) {
                    throw new InvalidStateException("Package must be 'packed' before it can be attached to a shipment");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO shipment_packages (shipment_id, package_id) VALUES (?, ?)")) {
                    ps.setLong(1, shipmentId);
                    ps.setLong(2, packageId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Batch operation across every package attached to this shipment, which
     * may span multiple orders (a shipment isn't restricted to one order -
     * see the design note this feature started with). For each package_line
     * touched: writes an audit-only SHIP ledger entry (does NOT further
     * decrement available_stock() - PICK already did that), marks the
     * order_line 'shipped', and marks the package 'shipped'. Then rolls up
     * order-level status per DISTINCT order affected: 'shipped' if every
     * line on that order is now shipped, 'partially_shipped' otherwise.
     * <p>
     * Known simplification: an order_line is marked 'shipped' the moment it
     * appears in ANY dispatched package, without checking whether its full
     * picked quantity was covered by packages across potentially multiple
     * separate shipments over time. The packing-time conservation check
     * (PackageRepository.addLine) prevents over-packing, but doesn't
     * guarantee a line's full allocation ships in a single dispatch event.
     * Flagged rather than silently assumed correct.
     */
    public void dispatch(long shipmentId, String dispatchedBy) throws SQLException {
        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                long warehouseId;
                String status;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT warehouse_id, shipment_status FROM shipments WHERE shipment_id = ? FOR UPDATE")) {
                    ps.setLong(1, shipmentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InvalidStateException("Shipment not found: " + shipmentId);
                        warehouseId = rs.getLong("warehouse_id");
                        status = rs.getString("shipment_status");
                    }
                }
                if (!"manifested".equals(status)) {
                    throw new InvalidStateException("Shipment is not in 'manifested' status - cannot dispatch");
                }

                record LineToShip(long packageLineId, long orderLineId, long orderId, long productId, int quantity) {
                }

                List<LineToShip> linesToShip = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT pl.package_line_id, pl.order_line_id, ol.order_id, ol.product_id, pl.package_line_quantity
                        FROM shipment_packages sp
                        JOIN package_lines pl ON pl.package_id = sp.package_id
                        JOIN order_lines ol ON ol.order_line_id = pl.order_line_id
                        WHERE sp.shipment_id = ?
                        """)) {
                    ps.setLong(1, shipmentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            linesToShip.add(new LineToShip(
                                    rs.getLong("package_line_id"), rs.getLong("order_line_id"), rs.getLong("order_id"),
                                    rs.getLong("product_id"), rs.getInt("package_line_quantity")));
                        }
                    }
                }

                if (linesToShip.isEmpty()) {
                    throw new InvalidStateException("No packages are attached to this shipment - nothing to dispatch");
                }

                Set<Long> orderLinesTouched = new HashSet<>();
                Set<Long> ordersTouched = new HashSet<>();

                for (LineToShip line : linesToShip) {
                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO stock_movements
                                (product_id, warehouse_id, stock_movement_type, stock_movement_quantity_delta,
                                 stock_movement_reference_table, stock_movement_reference_id, stock_movement_created_by)
                            VALUES (?, ?, 'SHIP', ?, 'package_lines', ?, ?)
                            """)) {
                        ps.setLong(1, line.productId());
                        ps.setLong(2, warehouseId);
                        ps.setInt(3, -line.quantity());
                        ps.setLong(4, line.packageLineId());
                        ps.setString(5, dispatchedBy);
                        ps.executeUpdate();
                    }
                    orderLinesTouched.add(line.orderLineId());
                    ordersTouched.add(line.orderId());
                }

                for (Long orderLineId : orderLinesTouched) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE order_lines SET order_line_status = 'shipped' WHERE order_line_id = ?")) {
                        ps.setLong(1, orderLineId);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE packages SET package_status = 'shipped'
                        WHERE package_id IN (SELECT package_id FROM shipment_packages WHERE shipment_id = ?)
                        """)) {
                    ps.setLong(1, shipmentId);
                    ps.executeUpdate();
                }

                for (Long orderId : ordersTouched) {
                    rollUpOrderStatus(conn, orderId);
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE shipments SET shipment_status = 'dispatched', shipment_dispatched_at = NOW() WHERE shipment_id = ?")) {
                    ps.setLong(1, shipmentId);
                    ps.executeUpdate();
                }

                conn.commit();
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void rollUpOrderStatus(Connection conn, long orderId) throws SQLException {
        boolean allShipped;
        boolean anyShipped;
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT bool_and(order_line_status = 'shipped') AS all_shipped,
                       bool_or(order_line_status = 'shipped') AS any_shipped
                FROM order_lines WHERE order_id = ?
                """)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                allShipped = rs.getBoolean("all_shipped");
                anyShipped = rs.getBoolean("any_shipped");
            }
        }
        String newStatus = allShipped ? "shipped" : anyShipped ? "partially_shipped" : null;
        if (newStatus != null) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE orders SET order_status = ?::order_status WHERE order_id = ? AND order_status != 'cancelled'")) {
                ps.setString(1, newStatus);
                ps.setLong(2, orderId);
                ps.executeUpdate();
            }
        }
    }

    public Optional<ShipmentResponse> findByIdWithPackages(long shipmentId) throws SQLException {
        String headerSql = "SELECT * FROM shipments WHERE shipment_id = ?";
        String packagesSql = "SELECT package_id FROM shipment_packages WHERE shipment_id = ? ORDER BY package_id";

        try (Connection conn = ConnectionPool.getDataSource().getConnection()) {
            long warehouseId;
            String carrier, trackingNumber, status;
            java.sql.Timestamp manifestedAt, dispatchedAt;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, shipmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    warehouseId = rs.getLong("warehouse_id");
                    carrier = rs.getString("shipment_carrier");
                    trackingNumber = rs.getString("shipment_tracking_number");
                    status = rs.getString("shipment_status");
                    manifestedAt = rs.getTimestamp("shipment_manifested_at");
                    dispatchedAt = rs.getTimestamp("shipment_dispatched_at");
                }
            }

            List<Long> packageIds = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(packagesSql)) {
                ps.setLong(1, shipmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) packageIds.add(rs.getLong("package_id"));
                }
            }

            return Optional.of(new ShipmentResponse(shipmentId, warehouseId, carrier, trackingNumber, status,
                    manifestedAt, dispatchedAt, packageIds));
        }
    }

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null && !value.isBlank()) ps.setString(index, value);
        else ps.setNull(index, Types.VARCHAR);
    }
}
