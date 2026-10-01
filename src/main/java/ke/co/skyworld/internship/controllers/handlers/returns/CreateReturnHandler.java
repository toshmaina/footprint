package ke.co.skyworld.internship.controllers.handlers.returns;



import ke.co.skyworld.internship.domain.beans.returns.ReturnRequest;
import ke.co.skyworld.internship.repository.ReturnRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class CreateReturnHandler extends SkyInventoryManagementHttpHandler {

    private final ReturnRepository returnRepository = new ReturnRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        ReturnRequest request;
        try {
            request = parseBody(exchange, ReturnRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getOrderId() == null || request.getWarehouseId() == null) {
            sendError(exchange, "orderId and warehouseId are required", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request.getLines() == null || request.getLines().isEmpty()) {
            sendError(exchange, "A return must have at least one line", StatusCodes.BAD_REQUEST);
            return;
        }
        for (ReturnRequest.Line line : request.getLines()) {
            if (line.getOrderLineId() == null || line.getQuantity() == null || line.getQuantity() <= 0) {
                sendError(exchange, "Each line requires orderLineId and a positive quantity", StatusCodes.BAD_REQUEST);
                return;
            }
        }

        RequestContext context = currentUser(exchange);
        String requestedBy = String.valueOf(context.userAccountId());

        try {
            long returnId = returnRepository.createWithLines(request.getOrderId(), request.getWarehouseId(),
                    request.getLines(), requestedBy);
            send(exchange, java.util.Map.of("returnId", returnId), StatusCodes.CREATED);
        } catch (ReturnRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "orderId, warehouseId, or a line's orderLineId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Return creation failed: " + e.getMessage(), e);
            sendError(exchange, "Return creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
