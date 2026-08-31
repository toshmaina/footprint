package ke.co.skyworld.internship.repository;

import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DiscrepancyRepository {

    /**
     * Only allowed from 'open' or 'under_review' - guards against
     * re-resolving something already closed out.
     */
    public boolean resolve(long discrepancyId, String status, String resolvedBy, String notes) throws SQLException {
        String sql = """
                UPDATE goods_receipt_discrepancies
                SET goods_receipt_discrepancy_status = ?::goods_receipt_discrepancy_status,
                    goods_receipt_discrepancy_resolved_by = ?,
                    goods_receipt_discrepancy_resolved_at = NOW(),
                    goods_receipt_discrepancy_notes = COALESCE(?, goods_receipt_discrepancy_notes)
                WHERE goods_receipt_discrepancy_id = ?
                  AND goods_receipt_discrepancy_status IN ('open', 'under_review')
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, resolvedBy);
            if (notes != null && !notes.isBlank()) ps.setString(3, notes);
            else ps.setNull(3, Types.VARCHAR);
            ps.setLong(4, discrepancyId);
            return ps.executeUpdate() > 0;
        }
    }
}
