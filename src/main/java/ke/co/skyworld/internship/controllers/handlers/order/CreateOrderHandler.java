package ke.co.skyworld.internship.controllers.handlers.order;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.order.OrderRequest;
import ke.co.skyworld.internship.domain.beans.order.OrderResponse;
import ke.co.skyworld.internship.repository.OrderRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;

import java.sql.SQLException;

public class CreateOrderHandler extends SkyInventoryManagementHttpHandler {

    private final OrderRepository orderRepository = new OrderRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        OrderRequest request;
        try {
            request = parseBody(exchange, OrderRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getCustomerId() == null) {
            sendError(exchange, "customerId is required", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request.getLines() == null || request.getLines().isEmpty()) {
            sendError(exchange, "An order must have at least one line", StatusCodes.BAD_REQUEST);
            return;
        }
        for (OrderRequest.Line line : request.getLines()) {
            if (line.getProductId() == null || line.getQuantityOrdered() == null || line.getQuantityOrdered() <= 0) {
                sendError(exchange, "Each line requires productId and a positive quantityOrdered", StatusCodes.BAD_REQUEST);
                return;
            }
        }

        RequestContext context = currentUser(exchange);
        String requestedBy = String.valueOf(context.userAccountId());

        try {
            OrderResponse response = orderRepository.createWithLines(request.getCustomerId(), request.getLines(), requestedBy);
            send(exchange, response, StatusCodes.CREATED);
        } catch (OrderRepository.CustomerNotFoundException e) {
            sendError(exchange, e.getMessage(), StatusCodes.BAD_REQUEST);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "A line's productId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Order creation failed: " + e.getMessage(), e);
            sendError(exchange, "Order creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}