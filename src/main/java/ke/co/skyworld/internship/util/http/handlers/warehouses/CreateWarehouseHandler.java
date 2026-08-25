package ke.co.skyworld.internship.util.http.handlers.warehouses;


import ke.co.skyworld.internship.domain.beans.warehouse.WarehouseRequest;
import ke.co.skyworld.internship.repository.WarehouseRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class CreateWarehouseHandler extends SkyInventoryManagementHttpHandler {

    private final WarehouseRepository warehouseRepository = new WarehouseRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        WarehouseRequest request;
        try {
            request = parseBody(exchange, WarehouseRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || isBlank(request.getCode()) || isBlank(request.getName())) {
            sendError(exchange, "code and name are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            if (warehouseRepository.codeExists(request.getCode())) {
                sendError(exchange, "A warehouse with this code already exists", StatusCodes.CONFLICT);
                return;
            }
            long warehouseId = warehouseRepository.create(request.getCode(), request.getName(), request.getAddress());
            send(exchange, java.util.Map.of("warehouseId", warehouseId), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "A warehouse with this code already exists", StatusCodes.CONFLICT);
                return;
            }
            Log.error(getClass(), "handleRequest", "Warehouse creation failed: " + e.getMessage(), e);
            sendError(exchange, "Warehouse creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}