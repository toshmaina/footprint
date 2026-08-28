package ke.co.skyworld.internship.controllers.handlers.putaway;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.putawayconfirmation.PutawayConfirmationRequest;
import ke.co.skyworld.internship.repository.PutawayRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;

import java.sql.SQLException;

public class ConfirmPutawayHandler extends SkyInventoryManagementHttpHandler {

    private final PutawayRepository putawayRepository = new PutawayRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long putawayTaskId;
        try {
            putawayTaskId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid putaway task id", StatusCodes.BAD_REQUEST);
            return;
        }

        PutawayConfirmationRequest request;
        try {
            request = parseBody(exchange, PutawayConfirmationRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getActualStorageLocationId() == null) {
            sendError(exchange, "actualStorageLocationId is required", StatusCodes.BAD_REQUEST);
            return;
        }

        RequestContext context = currentUser(exchange);
        String confirmedBy = String.valueOf(context.userAccountId());

        try {
            putawayRepository.confirmTask(putawayTaskId, request.getActualStorageLocationId(), confirmedBy);
            send(exchange, java.util.Map.of("message", "Putaway confirmed"), StatusCodes.OK);
        } catch (PutawayRepository.InvalidStateException e) {
            // Covers both "already confirmed" (the double-confirmation case
            // the row lock + unique constraint exist to prevent) and
            // "task not found" / "cancelled".
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                // Backstop path: reached only if two confirmations somehow
                // got past the row lock (see PutawayRepository's comment).
                sendError(exchange, "This putaway task was already confirmed", StatusCodes.CONFLICT);
                return;
            }
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "actualStorageLocationId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Putaway confirmation failed: " + e.getMessage(), e);
            sendError(exchange, "Putaway confirmation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}