package ke.co.skyworld.internship.controllers.handlers.packages;



import ke.co.skyworld.internship.domain.beans.packages.PackageLineRequest;
import ke.co.skyworld.internship.repository.PackageRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class AddPackageLineHandler extends SkyInventoryManagementHttpHandler {

    private final PackageRepository packageRepository = new PackageRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long packageId;
        try {
            packageId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid package id", StatusCodes.BAD_REQUEST);
            return;
        }

        PackageLineRequest request;
        try {
            request = parseBody(exchange, PackageLineRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request == null || request.getOrderLineId() == null || request.getQuantity() == null
                || request.getQuantity() <= 0) {
            sendError(exchange, "orderLineId and a positive quantity are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            long lineId = packageRepository.addLine(packageId, request.getOrderLineId(), request.getQuantity());
            send(exchange, java.util.Map.of("packageLineId", lineId), StatusCodes.CREATED);
        } catch (PackageRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Adding package line failed: " + e.getMessage(), e);
            sendError(exchange, "Adding package line failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
