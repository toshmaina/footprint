package ke.co.skyworld.internship.util.http.handlers.products;

import ke.co.skyworld.internship.domain.beans.ProductRequest;
import ke.co.skyworld.internship.repository.ProductRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Set;

public class CreateProductHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_CLASSIFICATIONS = Set.of("A", "B", "C");

    private final ProductRepository productRepository = new ProductRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        ProductRequest request;
        try {
            request = parseBody(exchange, ProductRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || isBlank(request.getSku()) || isBlank(request.getName())) {
            sendError(exchange, "sku and name are required", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request.getClassification() != null && !VALID_CLASSIFICATIONS.contains(request.getClassification())) {
            sendError(exchange, "classification must be one of A, B, C", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request.getReorderThreshold() != null && request.getReorderThreshold() < 0) {
            sendError(exchange, "reorderThreshold cannot be negative", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            if (productRepository.skuExists(request.getSku())) {
                sendError(exchange, "A product with this SKU already exists", StatusCodes.CONFLICT);
                return;
            }

            long productId = productRepository.create(request.getSku(), request.getName(),
                    request.getCategory(), request.getReorderThreshold(), request.getClassification());

            send(exchange, java.util.Map.of("productId", productId), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "A product with this SKU already exists", StatusCodes.CONFLICT);
                return;
            }
            Log.error(getClass(), "handleRequest", "Product creation failed: " + e.getMessage(), e);
            sendError(exchange, "Product creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}