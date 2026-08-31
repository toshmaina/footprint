package ke.co.skyworld.internship.repository;


import ke.co.skyworld.internship.domain.beans.pickupwave.PickWaveResponse;
import ke.co.skyworld.internship.util.db.ConnectionPool;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PickWaveRepository {

    public long create(long warehouseId, java.util.Date cutoffTime) throws SQLException {
        String sql = "INSERT INTO pick_waves (warehouse_id, pick_wave_cutoff_time) VALUES (?, ?) RETURNING pick_wave_id";
        try (Connection conn = ConnectionPool.getInstance().borrow();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, warehouseId);
            if (cutoffTime != null) ps.setTimestamp(2, new Timestamp(cutoffTime.getTime()));
            else ps.setNull(2, Types.TIMESTAMP);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong("pick_wave_id");
            }
        }
    }

    public Optional<PickWaveResponse> findByIdWithTasks(long pickWaveId) throws SQLException {
        String headerSql = "SELECT * FROM pick_waves WHERE pick_wave_id = ?";
        String tasksSql = "SELECT * FROM pick_tasks WHERE pick_wave_id = ? ORDER BY pick_task_id";

        try (Connection conn = ConnectionPool.getInstance().borrow()) {
            long warehouseId;
            String status;
            java.sql.Timestamp cutoff;
            java.sql.Timestamp created;
            try (PreparedStatement ps = conn.prepareStatement(headerSql)) {
                ps.setLong(1, pickWaveId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return Optional.empty();
                    warehouseId = rs.getLong("warehouse_id");
                    status = rs.getString("pick_wave_status");
                    cutoff = rs.getTimestamp("pick_wave_cutoff_time");
                    created = rs.getTimestamp("date_created");
                }
            }

            List<PickWaveResponse.TaskSummary> tasks = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(tasksSql)) {
                ps.setLong(1, pickWaveId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        tasks.add(new PickWaveResponse.TaskSummary(
                                rs.getLong("pick_task_id"), rs.getLong("order_line_id"), rs.getLong("license_plate_id"),
                                rs.getLong("storage_location_id"), rs.getLong("product_id"),
                                rs.getInt("pick_task_quantity_requested"), rs.getString("pick_task_status")));
                    }
                }
            }

            return Optional.of(new PickWaveResponse(pickWaveId, warehouseId, status, cutoff, tasks, created));
        }
    }
}
