package ke.co.skyworld.internship.domain.beans.count_cycles;


import java.util.Date;
import java.util.List;



    public record CycleCountTaskResponse(long taskId, long warehouseId, long storageLocationId, Date scheduledDate,
                                  String assignedTo, String status, List<Line> lines) {

        public record Line(long taskLineId, long productId, int systemQuantitySnapshot, Long latestResultLineId,
                           Integer countedQuantity, Integer varianceQuantity, String reviewDecision) { }
    }
