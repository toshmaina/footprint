package ke.co.skyworld.internship.domain.beans.pickupwave;


import java.util.Date;
import java.util.List;

public class PickWaveResponse {

    private long pickWaveId;
    private long warehouseId;
    private String status;
    private Date cutoffTime;
    private List<TaskSummary> tasks;
    private Date dateCreated;

    public PickWaveResponse() {
    }

    public PickWaveResponse(long pickWaveId, long warehouseId, String status, Date cutoffTime, List<TaskSummary> tasks, Date dateCreated) {
        this.pickWaveId = pickWaveId;
        this.warehouseId = warehouseId;
        this.status = status;
        this.cutoffTime = cutoffTime;
        this.tasks = tasks;
        this.dateCreated = dateCreated;
    }

    public long getPickWaveId() {
        return pickWaveId;
    }

    public void setPickWaveId(long pickWaveId) {
        this.pickWaveId = pickWaveId;
    }

    public long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCutoffTime() {
        return cutoffTime;
    }

    public void setCutoffTime(Date cutoffTime) {
        this.cutoffTime = cutoffTime;
    }

    public List<TaskSummary> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskSummary> tasks) {
        this.tasks = tasks;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    // --- Inner Class for Task Summaries ---

    public static class TaskSummary {
        private long pickTaskId;
        private long orderLineId;
        private long licensePlateId;
        private long storageLocationId;
        private long productId;
        private int requestedQuantity;
        private String status;

        public TaskSummary() {
        }

        public TaskSummary(long pickTaskId, long orderLineId, long licensePlateId, long storageLocationId, long productId, int requestedQuantity, String status) {
            this.pickTaskId = pickTaskId;
            this.orderLineId = orderLineId;
            this.licensePlateId = licensePlateId;
            this.storageLocationId = storageLocationId;
            this.productId = productId;
            this.requestedQuantity = requestedQuantity;
            this.status = status;
        }

        public long getPickTaskId() {
            return pickTaskId;
        }

        public void setPickTaskId(long pickTaskId) {
            this.pickTaskId = pickTaskId;
        }

        public long getOrderLineId() {
            return orderLineId;
        }

        public void setOrderLineId(long orderLineId) {
            this.orderLineId = orderLineId;
        }

        public long getLicensePlateId() {
            return licensePlateId;
        }

        public void setLicensePlateId(long licensePlateId) {
            this.licensePlateId = licensePlateId;
        }

        public long getStorageLocationId() {
            return storageLocationId;
        }

        public void setStorageLocationId(long storageLocationId) {
            this.storageLocationId = storageLocationId;
        }

        public long getProductId() {
            return productId;
        }

        public void setProductId(long productId) {
            this.productId = productId;
        }

        public int getRequestedQuantity() {
            return requestedQuantity;
        }

        public void setRequestedQuantity(int requestedQuantity) {
            this.requestedQuantity = requestedQuantity;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}