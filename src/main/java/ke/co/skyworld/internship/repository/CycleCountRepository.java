package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.count_cycles.CycleCountScheduleResponse;
import ke.co.skyworld.internship.domain.beans.count_cycles.CycleCountTaskResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class CycleCountRepository {

    public static class InvalidStateException extends RuntimeException {
        public InvalidStateException(String message) {
            super(message);
        }
    }

    // -------------------------------------------------------------
    // Schedules
    // -------------------------------------------------------------

    public long createSchedule(long warehouseId, String classification, int frequencyDays) throws SQLException {
        String sql = """
                INSERT INTO cycle_count_schedules (warehouse_id, cycle_count_schedule_product_classification, cycle_count_schedule_count_frequency_days)
                VALUES (?, ?::product_classification, ?)
                RETURNING cycle_count_schedule_id
                """;
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
            ps.setString(2, classification);
            ps.setInt(3, frequencyDays);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("cycle_count_schedule_id");
            }
        }
    }

    public List<CycleCountScheduleResponse> listSchedules(Long warehouseId) throws SQLException {
        String where = warehouseId != null ? "WHERE warehouse_id = ?" : "";
        String sql = "SELECT * FROM cycle_count_schedules " + where + " ORDER BY cycle_count_schedule_id";
        List<CycleCountScheduleResponse> items = new ArrayList<>();
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (warehouseId != null) ps.setLong(1, warehouseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CycleCountScheduleResponse(
                            rs.getLong("cycle_count_schedule_id"), rs.getLong("warehouse_id"),
                            rs.getString("cycle_count_schedule_product_classification"),
                            rs.getInt("cycle_count_schedule_count_frequency_days")));
                }
            }
        }
        return items;
    }

    // -------------------------------------------------------------
    // Tasks - creation auto-generates the frozen snapshot
    // -------------------------------------------------------------

    /**
     * Computes a LOCATION-scoped balance per product (not warehouse-scoped
     * like available_stock()) by summing the ledger for movement types that
     * carry storage_location_id, then freezes that as the snapshot at task
     * creation time - never recomputed later, per the design requirement
     * that you must be able to prove what the variance was measured against.
     */
    public long createTaskWithSnapshot(long warehouseId, long storageLocationId, java.util.Date scheduledDate,
                                       String assignedTo) throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long taskId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO cycle_count_tasks (warehouse_id, storage_location_id, cycle_count_task_scheduled_date, cycle_count_task_assigned_to)
                        VALUES (?, ?, ?, ?)
                        RETURNING cycle_count_task_id
                        """)) {
                    ps.setLong(1, warehouseId);
                    ps.setLong(2, storageLocationId);
                    if (scheduledDate != null) ps.setDate(3, new java.sql.Date(scheduledDate.getTime()));
                    else ps.setNull(3, Types.DATE);
                    if (assignedTo != null && !assignedTo.isBlank()) ps.setString(4, assignedTo);
                    else ps.setNull(4, Types.VARCHAR);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        taskId = rs.getLong("cycle_count_task_id");
                    }
                }

                // Location-scoped balance per product, snapshotted now.
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT product_id, SUM(stock_movement_quantity_delta) AS balance
                        FROM stock_movements
                        WHERE storage_location_id = ?
                          AND stock_movement_type IN ('PUTAWAY', 'PICK', 'TRANSFER_IN', 'TRANSFER_OUT',
                                                       'CYCLE_COUNT_ADJUSTMENT', 'SHRINKAGE_ADJUSTMENT', 'DAMAGE_ADJUSTMENT')
                        GROUP BY product_id
                        HAVING SUM(stock_movement_quantity_delta) != 0
                        """)) {
                    ps.setLong(1, storageLocationId);
                    try (ResultSet rs = ps.executeQuery();
                         PreparedStatement insertLine = conn.prepareStatement("""
                                 INSERT INTO cycle_count_task_lines (cycle_count_task_id, product_id, cycle_count_task_line_system_quantity_snapshot)
                                 VALUES (?, ?, ?)
                                 """)) {
                        while (rs.next()) {
                            insertLine.setLong(1, taskId);
                            insertLine.setLong(2, rs.getLong("product_id"));
                            insertLine.setInt(3, rs.getInt("balance"));
                            insertLine.addBatch();
                        }
                        insertLine.executeBatch();
                    }
                }

                conn.commit();
                return taskId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Optional<CycleCountTaskResponse> findTaskByIdWithLines(long taskId) throws SQLException {
        String headerSql = "SELECT * FROM cycle_count_tasks WHERE cycle_count_task_id = ?";
        // LATERAL join to the most recent result_line + its review decision (if any) per task line -
        // supports the recount case where a task_line can have multiple result_line attempts.
        String linesSql = """
                SELECT ctl.cycle_count_task_line_id, ctl.product_id, ctl.cycle_count_task_line_system_quantity_snapshot,
                       latest.cycle_count_result_line_id, latest.cycle_count_result_line_counted_quantity,
                       latest.cycle_count_result_line_variance_quantity, rev.cycle_count_variance_review_decision
                FROM cycle_count_task_lines ctl
                LEFT JOIN LATERAL (
                    SELECT * FROM cycle_count_result_lines crl
                    WHERE crl.cycle_count_task_line_id = ctl.cycle_count_task_line_id
                    ORDER BY crl.cycle_count_result_line_id DESC LIMIT 1
                ) latest ON true
                LEFT JOIN LATERAL (
                    SELECT * FROM cycle_count_variance_reviews cvr
                    WHERE cvr.cycle_count_result_line_id = latest.cycle_count_result_line_id
                    ORDER BY cvr.cycle_count_variance_review_id DESC LIMIT 1
                ) rev ON true
                WHERE ctl.cycle_count_task_id = ?
                ORDER BY ctl.cycle_count_task_line_id
                """;

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            long warehouseId, storageLocationId;
            Date scheduledDate;
            String assignedTo, status;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, taskId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    warehouseId = rs.getLong("warehouse_id");
                    storageLocationId = rs.getLong("storage_location_id");
                    scheduledDate = rs.getDate("cycle_count_task_scheduled_date");
                    assignedTo = rs.getString("cycle_count_task_assigned_to");
                    status = rs.getString("cycle_count_task_status");
                }
            }

            List<CycleCountTaskResponse.Line> lines = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(linesSql)) {
                ps.setLong(1, taskId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        long resultId = rs.getLong("cycle_count_result_line_id");
                        Long resultIdObj = rs.wasNull() ? null : resultId;
                        int counted = rs.getInt("cycle_count_result_line_counted_quantity");
                        Integer countedObj = rs.wasNull() ? null : counted;
                        int variance = rs.getInt("cycle_count_result_line_variance_quantity");
                        Integer varianceObj = rs.wasNull() ? null : variance;
                        lines.add(new CycleCountTaskResponse.Line(
                                rs.getLong("cycle_count_task_line_id"), rs.getLong("product_id"),
                                rs.getInt("cycle_count_task_line_system_quantity_snapshot"),
                                resultIdObj, countedObj, varianceObj,
                                rs.getString("cycle_count_variance_review_decision")));
                    }
                }
            }

            return Optional.of(new CycleCountTaskResponse(taskId, warehouseId, storageLocationId, scheduledDate,
                    assignedTo, status, lines));
        }
    }

    // -------------------------------------------------------------
    // Physical count submission
    // -------------------------------------------------------------

    /**
     * Always inserts a NEW result_line rather than updating one in place -
     * this is what makes recounts work naturally (a 'recount_required'
     * review just means the next submission for this task_line becomes
     * the new "latest" one that findTaskByIdWithLines() surfaces).
     * Rolls up task status: pending -> in_progress on first submission,
     * -> submitted once every line has at least one result.
     */
    public long submitCount(long taskLineId, int countedQuantity, String countedBy) throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                long taskId;
                int snapshot;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT cycle_count_task_id, cycle_count_task_line_system_quantity_snapshot FROM cycle_count_task_lines WHERE cycle_count_task_line_id = ?")) {
                    ps.setLong(1, taskLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InvalidStateException("Task line not found: " + taskLineId);
                        taskId = rs.getLong("cycle_count_task_id");
                        snapshot = rs.getInt("cycle_count_task_line_system_quantity_snapshot");
                    }
                }

                long resultLineId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO cycle_count_result_lines
                            (cycle_count_task_line_id, cycle_count_result_line_counted_quantity,
                             cycle_count_result_line_counted_by, cycle_count_result_line_variance_quantity)
                        VALUES (?, ?, ?, ?)
                        RETURNING cycle_count_result_line_id
                        """)) {
                    ps.setLong(1, taskLineId);
                    ps.setInt(2, countedQuantity);
                    ps.setString(3, countedBy);
                    ps.setInt(4, countedQuantity - snapshot);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        resultLineId = rs.getLong("cycle_count_result_line_id");
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE cycle_count_tasks SET cycle_count_task_status = 'in_progress' WHERE cycle_count_task_id = ? AND cycle_count_task_status = 'pending'")) {
                    ps.setLong(1, taskId);
                    ps.executeUpdate();
                }

                boolean allLinesHaveResults;
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT bool_and(EXISTS (
                            SELECT 1 FROM cycle_count_result_lines crl WHERE crl.cycle_count_task_line_id = ctl.cycle_count_task_line_id
                        )) AS all_have_results
                        FROM cycle_count_task_lines ctl WHERE ctl.cycle_count_task_id = ?
                        """)) {
                    ps.setLong(1, taskId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        allLinesHaveResults = rs.getBoolean("all_have_results");
                    }
                }
                if (allLinesHaveResults) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE cycle_count_tasks SET cycle_count_task_status = 'submitted' WHERE cycle_count_task_id = ?")) {
                        ps.setLong(1, taskId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return resultLineId;
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // -------------------------------------------------------------
    // Variance review - the approval gate before anything touches the ledger
    // -------------------------------------------------------------

    /**
     * On 'approved' with a nonzero variance: creates the inventory_adjustment
     * + line + posts CYCLE_COUNT_ADJUSTMENT to stock_movements, using the
     * seeded CYCLE_COUNT_CORRECTION reason code. approval_status is set
     * straight to 'approved' on the adjustment itself, since this review
     * step IS the approval - not a second gate.
     */
    public long reviewResultLine(long resultLineId, String decision, String reviewedBy) throws SQLException {
        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            conn.setAutoCommit(false);
            try {
                int variance, snapshot, counted;
                long taskId, productId, warehouseId, storageLocationId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT crl.cycle_count_result_line_variance_quantity, crl.cycle_count_result_line_counted_quantity,
                               ctl.cycle_count_task_line_system_quantity_snapshot, ctl.product_id, ctl.cycle_count_task_id,
                               ct.warehouse_id, ct.storage_location_id
                        FROM cycle_count_result_lines crl
                        JOIN cycle_count_task_lines ctl ON ctl.cycle_count_task_line_id = crl.cycle_count_task_line_id
                        JOIN cycle_count_tasks ct ON ct.cycle_count_task_id = ctl.cycle_count_task_id
                        WHERE crl.cycle_count_result_line_id = ? FOR UPDATE OF crl
                        """)) {
                    ps.setLong(1, resultLineId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new InvalidStateException("Result line not found: " + resultLineId);
                        variance = rs.getInt("cycle_count_result_line_variance_quantity");
                        counted = rs.getInt("cycle_count_result_line_counted_quantity");
                        snapshot = rs.getInt("cycle_count_task_line_system_quantity_snapshot");
                        productId = rs.getLong("product_id");
                        taskId = rs.getLong("cycle_count_task_id");
                        warehouseId = rs.getLong("warehouse_id");
                        storageLocationId = rs.getLong("storage_location_id");
                    }
                }

                long reviewId;
                try (PreparedStatement ps = conn.prepareStatement("""
                        INSERT INTO cycle_count_variance_reviews (cycle_count_result_line_id, cycle_count_variance_review_reviewed_by, cycle_count_variance_review_decision)
                        VALUES (?, ?, ?::cycle_count_variance_review_decision)
                        RETURNING cycle_count_variance_review_id
                        """)) {
                    ps.setLong(1, resultLineId);
                    ps.setString(2, reviewedBy);
                    ps.setString(3, decision);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        reviewId = rs.getLong("cycle_count_variance_review_id");
                    }
                }

                if ("approved".equals(decision) && variance != 0) {
                    long reasonCodeId;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT adjustment_reason_code_id FROM adjustment_reason_codes WHERE adjustment_reason_code_name = 'CYCLE_COUNT_CORRECTION'")) {
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new InvalidStateException(
                                    "CYCLE_COUNT_CORRECTION reason code not found - has migration 15 been run?");
                            reasonCodeId = rs.getLong("adjustment_reason_code_id");
                        }
                    }

                    long adjustmentId;
                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO inventory_adjustments
                                (warehouse_id, adjustment_reason_code_id, inventory_adjustment_source_type,
                                 inventory_adjustment_source_reference_id, inventory_adjustment_requested_by,
                                 inventory_adjustment_approved_by, inventory_adjustment_approval_status)
                            VALUES (?, ?, 'cycle_count', ?, ?, ?, 'approved')
                            RETURNING inventory_adjustment_id
                            """)) {
                        ps.setLong(1, warehouseId);
                        ps.setLong(2, reasonCodeId);
                        ps.setLong(3, reviewId);
                        ps.setString(4, reviewedBy);
                        ps.setString(5, reviewedBy);
                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            adjustmentId = rs.getLong("inventory_adjustment_id");
                        }
                    }

                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO inventory_adjustment_lines (inventory_adjustment_id, product_id, storage_location_id, inventory_adjustment_line_quantity_delta)
                            VALUES (?, ?, ?, ?)
                            """)) {
                        ps.setLong(1, adjustmentId);
                        ps.setLong(2, productId);
                        ps.setLong(3, storageLocationId);
                        ps.setInt(4, variance);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement("""
                            INSERT INTO stock_movements
                                (product_id, warehouse_id, stock_movement_type, stock_movement_quantity_delta,
                                 storage_location_id, stock_movement_reference_table, stock_movement_reference_id, stock_movement_created_by)
                            VALUES (?, ?, 'CYCLE_COUNT_ADJUSTMENT', ?, ?, 'inventory_adjustments', ?, ?)
                            """)) {
                        ps.setLong(1, productId);
                        ps.setLong(2, warehouseId);
                        ps.setInt(3, variance);
                        ps.setLong(4, storageLocationId);
                        ps.setLong(5, adjustmentId);
                        ps.setString(6, reviewedBy);
                        ps.executeUpdate();
                    }
                }

                boolean allLinesReviewed;
                try (PreparedStatement ps = conn.prepareStatement("""
                        SELECT bool_and(EXISTS (
                            SELECT 1 FROM cycle_count_variance_reviews cvr
                            JOIN cycle_count_result_lines crl2 ON crl2.cycle_count_result_line_id = cvr.cycle_count_result_line_id
                            WHERE crl2.cycle_count_task_line_id = ctl.cycle_count_task_line_id
                        )) AS all_reviewed
                        FROM cycle_count_task_lines ctl WHERE ctl.cycle_count_task_id = ?
                        """)) {
                    ps.setLong(1, taskId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        allLinesReviewed = rs.getBoolean("all_reviewed");
                    }
                }
                if (allLinesReviewed) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE cycle_count_tasks SET cycle_count_task_status = 'reviewed' WHERE cycle_count_task_id = ?")) {
                        ps.setLong(1, taskId);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return reviewId;
            } catch (SQLException | InvalidStateException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
