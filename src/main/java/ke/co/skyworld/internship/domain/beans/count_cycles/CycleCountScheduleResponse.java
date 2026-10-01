package ke.co.skyworld.internship.domain.beans.count_cycles;

public class CycleCountScheduleResponse {
    private long scheduleId;
    private long warehouseId;
    private String productClassification;
    private int countFrequencyDays;

    public CycleCountScheduleResponse() {
    }

    public CycleCountScheduleResponse(long scheduleId, long warehouseId, String productClassification, int countFrequencyDays) {
        this.scheduleId = scheduleId;
        this.warehouseId = warehouseId;
        this.productClassification = productClassification;
        this.countFrequencyDays = countFrequencyDays;
    }

    public long getScheduleId() { return scheduleId; }
    public void setScheduleId(long scheduleId) { this.scheduleId = scheduleId; }
    public long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(long warehouseId) { this.warehouseId = warehouseId; }
    public String getProductClassification() { return productClassification; }
    public void setProductClassification(String productClassification) { this.productClassification = productClassification; }
    public int getCountFrequencyDays() { return countFrequencyDays; }
    public void setCountFrequencyDays(int countFrequencyDays) { this.countFrequencyDays = countFrequencyDays; }
}

