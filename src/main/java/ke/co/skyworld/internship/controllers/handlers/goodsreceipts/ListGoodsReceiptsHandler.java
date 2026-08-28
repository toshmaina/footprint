package ke.co.skyworld.internship.controllers.handlers.goodsreceipts;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.domain.beans.goodsreceipt.GoodsReceiptResponse;
import ke.co.skyworld.internship.repository.GoodsReceiptRepository;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;

public class ListGoodsReceiptsHandler extends SkyInventoryManagementHttpHandler {

    private final GoodsReceiptRepository goodsReceiptRepository = new GoodsReceiptRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);
        try {
            PageResult<GoodsReceiptResponse> result = goodsReceiptRepository.list(pageAndPageSize[0], pageAndPageSize[1]);
            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing goods receipts failed: " + e.getMessage(), e);
            sendError(exchange, "Listing goods receipts failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
