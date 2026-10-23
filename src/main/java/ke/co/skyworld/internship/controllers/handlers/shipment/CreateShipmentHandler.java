package ke.co.skyworld.internship.controllers.handlers.shipment;




import ke.co.skyworld.internship.domain.beans.shipment.ShipmentRequest;
import ke.co.skyworld.internship.repository.ShipmentRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class CreateShipmentHandler extends SkyInventoryManagementHttpHandler {

    private final ShipmentRepository shipmentRepository = new ShipmentRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        ShipmentRequest request;
        try {
            request = parseBody(exchange, ShipmentRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request == null || request.getWarehouseId() == null) {
            sendError(exchange, "warehouseId is required", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            long id = shipmentRepository.create(request.getWarehouseId(), request.getCarrier(), request.getTrackingNumber());
            send(exchange, java.util.Map.of("shipmentId", id), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "warehouseId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Shipment creation failed: " + e.getMessage(), e);
            sendError(exchange, "Shipment creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}