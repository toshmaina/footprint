package ke.co.skyworld.internship.controllers.handlers.picktask;



import ke.co.skyworld.internship.domain.beans.picktask.PickConfirmationRequest;
import ke.co.skyworld.internship.repository.PickTaskRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

/**
 * Same double-guard concurrency pattern as ConfirmPutawayHandler: row lock
 * in PickTaskRepository.confirmTask() plus the UNIQUE(pick_task_id)
 * constraint on pick_confirmations as backstop.
 */
public class ConfirmPickHandler extends SkyInventoryManagementHttpHandler {

    private final PickTaskRepository pickTaskRepository = new PickTaskRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long pickTaskId;
        try {
            pickTaskId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid pick task id", StatusCodes.BAD_REQUEST);
            return;
        }

        PickConfirmationRequest request;
        try {
            request = parseBody(exchange, PickConfirmationRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getQuantityPicked() == null || request.getQuantityPicked() < 0) {
            sendError(exchange, "quantityPicked is required and cannot be negative", StatusCodes.BAD_REQUEST);
            return;
        }

        RequestContext context = currentUser(exchange);
        String confirmedBy = String.valueOf(context.userAccountId());

        try {
            pickTaskRepository.confirmTask(pickTaskId, request.getQuantityPicked(), confirmedBy);
            send(exchange, java.util.Map.of("message", "Pick confirmed"), StatusCodes.OK);
        } catch (PickTaskRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "This pick task was already confirmed", StatusCodes.CONFLICT);
                return;
            }
            Log.error(getClass(), "handleRequest", "Pick confirmation failed: " + e.getMessage(), e);
            sendError(exchange, "Pick confirmation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}