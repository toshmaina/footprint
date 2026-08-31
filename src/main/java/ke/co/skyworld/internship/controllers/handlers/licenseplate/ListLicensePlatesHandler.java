package ke.co.skyworld.internship.controllers.handlers.licenseplate;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.domain.beans.licenseplates.LicensePlateResponse;
import ke.co.skyworld.internship.repository.LicensePlateRepository;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Set;

public class ListLicensePlatesHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_STATUSES = Set.of(
            "receiving", "qa_hold", "putaway_pending", "stored", "quarantined", "consumed");

    private final LicensePlateRepository licensePlateRepository = new LicensePlateRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);
        Long warehouseId = getQueryParamLong(exchange, "warehouseId");
        String status = getQueryParam(exchange, "status");

        if (status != null && !VALID_STATUSES.contains(status)) {
            sendError(exchange, "status must be one of " + VALID_STATUSES, StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            PageResult<LicensePlateResponse> result = licensePlateRepository.list(
                    pageAndPageSize[0], pageAndPageSize[1], warehouseId, status);
            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing license plates failed: " + e.getMessage(), e);
            sendError(exchange, "Listing license plates failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
