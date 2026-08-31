package ke.co.skyworld.internship.controllers.handlers.putaway;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.putawaytask.PutawayTaskRequest;
import ke.co.skyworld.internship.repository.PutawayRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;


public class CreatePutawayTaskHandler extends SkyInventoryManagementHttpHandler {

    private final PutawayRepository putawayRepository = new PutawayRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long licensePlateId;
        try {
            licensePlateId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid license plate id", StatusCodes.BAD_REQUEST);
            return;
        }

        PutawayTaskRequest request;
        try {
            request = parseBody(exchange, PutawayTaskRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getSuggestedStorageLocationId() == null) {
            sendError(exchange, "suggestedStorageLocationId is required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            long taskId = putawayRepository.createTask(licensePlateId, request.getSuggestedStorageLocationId(),
                    request.getAssignedTo());
            send(exchange, java.util.Map.of("putawayTaskId", taskId), StatusCodes.CREATED);
        } catch (PutawayRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "suggestedStorageLocationId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Putaway task creation failed: " + e.getMessage(), e);
            sendError(exchange, "Putaway task creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}