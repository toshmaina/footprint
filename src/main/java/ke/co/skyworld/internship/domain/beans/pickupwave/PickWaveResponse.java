package ke.co.skyworld.internship.domain.beans.pickupwave;


import java.util.Date;
import java.util.List;

public record PickWaveResponse(long pickWaveId, long warehouseId, String status, Date cutoffTime,
                               List<TaskSummary> tasks, Date dateCreated) {
    public record TaskSummary(long pickTaskId, long orderLineId, long licensePlateId, long storageLocationId,
                              long productId, int requestedQuantity, String status) {
    }
}