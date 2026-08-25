package ke.co.skyworld.internship.util.http.handlers.products;

import ke.co.skyworld.internship.repository.ProductRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

/**
 * Soft-delete only - see migration 06's header comment for why a hard
 * DELETE isn't offered for this resource.
 */
public class DeactivateProductHandler extends SkyInventoryManagementHttpHandler {

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
            boolean deactivated = productRepository.deactivate(productId);
            if (!deactivated) {
                sendError(exchange, "Product not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, java.util.Map.of("message", "Product deactivated"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Product deactivation failed: " + e.getMessage(), e);
            sendError(exchange, "Product deactivation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}