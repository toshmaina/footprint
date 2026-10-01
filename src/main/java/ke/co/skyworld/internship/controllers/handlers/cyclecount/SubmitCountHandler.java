package ke.co.skyworld.internship.controllers.handlers.cyclecount;




import ke.co.skyworld.internship.domain.beans.count_cycles.SubmitCountRequest;
import ke.co.skyworld.internship.repository.CycleCountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Map;

/**
 * Always inserts a new result_line - see CycleCountRepository.submitCount()
 * for why this is what makes recounts work without extra machinery.
 */
public class SubmitCountHandler extends SkyInventoryManagementHttpHandler {

    private final CycleCountRepository cycleCountRepository = new CycleCountRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long taskLineId;
        try {
            taskLineId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid task line id", StatusCodes.BAD_REQUEST);
            return;
        }

        SubmitCountRequest request;
        try {
            request = parseBody(exchange, SubmitCountRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getCountedQuantity() == null || request.getCountedQuantity() < 0) {
            sendError(exchange, "countedQuantity is required and cannot be negative", StatusCodes.BAD_REQUEST);
            return;
        }

        RequestContext context = currentUser(exchange);
        String countedBy = String.valueOf(context.userAccountId());

        try {
            long resultLineId = cycleCountRepository.submitCount(taskLineId, request.getCountedQuantity(), countedBy);
            send(exchange, Map.of("resultLineId", resultLineId), StatusCodes.CREATED);
        } catch (CycleCountRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Submitting count failed: " + e.getMessage(), e);
            sendError(exchange, "Submitting count failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
