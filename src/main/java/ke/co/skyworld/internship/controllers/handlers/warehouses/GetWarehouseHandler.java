package ke.co.skyworld.internship.controllers.handlers.warehouses;


import ke.co.skyworld.internship.domain.beans.warehouse.WarehouseResponse;
import ke.co.skyworld.internship.repository.WarehouseRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Optional;

public class GetWarehouseHandler extends SkyInventoryManagementHttpHandler {

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
            Optional<WarehouseResponse> warehouse = warehouseRepository.findById(warehouseId);
            if (warehouse.isEmpty()) {
                sendError(exchange, "Warehouse not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, warehouse.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching warehouse failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching warehouse failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}