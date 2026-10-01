package ke.co.skyworld.internship.controllers.handlers.cyclecount;



import ke.co.skyworld.internship.domain.beans.count_cycles.CycleCountTaskResponse;
import ke.co.skyworld.internship.repository.CycleCountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Optional;

public class GetCycleCountTaskHandler extends SkyInventoryManagementHttpHandler {

    private final CycleCountRepository cycleCountRepository = new CycleCountRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long taskId;
        try {
            taskId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid task id", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            Optional<CycleCountTaskResponse> task = cycleCountRepository.findTaskByIdWithLines(taskId);
            if (task.isEmpty()) {
                sendError(exchange, "Cycle count task not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, task.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching task failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching task failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
