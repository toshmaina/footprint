package ke.co.skyworld.internship.util.http.handlers.products;

import ke.co.skyworld.internship.domain.beans.ProductResponse;
import ke.co.skyworld.internship.repository.ProductRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Optional;

public class GetProductHandler extends SkyInventoryManagementHttpHandler {

    private final ProductRepository productRepository = new ProductRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long productId;
        try {
            productId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid product id", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            Optional<ProductResponse> product = productRepository.findById(productId);
            if (product.isEmpty()) {
                sendError(exchange, "Product not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, product.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching product failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching product failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}