
package ke.co.skyworld.internship.controllers.handlers.suppliers;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.supplier.SupplierRequest;
import ke.co.skyworld.internship.repository.SupplierRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;


public class CreateSupplierHandler extends SkyInventoryManagementHttpHandler {

    private final SupplierRepository supplierRepository = new SupplierRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        SupplierRequest request;
        try {
            request = parseBody(exchange, SupplierRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || isBlank(request.getName())) {
            sendError(exchange, "name is required", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request.getDefaultLeadTimeDays() != null && request.getDefaultLeadTimeDays() < 0) {
            sendError(exchange, "defaultLeadTimeDays cannot be negative", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            long supplierId = supplierRepository.create(request.getName(), request.getContactEmail(),
                    request.getContactPhone(), request.getDefaultLeadTimeDays());
            send(exchange, java.util.Map.of("supplierId", supplierId), StatusCodes.CREATED);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Supplier creation failed: " + e.getMessage(), e);
            sendError(exchange, "Supplier creation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}