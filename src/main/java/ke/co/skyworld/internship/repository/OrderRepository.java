package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.order.OrderRequest;
import ke.co.skyworld.internship.domain.beans.order.OrderResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepository {

    private static final Duration RESERVATION_TTL = Duration.ofMinutes(15); // rule 2's default, matches what was

    /**
     * Rule 1 (warehouse allocation) is implemented as "hold": every line is
     * attempted only against the customer's customer_default_warehouse_id.
     * No cross-warehouse fallback - a shortfall goes straight to backorder
     * rather than the system hunting other warehouses.
     * <p>
     * Order header, every line, every reservation attempt (via the
     * concurrency-safe reserve_stock() function), and every resulting
     * backorder all happen in ONE transaction - either the whole order is
     * recorded consistently or none of it is. reserve_stock()'s advisory
     * lock is scoped to this same transaction, so it's held only as long
     * as this order-creation call takes, then released at commit.
     */
    public OrderResponse createWithLines(long customerId, List<OrderRequest.Line> lines, String requestedBy)
            throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long warehouseId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT customer_default_warehouse_id FROM customers WHERE customer_id = ?")) {
                    ps.setLong(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new CustomerNotFoundException("Customer not found: " + customerId);
                        }
                        warehouseId = rs.getLong("customer_default_warehouse_id");
                    }
                }

                long orderId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO orders (customer_id, order_status) VALUES (?, 'pending_allocation') RETURNING order_id")) {
                    ps.setLong(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        orderId = rs.getLong("order_id");
                    }
                }

                List<OrderResponse.Line> lineResults = new ArrayList<>();
                int reservedCount = 0;
                int backorderedCount = 0;

                for (OrderRequest.Line requestLine : lines) {
                    long orderLineId;
                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO order_lines (order_id, product_id, order_line_quantity_ordered, order_line_status)
                            VALUES (?, ?, ?, 'pending')
                            RETURNING order_line_id
                            """)) {
                        ps.setLong(1, orderId);
                        ps.setLong(2, requestLine.getProductId());
                        ps.setInt(3, requestLine.getQuantityOrdered());
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            orderLineId = rs.getLong("order_line_id");
                        }
                    }

                    Long reservationId;
                    Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + RESERVATION_TTL.toMillis());
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT reserve_stock(?, ?, ?, ?, ?, ?) AS reservation_id")) {
                        ps.setLong(1, requestLine.getProductId());
                        ps.setLong(2, warehouseId);
                        ps.setLong(3, orderLineId);
                        ps.setInt(4, requestLine.getQuantityOrdered());
                        ps.setTimestamp(5, expiresAt);
                        ps.setString(6, requestedBy);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            long id = rs.getLong("reservation_id");
                            reservationId = rs.wasNull() ? null : id;
                        }
                    }

                    Long backorderId = null;
                    String lineStatus;
                    int quantityAllocated;

                    if (reservationId != null) {
                        quantityAllocated = requestLine.getQuantityOrdered();
                        lineStatus = "reserved";
                        reservedCount++;
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE order_lines SET order_line_quantity_allocated = ?, order_line_status = 'reserved' WHERE order_line_id = ?")) {
                            ps.setInt(1, quantityAllocated);
                            ps.setLong(2, orderLineId);
                            ps.executeUpdate();
                        }
                    } else {
                        quantityAllocated = 0;
                        lineStatus = "backordered";
                        backorderedCount++;
                        try (PreparedStatement ps = conn.prepareStatement("""
                                INSERT INTO backorders (order_line_id, backorder_quantity, backorder_status,
                                                         backorder_source_type)
                                VALUES (?, ?, 'waiting', 'allocation_shortfall')
                                RETURNING backorder_id
                                """)) {
                            ps.setLong(1, orderLineId);
                            ps.setInt(2, requestLine.getQuantityOrdered());
                            try (ResultSet rs = ps.executeQuery()) {
                                rs.next();
                                backorderId = rs.getLong("backorder_id");
                            }
                        }
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE order_lines SET order_line_status = 'backordered' WHERE order_line_id = ?")) {
                            ps.setLong(1, orderLineId);
                            ps.executeUpdate();
                        }
                    }

                    lineResults.add(new OrderResponse.Line(orderLineId, requestLine.getProductId(),
                            requestLine.getQuantityOrdered(), quantityAllocated, lineStatus, reservationId, backorderId));
                }

                String orderStatus = backorderedCount == 0 ? "allocated"
                        : reservedCount == 0 ? "pending_allocation" : "partially_allocated";
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE orders SET order_status = ?::order_status WHERE order_id = ?")) {
                    ps.setString(1, orderStatus);
                    ps.setLong(2, orderId);
                    ps.executeUpdate();
                }

                conn.commit();
                return new OrderResponse(orderId, customerId, orderStatus, new java.util.Date(), lineResults);
            } catch (SQLException | CustomerNotFoundException e) {
                conn.rollback();
                throw e;
            }
        }
    }


    public Optional<OrderResponse> findByIdWithLines(long orderId) throws SQLException {
        String headerSql = "SELECT * FROM orders WHERE order_id = ?";
        String linesSql = """
                SELECT ol.*, sr.stock_reservation_id, b.backorder_id
                FROM order_lines ol
                LEFT JOIN stock_reservations sr ON sr.order_line_id = ol.order_line_id AND sr.stock_reservation_status = 'active'
                LEFT JOIN backorders b ON b.order_line_id = ol.order_line_id AND b.backorder_status = 'waiting'
                WHERE ol.order_id = ?
                ORDER BY ol.order_line_id
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            long customerId;
            String status;
            java.sql.Timestamp placedAt;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    customerId = rs.getLong("customer_id");
                    status = rs.getString("order_status");
                    placedAt = rs.getTimestamp("order_placed_at");
                }
            }

            List<OrderResponse.Line> lines = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(linesSql)) {
                ps.setLong(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long resId = rs.getLong("stock_reservation_id");
                        Long reservationId = rs.wasNull() ? null : resId;
                        long boId = rs.getLong("backorder_id");
                        Long backorderId = rs.wasNull() ? null : boId;
                        lines.add(new OrderResponse.Line(
                                rs.getLong("order_line_id"), rs.getLong("product_id"),
                                rs.getInt("order_line_quantity_ordered"), rs.getInt("order_line_quantity_allocated"),
                                rs.getString("order_line_status"), reservationId, backorderId));
                    }
                }
            }

            return Optional.of(new OrderResponse(orderId, customerId, status, placedAt, lines));
        }
    }

    public static class CustomerNotFoundException extends RuntimeException {
        public CustomerNotFoundException(String message) {
            super(message);
        }
    }
}
