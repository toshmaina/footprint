package ke.co.skyworld.internship.controllers.handlers.order;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.order.OrderResponse;
import ke.co.skyworld.internship.repository.OrderRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Optional;

public class GetOrderHandler extends SkyInventoryManagementHttpHandler {

    private final OrderRepository orderRepository = new OrderRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long orderId;
        try {
            orderId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid order id", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            Optional<OrderResponse> order = orderRepository.findByIdWithLines(orderId);
            if (order.isEmpty()) {
                sendError(exchange, "Order not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, order.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching order failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching order failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}