package ke.co.skyworld.internship.controllers.handlers.customer;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.domain.beans.customer.CustomerResponse;
import ke.co.skyworld.internship.repository.CustomerRepository;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

public class ListCustomersHandler extends SkyInventoryManagementHttpHandler {

    private final CustomerRepository customerRepository = new CustomerRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);
        try {
            PageResult<CustomerResponse> result = customerRepository.list(pageAndPageSize[0], pageAndPageSize[1]);
            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing customers failed: " + e.getMessage(), e);
            sendError(exchange, "Listing customers failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}