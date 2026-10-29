package ke.co.skyworld.internship.domain.beans.shipment;

public record ShipmentRequest(Long warehouseId, String carrier, String trackingNumber) {
}
