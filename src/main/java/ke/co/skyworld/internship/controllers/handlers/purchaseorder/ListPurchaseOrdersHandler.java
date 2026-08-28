package ke.co.skyworld.internship.controllers.handlers.purchaseorder;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.domain.beans.purchaseorder.PurchaseOrderResponse;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.repository.PurchaseOrderRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

public class ListPurchaseOrdersHandler extends SkyInventoryManagementHttpHandler {

    private final PurchaseOrderRepository purchaseOrderRepository = new PurchaseOrderRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);

        try {
            PageResult<PurchaseOrderResponse> result =
                    purchaseOrderRepository.list(pageAndPageSize[0], pageAndPageSize[1]);
            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing purchase orders failed: " + e.getMessage(), e);
            sendError(exchange, "Listing purchase orders failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}