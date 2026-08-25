package ke.co.skyworld.internship.util.http.handlers.products;


import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.domain.beans.ProductResponse;
import ke.co.skyworld.internship.repository.ProductRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class ListProductsHandler extends SkyInventoryManagementHttpHandler {

    private final ProductRepository productRepository = new ProductRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);
        boolean includeInactive = "true".equalsIgnoreCase(getQueryParam(exchange, "includeInactive", "false"));

        try {
            PageResult<ProductResponse> result =
                    productRepository.list(pageAndPageSize[0], pageAndPageSize[1], includeInactive);

            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing products failed: " + e.getMessage(), e);
            sendError(exchange, "Listing products failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}