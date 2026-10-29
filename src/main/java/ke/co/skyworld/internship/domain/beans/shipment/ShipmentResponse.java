package ke.co.skyworld.internship.domain.beans.shipment;


import java.util.Date;
import java.util.List;

public record ShipmentResponse(long shipmentId, long warehouseId, String carrier, String trackingNumber, String status,
                               Date manifestedAt, Date dispatchedAt, List<Long> packageIds) {
}