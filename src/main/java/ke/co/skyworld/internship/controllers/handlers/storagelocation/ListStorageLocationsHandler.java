package ke.co.skyworld.internship.controllers.handlers.storagelocation;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.domain.beans.storagelocation.StorageLocationResponse;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.repository.StorageLocationRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

public class ListStorageLocationsHandler extends SkyInventoryManagementHttpHandler {

    private final StorageLocationRepository storageLocationRepository = new StorageLocationRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);
        boolean includeInactive = "true".equalsIgnoreCase(getQueryParam(exchange, "includeInactive", "false"));
        Long warehouseId = getQueryParamLong(exchange, "warehouseId");

        try {
            PageResult<StorageLocationResponse> result = storageLocationRepository.list(
                    pageAndPageSize[0], pageAndPageSize[1], warehouseId, includeInactive);
            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing storage locations failed: " + e.getMessage(), e);
            sendError(exchange, "Listing storage locations failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}