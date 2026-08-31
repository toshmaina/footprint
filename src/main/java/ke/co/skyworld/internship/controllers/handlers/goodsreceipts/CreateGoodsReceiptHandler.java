package ke.co.skyworld.internship.controllers.handlers.goodsreceipts;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.goodsreceipt.GoodsReceiptRequest;
import ke.co.skyworld.internship.repository.GoodsReceiptRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;


public class CreateGoodsReceiptHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_CONDITIONS = Set.of("unverified", "damaged_visible", "ok_visible");

    private final GoodsReceiptRepository goodsReceiptRepository = new GoodsReceiptRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        GoodsReceiptRequest request;
        try {
            request = parseBody(exchange, GoodsReceiptRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getWarehouseId() == null) {
            sendError(exchange, "warehouseId is required", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request.getLines() == null || request.getLines().isEmpty()) {
            sendError(exchange, "A goods receipt must have at least one line", StatusCodes.BAD_REQUEST);
            return;
        }
        for (GoodsReceiptRequest.Line line : request.getLines()) {
            if (line.getProductId() == null || line.getQuantityCounted() == null) {
                sendError(exchange, "Each line requires productId and quantityCounted", StatusCodes.BAD_REQUEST);
                return;
            }
            if (line.getQuantityCounted() < 0) {
                sendError(exchange, "quantityCounted cannot be negative", StatusCodes.BAD_REQUEST);
                return;
            }
            if (line.getConditionFlag() != null && !VALID_CONDITIONS.contains(line.getConditionFlag())) {
                sendError(exchange, "conditionFlag must be one of unverified, damaged_visible, ok_visible", StatusCodes.BAD_REQUEST);
                return;
            }
        }

        RequestContext context = currentUser(exchange);
        String receivedBy = String.valueOf(context.userAccountId());

        try {
            long goodsReceiptId = goodsReceiptRepository.createWithLines(
                    request.getAdvanceShippingNoticeId(), request.getDockAppointmentId(),
                    request.getWarehouseId(), receivedBy, request.getLines());
            send(exchange, Map.of("goodsReceiptId", goodsReceiptId), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "warehouseId, advanceShippingNoticeId, or a line's productId/purchaseOrderLineId does not exist", StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "Goods receipt creation failed: " + e.getMessage(), e);
            sendError(exchange, "Goods receipt creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
