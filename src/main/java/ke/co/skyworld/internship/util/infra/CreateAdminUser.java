package ke.co.skyworld.internship.util.infra;

import ke.co.skyworld.internship.util.db.ConnectionPool;
import ke.co.skyworld.internship.util.security.Encryption;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * One-time bootstrap: creates the first user_account and grants it the
 * 'admin' role (which the identity schema seeds with system.full_access).
 * Run this once, directly, before the API is ever exposed - not wired to
 * any HTTP route. Requires the config decryption passphrase to already be
 * set in the environment, same as running the main API.
 * <p>
 * Usage: java -cp sky-inventory-api.jar com.skyworld.util.infra.CreateAdminUser <username> <email> <fullName> <password>
 */
public final class CreateAdminUser {

    private CreateAdminUser() {
    }

    public static void main(String[] args) throws SQLException {
        if (args.length != 4) {
            System.err.println("Usage: CreateAdminUser <username> <email> <fullName> <password>");
            System.exit(1);
        }

        String username = args[0] ;
        String email = args[1];
        String fullName = args[2];
        String password = args[3];

        ConnectionPool.initialize();

        String passwordHash = Encryption.hashPassword(password);

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long userAccountId = insertUserAccount(conn, username, email, fullName, passwordHash);
                assignAdminRole(conn, userAccountId);
                conn.commit();
                System.out.println("Created admin user '" + username + "' (user_account_id=" + userAccountId + ")");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } finally {
            ConnectionPool.shutdown();
        }
    }

    private static long insertUserAccount(Connection conn, String username, String email,
                                          String fullName, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO user_accounts
                    (user_account_username, user_account_email, user_account_full_name, user_account_password_hash)
                VALUES (?, ?, ?, ?)
                RETURNING user_account_id
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, fullName);
            ps.setString(4, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("user_account_id");
            }
        }
    }

    private static void assignAdminRole(Connection conn, long userAccountId) throws SQLException {
        String sql = """
                INSERT INTO user_roles (user_account_id, role_id)
                SELECT ?, role_id FROM roles WHERE role_name = 'admin'
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userAccountId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalStateException(
                        "No 'admin' role found - has 05_identity_and_access_schema.sql been run?");
            }
        }
    }
}