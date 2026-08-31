package ke.co.skyworld.internship.controllers.handlers.purchaseorder;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.purchaseorder.PurchaseOrderRequest;
import ke.co.skyworld.internship.repository.PurchaseOrderRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;

import java.sql.SQLException;

public class CreatePurchaseOrderHandler extends SkyInventoryManagementHttpHandler {

    private final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        PurchaseOrderRequest request;
        try {
            request = parseBody(exchange, PurchaseOrderRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getSupplierId() == null || request.getWarehouseId() == null) {
            sendError(exchange, "supplierId and warehouseId are required", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request.getLines() == null || request.getLines().isEmpty()) {
            sendError(exchange, "A purchase order must have at least one line", StatusCodes.BAD_REQUEST);
            return;
        }

        for (PurchaseOrderRequest.Line line : request.getLines()) {
            if (line.getProductId() == null || line.getQuantityOrdered() == null || line.getUnitCost() == null) {
                sendError(exchange, "Each line requires productId, quantityOrdered, and unitCost", StatusCodes.BAD_REQUEST);
                return;
            }
            if (line.getQuantityOrdered() <= 0) {
                sendError(exchange, "quantityOrdered must be positive", StatusCodes.BAD_REQUEST);
                return;
            }
            if (line.getUnitCost().signum() < 0) {
                sendError(exchange, "unitCost cannot be negative", StatusCodes.BAD_REQUEST);
                return;
            }
        }

        // Same free-text "who did this" limitation flagged earlier in this
        // project (VARCHAR columns across the schema instead of a real FK
        // to user_accounts) - storing the numeric id as text is a stopgap,
        // not a fix. Worth the broader migration eventually, not solved
        // piecemeal per-handler here.
        RequestContext context = currentUser(exchange);
        String createdBy = String.valueOf(context.userAccountId());

        try {
            long purchaseOrderId = purchaseOrderRepository.createWithLines(
                    request.getSupplierId(), request.getWarehouseId(), request.getExpectedDate(),
                    createdBy, request.getLines());
            send(exchange, java.util.Map.of("purchaseOrderId", purchaseOrderId), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "supplierId, warehouseId, or a line's productId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Purchase order creation failed: " + e.getMessage(), e);
            sendError(exchange, "Purchase order creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}