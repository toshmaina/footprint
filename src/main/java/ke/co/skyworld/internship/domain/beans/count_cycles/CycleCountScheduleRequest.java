package ke.co.skyworld.internship.domain.beans.count_cycles;


public class CycleCountScheduleRequest {

    private Long warehouseId;
    private String productClassification;
    private Integer countFrequencyDays;

    public CycleCountScheduleRequest() {
        // required for JSON deserialization
    }

    public CycleCountScheduleRequest(Long warehouseId, String productClassification, Integer countFrequencyDays) {
        this.warehouseId = warehouseId;
        this.productClassification = productClassification;
        this.countFrequencyDays = countFrequencyDays;
    }

    public Long getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(Long warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getProductClassification() {
        return productClassification;
    }

    public void setProductClassification(String productClassification) {
        this.productClassification = productClassification;
    }

    public Integer getCountFrequencyDays() {
        return countFrequencyDays;
    }

    public void setCountFrequencyDays(Integer countFrequencyDays) {
        this.countFrequencyDays = countFrequencyDays;
    }
}