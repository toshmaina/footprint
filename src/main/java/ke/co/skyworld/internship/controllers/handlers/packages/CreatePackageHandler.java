package ke.co.skyworld.internship.controllers.handlers.packages;




import ke.co.skyworld.internship.domain.beans.packages.PackageRequest;
import ke.co.skyworld.internship.repository.PackageRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class CreatePackageHandler extends SkyInventoryManagementHttpHandler {

    private final PackageRepository packageRepository = new PackageRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        PackageRequest request;
        try {
            request = parseBody(exchange, PackageRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request == null || request.getOrderId() == null) {
            sendError(exchange, "orderId is required", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            long id = packageRepository.create(request.getOrderId());
            send(exchange, java.util.Map.of("packageId", id), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "orderId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Package creation failed: " + e.getMessage(), e);
            sendError(exchange, "Package creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
