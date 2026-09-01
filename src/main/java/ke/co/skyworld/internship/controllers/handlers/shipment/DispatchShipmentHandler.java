package ke.co.skyworld.internship.controllers.handlers.shipment;



import ke.co.skyworld.internship.repository.ShipmentRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

/**
 * Writes audit-only SHIP ledger entries (PICK already decremented
 * available_stock()), marks every touched order_line 'shipped', and
 * rolls up order-level status per distinct order affected.
 */
public class DispatchShipmentHandler extends SkyInventoryManagementHttpHandler {

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

        RequestContext context = currentUser(exchange);
        String dispatchedBy = String.valueOf(context.userAccountId());

        try {
            shipmentRepository.dispatch(shipmentId, dispatchedBy);
            send(exchange, java.util.Map.of("message", "Shipment dispatched"), StatusCodes.OK);
        } catch (ShipmentRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Dispatch failed: " + e.getMessage(), e);
            sendError(exchange, "Dispatch failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
