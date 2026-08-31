package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationExpiryRepository {

    /**
     * Benefits directly from idx_stock_reservations_status_expiry, built
     * back in the outbound-phase migration specifically for this query.
     */
    public List<Long> findExpiredActiveReservationIds() throws SQLException {
        String sql = """
                SELECT stock_reservation_id FROM stock_reservations
                WHERE stock_reservation_status = 'active' AND stock_reservation_expires_at <= NOW()
                """;
        List<Long> ids = new ArrayList<>();
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getLong("stock_reservation_id"));
            }
        }
        return ids;
    }

    /**
     * Calls the existing release_reservation() function - already
     * race-safe (advisory lock + guarded UPDATE ... WHERE status='active'),
     * so it's safe to call redundantly if this sweep ever somehow runs
     * concurrently across multiple app instances against the same
     * reservation - the second call is just a no-op, not a double-release.
     */
    public boolean releaseReservation(long reservationId, String releasedBy) throws SQLException {
        String sql = "SELECT release_reservation(?, ?) AS released";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reservationId);
            ps.setString(2, releasedBy);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBoolean("released");
            }
        }
    }
}