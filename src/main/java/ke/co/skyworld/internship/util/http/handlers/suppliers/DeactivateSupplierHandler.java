package ke.co.skyworld.internship.util.http.handlers.suppliers;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.repository.SupplierRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Map;

public class DeactivateSupplierHandler extends SkyInventoryManagementHttpHandler {

    private final SupplierRepository supplierRepository = new SupplierRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long supplierId;
        try {
            supplierId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid supplier id", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            boolean deactivated = supplierRepository.deactivate(supplierId);
            if (!deactivated) {
                sendError(exchange, "Supplier not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, Map.of("message", "Supplier deactivated"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Supplier deactivation failed: " + e.getMessage(), e);
            sendError(exchange, "Supplier deactivation failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}