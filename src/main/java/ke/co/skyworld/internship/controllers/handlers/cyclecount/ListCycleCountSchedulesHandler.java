package ke.co.skyworld.internship.controllers.handlers.cyclecount;

import ke.co.skyworld.internship.repository.CycleCountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

public class ListCycleCountSchedulesHandler extends SkyInventoryManagementHttpHandler {

    private final CycleCountRepository cycleCountRepository = new CycleCountRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        Long warehouseId = getQueryParamLong(exchange, "warehouseId");
        try {
            send(exchange, cycleCountRepository.listSchedules(warehouseId), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing schedules failed: " + e.getMessage(), e);
            sendError(exchange, "Listing schedules failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
