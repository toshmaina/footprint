package ke.co.skyworld.internship.controllers.handlers.picktask;


import ke.co.skyworld.internship.domain.beans.picktask.PickTaskRequest;
import ke.co.skyworld.internship.repository.PickTaskRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class CreatePickTaskHandler extends SkyInventoryManagementHttpHandler {

    private final PickTaskRepository pickTaskRepository = new PickTaskRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long pickWaveId;
        try {
            pickWaveId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid pick wave id", StatusCodes.BAD_REQUEST);
            return;
        }

        PickTaskRequest request;
        try {
            request = parseBody(exchange, PickTaskRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getOrderLineId() == null || request.getLicensePlateId() == null) {
            sendError(exchange, "orderLineId and licensePlateId are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            long taskId = pickTaskRepository.createTask(pickWaveId, request.getOrderLineId(),
                    request.getLicensePlateId(), request.getAssignedTo());
            send(exchange, java.util.Map.of("pickTaskId", taskId), StatusCodes.CREATED);
        } catch (PickTaskRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "This order line is already assigned to this pick wave", StatusCodes.CONFLICT);
                return;
            }
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "pickWaveId, orderLineId, or licensePlateId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Pick task creation failed: " + e.getMessage(), e);
            sendError(exchange, "Pick task creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}