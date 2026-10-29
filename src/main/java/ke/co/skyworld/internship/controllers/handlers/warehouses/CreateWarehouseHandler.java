package ke.co.skyworld.internship.controllers.handlers.warehouses;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.warehouse.WarehouseRequest;
import ke.co.skyworld.internship.repository.WarehouseRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Map;

public class CreateWarehouseHandler extends SkyInventoryManagementHttpHandler {

    private final WarehouseRepository warehouseRepository = new WarehouseRepository();

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        WarehouseRequest request;
        try {
            request = parseBody(exchange, WarehouseRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || isBlank(request.code()) || isBlank(request.name())) {
            sendError(exchange, "code and name are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            if (warehouseRepository.codeExists(request.code())) {
                sendError(exchange, "A warehouse with this code already exists", StatusCodes.CONFLICT);
                return;
            }
            long warehouseId = warehouseRepository.create(request.code(), request.name(), request.address());
            send(exchange, Map.of("warehouseId", warehouseId), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "A warehouse with this code already exists", StatusCodes.CONFLICT);
                return;
            }
            Log.error(getClass(), "handleRequest", "Warehouse creation failed: " + e.getMessage(), e);
            sendError(exchange, "Warehouse creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}