package ke.co.skyworld.internship.controllers.handlers.cyclecount;

import ke.co.skyworld.internship.domain.beans.count_cycles.CycleCountScheduleRequest;
import ke.co.skyworld.internship.repository.CycleCountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Set;

public class CreateCycleCountScheduleHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_CLASSIFICATIONS = Set.of("A", "B", "C");

    private final CycleCountRepository cycleCountRepository = new CycleCountRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        CycleCountScheduleRequest request;
        try {
            request = parseBody(exchange, CycleCountScheduleRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getWarehouseId() == null || request.getProductClassification() == null
                || !VALID_CLASSIFICATIONS.contains(request.getProductClassification())
                || request.getCountFrequencyDays() == null || request.getCountFrequencyDays() <= 0) {
            sendError(exchange, "warehouseId, productClassification (A/B/C), and a positive countFrequencyDays are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            long id = cycleCountRepository.createSchedule(request.getWarehouseId(), request.getProductClassification(),
                    request.getCountFrequencyDays());
            send(exchange, java.util.Map.of("scheduleId", id), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "A schedule already exists for this warehouse and classification", StatusCodes.CONFLICT);
                return;
            }
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "warehouseId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Schedule creation failed: " + e.getMessage(), e);
            sendError(exchange, "Schedule creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
