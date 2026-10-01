package ke.co.skyworld.internship.controllers.handlers.returns;



import ke.co.skyworld.internship.domain.beans.returns.ProcessReturnLineRequest;
import ke.co.skyworld.internship.repository.ReturnRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Set;

/**
 * The rule-5 gap-closing endpoint: a human explicitly sets the condition
 * flag before a returned unit can re-enter the sellable pool (resellable)
 * or gets routed to quarantine (damaged). See ReturnRepository for how
 * this reuses the existing QA-pass/QA-fail LPN statuses and downstream
 * putaway machinery unmodified.
 */
public class ProcessReturnLineHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_CONDITIONS = Set.of("resellable", "damaged");

    private final ReturnRepository returnRepository = new ReturnRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long returnLineId;
        try {
            returnLineId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid return line id", StatusCodes.BAD_REQUEST);
            return;
        }

        ProcessReturnLineRequest request;
        try {
            request = parseBody(exchange, ProcessReturnLineRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getCondition() == null || !VALID_CONDITIONS.contains(request.getCondition())) {
            sendError(exchange, "condition is required and must be 'resellable' or 'damaged'", StatusCodes.BAD_REQUEST);
            return;
        }

        RequestContext context = currentUser(exchange);
        String processedBy = String.valueOf(context.userAccountId());

        try {
            long licensePlateId = returnRepository.processLine(returnLineId, request.getCondition(),
                    request.getLicensePlateCode(), processedBy);
            send(exchange, java.util.Map.of("licensePlateId", licensePlateId), StatusCodes.OK);
        } catch (ReturnRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "This license plate code is already in use", StatusCodes.CONFLICT);
                return;
            }
            Log.error(getClass(), "handleRequest", "Return line processing failed: " + e.getMessage(), e);
            sendError(exchange, "Return line processing failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
