package ke.co.skyworld.internship.repository;

import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class PermissionRepository {

    /**
     * Returns the effective permission set for a user - the union of every
     * permission granted by every role assigned to them. An empty set means
     * the user is authenticated but has no explicit grants (still valid,
     * just can't do anything gated behind hasPermission()).
     */
    public Set<String> listPermissionsForUser(long userAccountId) throws SQLException {
        String sql = """
                SELECT DISTINCT p.permission_key
                FROM permissions p
                JOIN role_permissions rp ON rp.permission_id = p.permission_id
                JOIN user_roles ur ON ur.role_id = rp.role_id
                WHERE ur.user_account_id = ?
                """;

        Set<String> permissions = new HashSet<>();

        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permissions.add(rs.getString("permission_key"));
                }
            }
        }

        return permissions;
    }
}