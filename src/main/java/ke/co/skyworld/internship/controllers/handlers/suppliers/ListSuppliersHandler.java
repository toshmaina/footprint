package ke.co.skyworld.internship.controllers.handlers.suppliers;



import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.domain.beans.supplier.SupplierResponse;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.repository.SupplierRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;


public class ListSuppliersHandler extends SkyInventoryManagementHttpHandler {

    private final SupplierRepository supplierRepository = new SupplierRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);
        boolean includeInactive = "true".equalsIgnoreCase(getQueryParam(exchange, "includeInactive", "false"));

        try {
            PageResult<SupplierResponse> result =
                    supplierRepository.list(pageAndPageSize[0], pageAndPageSize[1], includeInactive);
            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing suppliers failed: " + e.getMessage(), e);
            sendError(exchange, "Listing suppliers failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}