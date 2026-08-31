package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.customer.CustomerResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository {

    private static void setNullableString(PreparedStatement ps, int index, String value) throws SQLException {
        if (value != null && !value.isBlank()) ps.setString(index, value);
        else ps.setNull(index, Types.VARCHAR);
    }

    private static CustomerResponse mapRow(ResultSet rs) throws SQLException {
        return new CustomerResponse(
                rs.getLong("customer_id"), rs.getString("customer_name"), rs.getLong("customer_default_warehouse_id"),
                rs.getString("customer_contact_email"), rs.getString("customer_contact_phone"),
                rs.getTimestamp("date_created"), rs.getTimestamp("date_modified"));
    }

    public long create(String name, long defaultWarehouseId, String contactEmail, String contactPhone) throws SQLException {
        String sql = """
                INSERT INTO customers (customer_name, customer_default_warehouse_id, customer_contact_email, customer_contact_phone)
                VALUES (?, ?, ?, ?)
                RETURNING customer_id
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setLong(2, defaultWarehouseId);
            setNullableString(ps, 3, contactEmail);
            setNullableString(ps, 4, contactPhone);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("customer_id");
            }
        }
    }

    public Optional<CustomerResponse> findById(long customerId) throws SQLException {
        String sql = "SELECT * FROM customers WHERE customer_id = ?";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }

    public PageResult<CustomerResponse> list(int page, int pageSize) throws SQLException {
        int offset = Math.max(0, (page - 1) * pageSize);
        List<CustomerResponse> items = new ArrayList<>();
        long total;
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM customers");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                total = rs.getLong(1);
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM customers ORDER BY customer_id LIMIT ? OFFSET ?")) {
                ps.setInt(1, pageSize);
                ps.setInt(2, offset);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) items.add(mapRow(rs));
                }
            }
        }
        return new PageResult<>(items, total);
    }
}