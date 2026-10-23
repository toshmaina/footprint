package ke.co.skyworld.internship.controllers.handlers.cyclecount;




import ke.co.skyworld.internship.domain.beans.count_cycles.CycleCountTaskRequest;
import ke.co.skyworld.internship.repository.CycleCountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class CreateCycleCountTaskHandler extends SkyInventoryManagementHttpHandler {

    private final CycleCountRepository cycleCountRepository = new CycleCountRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        CycleCountTaskRequest request;
        try {
            request = parseBody(exchange, CycleCountTaskRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getWarehouseId() == null || request.getStorageLocationId() == null) {
            sendError(exchange, "warehouseId and storageLocationId are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            long taskId = cycleCountRepository.createTaskWithSnapshot(request.getWarehouseId(),
                    request.getStorageLocationId(), request.getScheduledDate(), request.getAssignedTo());
            send(exchange, java.util.Map.of("taskId", taskId), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "warehouseId or storageLocationId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Task creation failed: " + e.getMessage(), e);
            sendError(exchange, "Task creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
