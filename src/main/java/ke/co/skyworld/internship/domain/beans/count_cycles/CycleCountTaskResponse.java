package ke.co.skyworld.internship.domain.beans.count_cycles;


import java.util.Date;
import java.util.List;

public class CycleCountTaskResponse {
    private long taskId;
    private long warehouseId;
    private long storageLocationId;
    private Date scheduledDate;
    private String assignedTo;
    private String status;
    private List<Line> lines;

    public CycleCountTaskResponse() {
    }

    public CycleCountTaskResponse(long taskId, long warehouseId, long storageLocationId, Date scheduledDate,
                                  String assignedTo, String status, List<Line> lines) {
        this.taskId = taskId;
        this.warehouseId = warehouseId;
        this.storageLocationId = storageLocationId;
        this.scheduledDate = scheduledDate;
        this.assignedTo = assignedTo;
        this.status = status;
        this.lines = lines;
    }

    public long getTaskId() { return taskId; }
    public void setTaskId(long taskId) { this.taskId = taskId; }
    public long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(long warehouseId) { this.warehouseId = warehouseId; }
    public long getStorageLocationId() { return storageLocationId; }
    public void setStorageLocationId(long storageLocationId) { this.storageLocationId = storageLocationId; }
    public Date getScheduledDate() { return scheduledDate; }
    public void setScheduledDate(Date scheduledDate) { this.scheduledDate = scheduledDate; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }

    public static class Line {
        private long taskLineId;
        private long productId;
        private int systemQuantitySnapshot;
        private Long latestResultLineId;
        private Integer countedQuantity;
        private Integer varianceQuantity;
        private String reviewDecision;

        public Line() {
        }

        public Line(long taskLineId, long productId, int systemQuantitySnapshot, Long latestResultLineId,
                    Integer countedQuantity, Integer varianceQuantity, String reviewDecision) {
            this.taskLineId = taskLineId;
            this.productId = productId;
            this.systemQuantitySnapshot = systemQuantitySnapshot;
            this.latestResultLineId = latestResultLineId;
            this.countedQuantity = countedQuantity;
            this.varianceQuantity = varianceQuantity;
            this.reviewDecision = reviewDecision;
        }

        public long getTaskLineId() { return taskLineId; }
        public void setTaskLineId(long taskLineId) { this.taskLineId = taskLineId; }
        public long getProductId() { return productId; }
        public void setProductId(long productId) { this.productId = productId; }
        public int getSystemQuantitySnapshot() { return systemQuantitySnapshot; }
        public void setSystemQuantitySnapshot(int systemQuantitySnapshot) { this.systemQuantitySnapshot = systemQuantitySnapshot; }
        public Long getLatestResultLineId() { return latestResultLineId; }
        public void setLatestResultLineId(Long latestResultLineId) { this.latestResultLineId = latestResultLineId; }
        public Integer getCountedQuantity() { return countedQuantity; }
        public void setCountedQuantity(Integer countedQuantity) { this.countedQuantity = countedQuantity; }
        public Integer getVarianceQuantity() { return varianceQuantity; }
        public void setVarianceQuantity(Integer varianceQuantity) { this.varianceQuantity = varianceQuantity; }
        public String getReviewDecision() { return reviewDecision; }
        public void setReviewDecision(String reviewDecision) { this.reviewDecision = reviewDecision; }
    }
}
