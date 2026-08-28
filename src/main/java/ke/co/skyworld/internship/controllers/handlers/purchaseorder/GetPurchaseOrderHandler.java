package ke.co.skyworld.internship.controllers.handlers.purchaseorder;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.purchaseorder.PurchaseOrderResponse;
import ke.co.skyworld.internship.repository.PurchaseOrderRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Optional;

public class GetPurchaseOrderHandler extends SkyInventoryManagementHttpHandler {

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
            Optional<PurchaseOrderResponse> po = purchaseOrderRepository.findByIdWithLines(purchaseOrderId);
            if (po.isEmpty()) {
                sendError(exchange, "Purchase order not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, po.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching purchase order failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching purchase order failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}