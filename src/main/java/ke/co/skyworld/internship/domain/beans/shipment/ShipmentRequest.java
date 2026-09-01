package ke.co.skyworld.internship.domain.beans.shipment;
public class ShipmentRequest {
    private Long warehouseId;
    private String carrier;
    private String trackingNumber;

    public ShipmentRequest() {
    }

    public Long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(Long warehouseId) { this.warehouseId = warehouseId; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
}
