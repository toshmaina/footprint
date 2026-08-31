package ke.co.skyworld.internship.repository;

import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;

/**
 * Minimal for now - what AuthMiddleware, login, and registration need.
 * Full CRUD for user account management is a separate, later piece of work,
 * same as products/warehouses/suppliers.
 */
public class UserAccountRepository {

    /**
     * Returns null if the user_account_id doesn't exist (e.g. the row was
     * deleted after the token was issued) - callers should treat that the
     * same as an invalid token, not throw a 500.
     */
    public AuthSnapshot findAuthSnapshot(long userAccountId) throws SQLException {
        String sql = """
                SELECT user_account_default_warehouse_id, user_account_status
                FROM user_accounts
                WHERE user_account_id = ?
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                long warehouseId = rs.getLong("user_account_default_warehouse_id");
                Long defaultWarehouseId = rs.wasNull() ? null : warehouseId;
                return new AuthSnapshot(defaultWarehouseId, rs.getString("user_account_status"));
            }
        }
    }

    /**
     * Returns null if no account exists for that username - the login
     * handler intentionally responds with the same generic "invalid
     * credentials" message whether the username doesn't exist or the
     * password was wrong, so as not to leak which usernames are registered.
     */
    public LoginCredentials findLoginCredentials(String username) throws SQLException {
        String sql = """
                SELECT user_account_id, user_account_password_hash, user_account_status
                FROM user_accounts
                WHERE user_account_username = ?
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new LoginCredentials(
                        rs.getLong("user_account_id"),
                        rs.getString("user_account_password_hash"),
                        rs.getString("user_account_status")
                );
            }
        }
    }

    /**
     * Checks if a user already exists with the given username or email.
     * Used by the registration handler to fail fast before attempting an insert.
     */
    public boolean userExists(String username, String email) throws SQLException {
        String sql = """
                SELECT 1
                FROM user_accounts
                WHERE user_account_username = ? OR (user_account_email = ? AND user_account_email IS NOT NULL)
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            if (email != null && !email.isBlank()) {
                ps.setString(2, email);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Creates a new user account and returns the generated user_account_id.
     * Takes advantage of PostgreSQL's RETURNING clause for efficiency.
     */
    public long createUser(String username, String email, String passwordHash, String fullName, Long warehouseId) throws SQLException {
        String sql = """
                INSERT INTO user_accounts (
                    user_account_username, 
                    user_account_email, 
                    user_account_password_hash, 
                    user_account_full_name, 
                    user_account_default_warehouse_id
                ) VALUES (?, ?, ?, ?, ?)
                RETURNING user_account_id
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);

            if (email != null && !email.isBlank()) {
                ps.setString(2, email);
            } else {
                ps.setNull(2, Types.VARCHAR);
            }

            ps.setString(3, passwordHash);
            ps.setString(4, fullName);

            if (warehouseId != null) {
                ps.setLong(5, warehouseId);
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("user_account_id");
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    public record AuthSnapshot(Long defaultWarehouseId, String status) {
    }

    public record LoginCredentials(long userAccountId, String passwordHash, String status) {
    }
}