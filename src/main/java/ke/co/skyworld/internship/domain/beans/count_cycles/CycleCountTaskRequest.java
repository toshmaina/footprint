package ke.co.skyworld.internship.domain.beans.count_cycles;



import java.util.Date;

public class CycleCountTaskRequest {

    private Long warehouseId;
    private Long storageLocationId;
    private Date scheduledDate;
    private String assignedTo;

    public CycleCountTaskRequest() {
        // required for JSON deserialization
    }

    public CycleCountTaskRequest(Long warehouseId, Long storageLocationId, Date scheduledDate, String assignedTo) {
        this.warehouseId = warehouseId;
        this.storageLocationId = storageLocationId;
        this.scheduledDate = scheduledDate;
        this.assignedTo = assignedTo;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Long getStorageLocationId() {
        return storageLocationId;
    }

    public void setStorageLocationId(Long storageLocationId) {
        this.storageLocationId = storageLocationId;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }
}