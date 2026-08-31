package ke.co.skyworld.internship.controllers.handlers.storagelocation;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.repository.StorageLocationRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

public class DeactivateStorageLocationHandler extends SkyInventoryManagementHttpHandler {

    private final StorageLocationRepository storageLocationRepository = new StorageLocationRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long id;
        try {
            id = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid storage location id", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            boolean deactivated = storageLocationRepository.deactivate(id);
            if (!deactivated) {
                sendError(exchange, "Storage location not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, java.util.Map.of("message", "Storage location deactivated"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Storage location deactivation failed: " + e.getMessage(), e);
            sendError(exchange, "Storage location deactivation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}