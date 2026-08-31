package ke.co.skyworld.internship.controllers.handlers.licenseplate;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.licenseplates.LicensePlateResponse;
import ke.co.skyworld.internship.repository.LicensePlateRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;


import java.sql.SQLException;
import java.util.Optional;

public class GetLicensePlateHandler extends SkyInventoryManagementHttpHandler {

    private final LicensePlateRepository licensePlateRepository = new LicensePlateRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long licensePlateId;
        try {
            licensePlateId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid license plate id", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            Optional<LicensePlateResponse> plate = licensePlateRepository.findById(licensePlateId);
            if (plate.isEmpty()) {
                sendError(exchange, "License plate not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, plate.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching license plate failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching license plate failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}

