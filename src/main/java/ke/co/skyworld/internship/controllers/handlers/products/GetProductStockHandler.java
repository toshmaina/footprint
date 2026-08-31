package ke.co.skyworld.internship.controllers.handlers.products;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.stocksreport.StockReportResponse;
import ke.co.skyworld.internship.repository.StockReportRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * GET /products/{sku}/stock?warehouseId=&asOf=
 * The exact route your brief specifies in section 7's REST examples, and
 * the endpoint behind the "point-in-time stock report" required in
 * section 4 - "given a product and warehouse and a timestamp, show what
 * was available as of that moment."
 */
public class GetProductStockHandler extends SkyInventoryManagementHttpHandler {

    private final StockReportRepository stockReportRepository = new StockReportRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String sku = getPathVar(exchange, "sku");
        if (sku == null || sku.isBlank()) {
            sendError(exchange, "sku is required", StatusCodes.BAD_REQUEST);
            return;
        }

        Long warehouseId = getQueryParamLong(exchange, "warehouseId");
        if (warehouseId == null) {
            sendError(exchange, "warehouseId query parameter is required", StatusCodes.BAD_REQUEST);
            return;
        }

        Timestamp asOf;
        String asOfParam = getQueryParam(exchange, "asOf");
        if (asOfParam == null || asOfParam.isBlank()) {
            asOf = new Timestamp(System.currentTimeMillis());
        } else {
            try {
                asOf = Timestamp.valueOf(LocalDateTime.parse(asOfParam));
            } catch (DateTimeParseException e) {
                sendError(exchange, "asOf must be an ISO-8601 timestamp, e.g. 2026-08-20T14:30:00", StatusCodes.BAD_REQUEST);
                return;
            }
        }

        try {
            Optional<StockReportRepository.ProductLookup> product = stockReportRepository.findProductIdBySku(sku);
            if (product.isEmpty()) {
                sendError(exchange, "No product found with sku '" + sku + "'", StatusCodes.NOT_FOUND);
                return;
            }
            if (!stockReportRepository.warehouseExists(warehouseId)) {
                sendError(exchange, "warehouseId does not exist", StatusCodes.NOT_FOUND);
                return;
            }

            int available = stockReportRepository.getAvailableStock(product.get().productId(), warehouseId, asOf);

            send(exchange, new StockReportResponse(product.get().sku(), product.get().productId(), warehouseId,
                    asOf, available), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Stock report failed: " + e.getMessage(), e);
            sendError(exchange, "Stock report failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}