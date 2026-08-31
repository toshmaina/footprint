package ke.co.skyworld.internship.controllers.handlers.warehouses;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.domain.beans.warehouse.WarehouseResponse;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.repository.WarehouseRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

public class ListWarehousesHandler extends SkyInventoryManagementHttpHandler {

    private final WarehouseRepository warehouseRepository = new WarehouseRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);
        boolean includeInactive = "true".equalsIgnoreCase(getQueryParam(exchange, "includeInactive", "false"));

        try {
            PageResult<WarehouseResponse> result =
                    warehouseRepository.list(pageAndPageSize[0], pageAndPageSize[1], includeInactive);
            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing warehouses failed: " + e.getMessage(), e);
            sendError(exchange, "Listing warehouses failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}