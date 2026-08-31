package ke.co.skyworld.internship.controllers.handlers.customer;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.customer.CustomerResponse;
import ke.co.skyworld.internship.repository.CustomerRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Optional;

public class GetCustomerHandler extends SkyInventoryManagementHttpHandler {

    private final CustomerRepository customerRepository = new CustomerRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long id;
        try {
            id = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid customer id", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            Optional<CustomerResponse> customer = customerRepository.findById(id);
            if (customer.isEmpty()) {
                sendError(exchange, "Customer not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, customer.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching customer failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching customer failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}