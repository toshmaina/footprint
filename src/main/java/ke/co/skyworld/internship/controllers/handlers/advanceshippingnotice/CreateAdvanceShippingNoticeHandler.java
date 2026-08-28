package ke.co.skyworld.internship.controllers.handlers.advanceshippingnotice;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.advanceshippingnotice.AdvanceShippingNoticeRequest;
import ke.co.skyworld.internship.repository.AdvanceShippingNoticeRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Map;

public class CreateAdvanceShippingNoticeHandler extends SkyInventoryManagementHttpHandler {

    private final AdvanceShippingNoticeRepository asnRepository = new AdvanceShippingNoticeRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        AdvanceShippingNoticeRequest request;
        try {
            request = parseBody(exchange, AdvanceShippingNoticeRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getSupplierId() == null || request.getWarehouseId() == null
                || request.getExpectedArrival() == null) {
            sendError(exchange, "supplierId, warehouseId, and expectedArrival are required", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request.getLines() == null || request.getLines().isEmpty()) {
            sendError(exchange, "An ASN must have at least one line", StatusCodes.BAD_REQUEST);
            return;
        }

        for (AdvanceShippingNoticeRequest.Line line : request.getLines()) {
            if (line.getProductId() == null || line.getQuantityExpected() == null) {
                sendError(exchange, "Each line requires productId and quantityExpected", StatusCodes.BAD_REQUEST);
                return;
            }
            if (line.getQuantityExpected() <= 0) {
                sendError(exchange, "quantityExpected must be positive", StatusCodes.BAD_REQUEST);
                return;
            }
        }

        try {
            long asnId = asnRepository.createWithLines(request.getPurchaseOrderId(), request.getSupplierId(),
                    request.getWarehouseId(), request.getCarrier(), request.getExpectedArrival(), request.getLines());
            send(exchange, Map.of("advanceShippingNoticeId", asnId), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23503".equals(e.getSQLState())) {
                sendError(exchange, "purchaseOrderId, supplierId, warehouseId, or a line's productId does not exist",
                        StatusCodes.BAD_REQUEST);
                return;
            }
            Log.error(getClass(), "handleRequest", "ASN creation failed: " + e.getMessage(), e);
            sendError(exchange, "ASN creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}