package ke.co.skyworld.internship.controllers.handlers.purchaseorder;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.repository.PurchaseOrderRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

/**
 * Status transition only - see PurchaseOrderRepository.cancel() for why
 * this is restricted to draft/sent, not a general-purpose update.
 */
public class CancelPurchaseOrderHandler extends SkyInventoryManagementHttpHandler {

    private final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long purchaseOrderId;
        try {
            purchaseOrderId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid purchase order id", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            boolean cancelled = purchaseOrderRepository.cancel(purchaseOrderId);
            if (!cancelled) {
                sendError(exchange,
                        "Purchase order not found, or not in a cancellable status (draft/sent only)",
                        StatusCodes.CONFLICT);
                return;
            }
            send(exchange, java.util.Map.of("message", "Purchase order cancelled"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Purchase order cancellation failed: " + e.getMessage(), e);
            sendError(exchange, "Purchase order cancellation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}