package ke.co.skyworld.internship.repository;

import ke.co.skyworld.internship.util.logging.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Rule 3's restock side. Called from within PutawayRepository.confirmTask()'s
 * transaction, right after the PUTAWAY ledger row is written - that's the
 * exact moment new stock actually becomes available, so it's the correct
 * trigger for sweeping waiting backorders.
 * <p>
 * Policy: strict FIFO by order date. Each waiting backorder is filled with
 * as much as is currently available before moving to the next - including
 * PARTIAL fills, matching the brief's own acceptance test 2 example
 * (A=5 filled, B=3/5 filled, C=0) rather than an all-or-nothing "skip if
 * can't fill completely" rule.
 * <p>
 * Operates on the CALLER's connection/transaction rather than opening its
 * own - unlike every other repository in this project - because it must
 * run atomically with the putaway confirmation that triggered it, and
 * because reserve_stock()'s advisory lock is scoped to whatever
 * transaction it's called within.
 */
public class BackorderAllocator {

    private static final Duration RESERVATION_TTL = Duration.ofMinutes(15); // matches OrderRepository's default

    public static void allocate(Connection conn, long productId, long warehouseId, String allocatedBy) throws SQLException {
        List<long[]> waitingBackorders = new ArrayList<>(); // [backorder_id, order_line_id, quantity]

        // Row-locked so two concurrent putaway confirmations for the same
        // product+warehouse can't both sweep and double-allocate the same
        // backorders.
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT b.backorder_id, b.order_line_id, b.backorder_quantity
                FROM backorders b
                JOIN order_lines ol ON ol.order_line_id = b.order_line_id
                JOIN orders o ON o.order_id = ol.order_id
                JOIN customers c ON c.customer_id = o.customer_id
                WHERE b.backorder_status = 'waiting'
                  AND ol.product_id = ?
                  AND c.customer_default_warehouse_id = ?
                ORDER BY o.order_placed_at ASC, b.backorder_id ASC
                FOR UPDATE OF b
                """)) {
            ps.setLong(1, productId);
            ps.setLong(2, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    waitingBackorders.add(new long[]{
                            rs.getLong("backorder_id"), rs.getLong("order_line_id"), rs.getLong("backorder_quantity")
                    });
                }
            }
        }

        if (waitingBackorders.isEmpty()) {
            return;
        }

        int totalAllocated = 0;

        for (long[] row : waitingBackorders) {
            long backorderId = row[0];
            long orderLineId = row[1];
            long backorderQuantity = row[2];

            int atp = currentAtp(conn, productId, warehouseId);
            if (atp <= 0) {
                break; // nothing left this round - remaining backorders stay waiting
            }

            int allocateQty = (int) Math.min(backorderQuantity, atp);
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + RESERVATION_TTL.toMillis());

            Long reservationId;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT reserve_stock(?, ?, ?, ?, ?, ?) AS reservation_id")) {
                ps.setLong(1, productId);
                ps.setLong(2, warehouseId);
                ps.setLong(3, orderLineId);
                ps.setInt(4, allocateQty);
                ps.setTimestamp(5, expiresAt);
                ps.setString(6, allocatedBy);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    long id = rs.getLong("reservation_id");
                    reservationId = rs.wasNull() ? null : id;
                }
            }

            if (reservationId == null) {
                // ATP changed underneath us between the read above and this
                // call (another concurrent path consumed it) - stop this
                // sweep rather than retry-loop; the next putaway
                // confirmation will pick up where this left off.
                Log.warning(BackorderAllocator.class, "allocate",
                        "reserve_stock returned null mid-sweep for backorder " + backorderId + " - stopping sweep early");
                break;
            }

            boolean fullyFilled = allocateQty == backorderQuantity;

            if (fullyFilled) {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE backorders SET backorder_status = 'fulfilled' WHERE backorder_id = ?")) {
                    ps.setLong(1, backorderId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE order_lines
                        SET order_line_quantity_allocated = order_line_quantity_allocated + ?,
                            order_line_status = 'reserved'
                        WHERE order_line_id = ?
                        """)) {
                    ps.setInt(1, allocateQty);
                    ps.setLong(2, orderLineId);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE backorders SET backorder_quantity = backorder_quantity - ?,
                               backorder_status = 'partially_allocated'
                        WHERE backorder_id = ?
                        """)) {
                    ps.setInt(1, allocateQty);
                    ps.setLong(2, backorderId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("""
                        UPDATE order_lines SET order_line_quantity_allocated = order_line_quantity_allocated + ?
                        WHERE order_line_id = ?
                        """)) {
                    ps.setInt(1, allocateQty);
                    ps.setLong(2, orderLineId);
                    ps.executeUpdate();
                    // order_line_status intentionally left as 'backordered' -
                    // it's partially reserved, not fully, and the status
                    // enum has no dedicated "partially reserved" value.
                    // order_line_quantity_allocated is the source of truth
                    // for how much has actually been secured so far.
                }
            }

            totalAllocated += allocateQty;
        }

        if (totalAllocated > 0) {
            Log.info(BackorderAllocator.class, "allocate",
                    "Allocated " + totalAllocated + " units of product " + productId + " in warehouse "
                            + warehouseId + " to waiting backorders");
        }
    }

    private static int currentAtp(Connection conn, long productId, long warehouseId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("""
                SELECT available_stock(?, ?) - COALESCE((
                    SELECT SUM(stock_reservation_quantity) FROM stock_reservations
                    WHERE product_id = ? AND warehouse_id = ? AND stock_reservation_status = 'active'
                ), 0) AS atp
                """)) {
            ps.setLong(1, productId);
            ps.setLong(2, warehouseId);
            ps.setLong(3, productId);
            ps.setLong(4, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("atp");
            }
        }
    }
}
