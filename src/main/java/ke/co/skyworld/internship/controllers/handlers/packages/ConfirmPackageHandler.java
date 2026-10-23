package ke.co.skyworld.internship.controllers.handlers.packages;




import ke.co.skyworld.internship.domain.beans.packages.PackageConfirmRequest;
import ke.co.skyworld.internship.repository.PackageRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class ConfirmPackageHandler extends SkyInventoryManagementHttpHandler {

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

        PackageConfirmRequest request;
        try {
            request = parseBody(exchange, PackageConfirmRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            boolean confirmed = packageRepository.confirmPack(packageId, request != null ? request.getWeight() : null);
            if (!confirmed) {
                sendError(exchange, "Package not found, or not in 'packing' status", StatusCodes.CONFLICT);
                return;
            }
            send(exchange, java.util.Map.of("message", "Package confirmed as packed"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Package confirmation failed: " + e.getMessage(), e);
            sendError(exchange, "Package confirmation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}