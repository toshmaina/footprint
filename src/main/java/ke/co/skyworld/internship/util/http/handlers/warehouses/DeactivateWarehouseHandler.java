package ke.co.skyworld.internship.util.http.handlers.warehouses;

import ke.co.skyworld.internship.repository.WarehouseRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class DeactivateWarehouseHandler extends SkyInventoryManagementHttpHandler {

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

        try {
            boolean deactivated = warehouseRepository.deactivate(warehouseId);
            if (!deactivated) {
                sendError(exchange, "Warehouse not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, java.util.Map.of("message", "Warehouse deactivated"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Warehouse deactivation failed: " + e.getMessage(), e);
            sendError(exchange, "Warehouse deactivation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}