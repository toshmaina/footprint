package ke.co.skyworld.internship.util.http.handlers.advanceshippingnotice;



import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.PagedResponse;
import ke.co.skyworld.internship.domain.beans.advanceshippingnotice.AdvanceShippingNoticeResponse;
import ke.co.skyworld.internship.repository.AdvanceShippingNoticeRepository;
import ke.co.skyworld.internship.repository.PageResult;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;



public class ListAdvanceShippingNoticesHandler extends SkyInventoryManagementHttpHandler {

    private final AdvanceShippingNoticeRepository asnRepository = new AdvanceShippingNoticeRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        int[] pageAndPageSize = getPageAndPageSize(exchange);

        try {
            PageResult<AdvanceShippingNoticeResponse> result =
                    asnRepository.list(pageAndPageSize[0], pageAndPageSize[1]);
            send(exchange, new PagedResponse<>(result.items(), pageAndPageSize[0], pageAndPageSize[1],
                    result.totalCount()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Listing ASNs failed: " + e.getMessage(), e);
            sendError(exchange, "Listing ASNs failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}