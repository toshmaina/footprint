package ke.co.skyworld.internship.controllers.handlers.shipment;




import ke.co.skyworld.internship.domain.beans.shipment.ShipmentResponse;
import ke.co.skyworld.internship.repository.ShipmentRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Optional;

public class GetShipmentHandler extends SkyInventoryManagementHttpHandler {

    private final ShipmentRepository shipmentRepository = new ShipmentRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long shipmentId;
        try {
            shipmentId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid shipment id", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            Optional<ShipmentResponse> shipment = shipmentRepository.findByIdWithPackages(shipmentId);
            if (shipment.isEmpty()) {
                sendError(exchange, "Shipment not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, shipment.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching shipment failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching shipment failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}