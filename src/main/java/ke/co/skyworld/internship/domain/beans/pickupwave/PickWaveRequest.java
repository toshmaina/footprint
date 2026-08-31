package ke.co.skyworld.internship.domain.beans.pickupwave;


import java.util.Date;

public class PickWaveRequest {

    private Long warehouseId;
    private Date cutoffTime;

    public PickWaveRequest() {
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public Date getCutoffTime() {
        return cutoffTime;
    }

    public void setCutoffTime(Date cutoffTime) {
        this.cutoffTime = cutoffTime;
    }
}