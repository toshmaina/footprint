package ke.co.skyworld.internship.controllers.handlers.warehouses;

import ke.co.skyworld.internship.domain.beans.warehouse.WarehouseRequest;
import ke.co.skyworld.internship.repository.WarehouseRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class UpdateWarehouseHandler extends SkyInventoryManagementHttpHandler {

    private final WarehouseRepository warehouseRepository = new WarehouseRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long warehouseId;
        try {
            warehouseId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid warehouse id", StatusCodes.BAD_REQUEST);
            return;
        }

        WarehouseRequest request;
        try {
            request = parseBody(exchange, WarehouseRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || isBlank(request.getName())) {
            sendError(exchange, "name is required", StatusCodes.BAD_REQUEST);
            return;
        }

        // Code is intentionally not updatable here, same reasoning as
        // product SKU - it's a stable external identifier used elsewhere.
        try {
            boolean updated = warehouseRepository.update(warehouseId, request.getName(), request.getAddress());
            if (!updated) {
                sendError(exchange, "Warehouse not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, java.util.Map.of("message", "Warehouse updated"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Warehouse update failed: " + e.getMessage(), e);
            sendError(exchange, "Warehouse update failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}