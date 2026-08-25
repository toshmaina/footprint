package ke.co.skyworld.internship.util.http.handlers.suppliers;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.supplier.SupplierResponse;
import ke.co.skyworld.internship.repository.SupplierRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Optional;

public class GetSupplierHandler extends SkyInventoryManagementHttpHandler {

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
            Optional<SupplierResponse> supplier = supplierRepository.findById(supplierId);
            if (supplier.isEmpty()) {
                sendError(exchange, "Supplier not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, supplier.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching supplier failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching supplier failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
