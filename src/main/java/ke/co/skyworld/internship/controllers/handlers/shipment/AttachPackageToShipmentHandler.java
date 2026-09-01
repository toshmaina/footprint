package ke.co.skyworld.internship.controllers.handlers.shipment;



import ke.co.skyworld.internship.domain.beans.shipment.AttachPackageRequest;
import ke.co.skyworld.internship.repository.ShipmentRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class AttachPackageToShipmentHandler extends SkyInventoryManagementHttpHandler {

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

        AttachPackageRequest request;
        try {
            request = parseBody(exchange, AttachPackageRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request == null || request.getPackageId() == null) {
            sendError(exchange, "packageId is required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            shipmentRepository.attachPackage(shipmentId, request.getPackageId());
            send(exchange, java.util.Map.of("message", "Package attached to shipment"), StatusCodes.OK);
        } catch (ShipmentRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "This package is already attached to this shipment", StatusCodes.CONFLICT);
                return;
            }
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "shipmentId or packageId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Attaching package failed: " + e.getMessage(), e);
            sendError(exchange, "Attaching package failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}