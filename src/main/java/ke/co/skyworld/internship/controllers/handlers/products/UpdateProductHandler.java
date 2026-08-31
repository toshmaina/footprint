package ke.co.skyworld.internship.controllers.handlers.products;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.ProductRequest;
import ke.co.skyworld.internship.repository.ProductRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Set;

public class UpdateProductHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_CLASSIFICATIONS = Set.of("A", "B", "C");

    private final ProductRepository productRepository = new ProductRepository();

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long productId;
        try {
            productId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid product id", StatusCodes.BAD_REQUEST);
            return;
        }

        ProductRequest request;
        try {
            request = parseBody(exchange, ProductRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || isBlank(request.getName())) {
            sendError(exchange, "name is required", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request.getClassification() != null && !VALID_CLASSIFICATIONS.contains(request.getClassification())) {
            sendError(exchange, "classification must be one of A, B, C", StatusCodes.BAD_REQUEST);
            return;
        }

        // Note: SKU is intentionally not updatable here - it's the stable
        // external identifier referenced throughout the system (purchase
        // order lines, ASN lines, stock reports). Changing it after
        // creation is a bigger decision than a routine field edit; if you
        // need that, it should be a deliberate separate operation, not a
        // side effect of a general update call.
        try {
            boolean updated = productRepository.update(productId, request.getName(), request.getCategory(),
                    request.getReorderThreshold(), request.getClassification());
            if (!updated) {
                sendError(exchange, "Product not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, java.util.Map.of("message", "Product updated"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Product update failed: " + e.getMessage(), e);
            sendError(exchange, "Product update failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}