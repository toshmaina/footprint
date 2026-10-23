package ke.co.skyworld.internship.domain.beans.count_cycles;


import java.util.Date;

public record  CycleCountTaskRequest(Long warehouseId, Long storageLocationId, Date scheduledDate, String assignedTo) {}
