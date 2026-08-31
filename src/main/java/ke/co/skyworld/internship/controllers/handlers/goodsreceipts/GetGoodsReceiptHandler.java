package ke.co.skyworld.internship.controllers.handlers.goodsreceipts;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.goodsreceipt.GoodsReceiptResponse;
import ke.co.skyworld.internship.repository.GoodsReceiptRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Optional;


public class GetGoodsReceiptHandler extends SkyInventoryManagementHttpHandler {

    private final GoodsReceiptRepository goodsReceiptRepository = new GoodsReceiptRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long goodsReceiptId;
        try {
            goodsReceiptId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid goods receipt id", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            Optional<GoodsReceiptResponse> receipt = goodsReceiptRepository.findByIdWithLines(goodsReceiptId);
            if (receipt.isEmpty()) {
                sendError(exchange, "Goods receipt not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, receipt.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching goods receipt failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching goods receipt failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
