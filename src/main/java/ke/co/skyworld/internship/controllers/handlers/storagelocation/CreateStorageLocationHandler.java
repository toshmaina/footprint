package ke.co.skyworld.internship.controllers.handlers.storagelocation;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.storagelocation.StorageLocationRequest;
import ke.co.skyworld.internship.repository.StorageLocationRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Set;

public class CreateStorageLocationHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_TYPES = Set.of(
            "receiving_dock", "qa_hold", "quarantine", "sellable_storage", "staging");

    private final StorageLocationRepository storageLocationRepository = new StorageLocationRepository();

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        StorageLocationRequest request;
        try {
            request = parseBody(exchange, StorageLocationRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getWarehouseId() == null || isBlank(request.getBinCode())
                || isBlank(request.getLocationType())) {
            sendError(exchange, "warehouseId, binCode, and locationType are required", StatusCodes.BAD_REQUEST);
            return;
        }
        if (!VALID_TYPES.contains(request.getLocationType())) {
            sendError(exchange, "locationType must be one of " + VALID_TYPES, StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            if (storageLocationRepository.binCodeExists(request.getWarehouseId(), request.getBinCode())) {
                sendError(exchange, "A storage location with this bin code already exists in this warehouse", StatusCodes.CONFLICT);
                return;
            }
            long id = storageLocationRepository.create(request.getWarehouseId(), request.getZone(),
                    request.getAisle(), request.getRack(), request.getBinCode(), request.getLocationType());
            send(exchange, java.util.Map.of("storageLocationId", id), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "A storage location with this bin code already exists in this warehouse", StatusCodes.CONFLICT);
                return;
            }
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "warehouseId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Storage location creation failed: " + e.getMessage(), e);
            sendError(exchange, "Storage location creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}