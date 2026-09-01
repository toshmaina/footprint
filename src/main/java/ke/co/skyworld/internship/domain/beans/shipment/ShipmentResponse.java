package ke.co.skyworld.internship.domain.beans.shipment;


import java.util.Date;
import java.util.List;

public class ShipmentResponse {
    private long shipmentId;
    private long warehouseId;
    private String carrier;
    private String trackingNumber;
    private String status;
    private Date manifestedAt;
    private Date dispatchedAt;
    private List<Long> packageIds;

    public ShipmentResponse() {
    }

    public ShipmentResponse(long shipmentId, long warehouseId, String carrier, String trackingNumber, String status,
                            Date manifestedAt, Date dispatchedAt, List<Long> packageIds) {
        this.shipmentId = shipmentId;
        this.warehouseId = warehouseId;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.manifestedAt = manifestedAt;
        this.dispatchedAt = dispatchedAt;
        this.packageIds = packageIds;
    }

    public long getShipmentId() { return shipmentId; }
    public void setShipmentId(long shipmentId) { this.shipmentId = shipmentId; }
    public long getWarehouseId() { return warehouseId; }
    public void setWarehouseId(long warehouseId) { this.warehouseId = warehouseId; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getManifestedAt() { return manifestedAt; }
    public void setManifestedAt(Date manifestedAt) { this.manifestedAt = manifestedAt; }
    public Date getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(Date dispatchedAt) { this.dispatchedAt = dispatchedAt; }
    public List<Long> getPackageIds() { return packageIds; }
    public void setPackageIds(List<Long> packageIds) { this.packageIds = packageIds; }
}
