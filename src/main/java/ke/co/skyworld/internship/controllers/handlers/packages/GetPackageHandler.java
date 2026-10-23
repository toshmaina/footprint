package ke.co.skyworld.internship.controllers.handlers.packages;




import ke.co.skyworld.internship.domain.beans.packages.PackageResponse;
import ke.co.skyworld.internship.repository.PackageRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Optional;

public class GetPackageHandler extends SkyInventoryManagementHttpHandler {

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
        try {
            Optional<PackageResponse> pkg = packageRepository.findByIdWithLines(packageId);
            if (pkg.isEmpty()) {
                sendError(exchange, "Package not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, pkg.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching package failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching package failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}