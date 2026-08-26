package ke.co.skyworld.internship.util.http.handlers.advanceshippingnotice;



import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.advanceshippingnotice.AdvanceShippingNoticeResponse;
import ke.co.skyworld.internship.repository.AdvanceShippingNoticeRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Optional;



public class GetAdvanceShippingNoticeHandler extends SkyInventoryManagementHttpHandler {

    private final AdvanceShippingNoticeRepository asnRepository = new AdvanceShippingNoticeRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long asnId;
        try {
            asnId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid ASN id", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            Optional<AdvanceShippingNoticeResponse> asn = asnRepository.findByIdWithLines(asnId);
            if (asn.isEmpty()) {
                sendError(exchange, "ASN not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, asn.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching ASN failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching ASN failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}