package ke.co.skyworld.internship.controllers.handlers.pickupwave;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.pickupwave.PickWaveRequest;
import ke.co.skyworld.internship.repository.PickWaveRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

public class CreatePickWaveHandler extends SkyInventoryManagementHttpHandler {

    private final PickWaveRepository pickWaveRepository = new PickWaveRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        PickWaveRequest request;
        try {
            request = parseBody(exchange, PickWaveRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request == null || request.getWarehouseId() == null) {
            sendError(exchange, "warehouseId is required", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            long id = pickWaveRepository.create(request.getWarehouseId(), request.getCutoffTime());
            send(exchange, java.util.Map.of("pickWaveId", id), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "warehouseId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Pick wave creation failed: " + e.getMessage(), e);
            sendError(exchange, "Pick wave creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}