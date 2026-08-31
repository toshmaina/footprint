package ke.co.skyworld.internship.controllers.handlers.customer;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.customer.CustomerRequest;
import ke.co.skyworld.internship.repository.CustomerRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

public class CreateCustomerHandler extends SkyInventoryManagementHttpHandler {

    private final CustomerRepository customerRepository = new CustomerRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        CustomerRequest request;
        try {
            request = parseBody(exchange, CustomerRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getName() == null || request.getName().isBlank()
                || request.getDefaultWarehouseId() == null) {
            sendError(exchange, "name and defaultWarehouseId are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            long id = customerRepository.create(request.getName(), request.getDefaultWarehouseId(),
                    request.getContactEmail(), request.getContactPhone());
            send(exchange, java.util.Map.of("customerId", id), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "defaultWarehouseId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Customer creation failed: " + e.getMessage(), e);
            sendError(exchange, "Customer creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
