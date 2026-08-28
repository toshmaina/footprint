package ke.co.skyworld.internship.controllers.handlers.licenseplate;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.licenseplates.CreateLicensePlatesRequest;
import ke.co.skyworld.internship.repository.LicensePlateRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class CreateLicensePlatesHandler extends SkyInventoryManagementHttpHandler {

    private final LicensePlateRepository licensePlateRepository = new LicensePlateRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long goodsReceiptLineId;
        try {
            goodsReceiptLineId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid goods receipt line id", StatusCodes.BAD_REQUEST);
            return;
        }

        CreateLicensePlatesRequest request;
        try {
            request = parseBody(exchange, CreateLicensePlatesRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getPlates() == null || request.getPlates().isEmpty()) {
            sendError(exchange, "At least one plate is required", StatusCodes.BAD_REQUEST);
            return;
        }
        for (CreateLicensePlatesRequest.Plate plate : request.getPlates()) {
            if (plate.getCode() == null || plate.getCode().isBlank() || plate.getQuantity() == null
                    || plate.getQuantity() <= 0) {
                sendError(exchange, "Each plate requires a non-blank code and a positive quantity", StatusCodes.BAD_REQUEST);
                return;
            }
        }

        try {
            List<Long> ids = licensePlateRepository.createFromReceiptLine(goodsReceiptLineId, request.getPlates());
            send(exchange, Map.of("licensePlateIds", ids), StatusCodes.CREATED);
        } catch (LicensePlateRepository.LicensePlateQuantityExceededException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "One of the license plate codes is already in use", StatusCodes.CONFLICT);
                return;
            }
            Log.error(getClass(), "handleRequest", "License plate creation failed: " + e.getMessage(), e);
            sendError(exchange, "License plate creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
