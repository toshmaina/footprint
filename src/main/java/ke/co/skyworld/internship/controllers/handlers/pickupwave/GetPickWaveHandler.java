package ke.co.skyworld.internship.controllers.handlers.pickupwave;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.pickupwave.PickWaveResponse;
import ke.co.skyworld.internship.repository.PickWaveRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Optional;

public class GetPickWaveHandler extends SkyInventoryManagementHttpHandler {

    private final PickWaveRepository pickWaveRepository = new PickWaveRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long id;
        try {
            id = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid pick wave id", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            Optional<PickWaveResponse> wave = pickWaveRepository.findByIdWithTasks(id);
            if (wave.isEmpty()) {
                sendError(exchange, "Pick wave not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, wave.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching pick wave failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching pick wave failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
