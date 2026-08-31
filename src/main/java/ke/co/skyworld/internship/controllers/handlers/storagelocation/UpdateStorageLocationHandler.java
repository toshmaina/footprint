package ke.co.skyworld.internship.controllers.handlers.storagelocation;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.storagelocation.StorageLocationRequest;
import ke.co.skyworld.internship.repository.StorageLocationRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Set;

public class UpdateStorageLocationHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_TYPES = Set.of(
            "receiving_dock", "qa_hold", "quarantine", "sellable_storage", "staging");

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

        StorageLocationRequest request;
        try {
            request = parseBody(exchange, StorageLocationRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getLocationType() == null || !VALID_TYPES.contains(request.getLocationType())) {
            sendError(exchange, "locationType is required and must be one of " + VALID_TYPES, StatusCodes.BAD_REQUEST);
            return;
        }

        // warehouseId and binCode are intentionally not updatable - same
        // "stable external identifier" reasoning as product SKU / warehouse code.
        try {
            boolean updated = storageLocationRepository.update(id, request.getZone(), request.getAisle(),
                    request.getRack(), request.getLocationType());
            if (!updated) {
                sendError(exchange, "Storage location not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, java.util.Map.of("message", "Storage location updated"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Storage location update failed: " + e.getMessage(), e);
            sendError(exchange, "Storage location update failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}