package ke.co.skyworld.internship.controllers.handlers.goodsdiscrepancy;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.discrepancyresolution.DiscrepancyResolutionRequest;
import ke.co.skyworld.internship.repository.DiscrepancyRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;

import java.sql.SQLException;
import java.util.Set;

public class ResolveDiscrepancyHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_STATUSES = Set.of("resolved_accepted", "resolved_rejected");

    private final DiscrepancyRepository discrepancyRepository = new DiscrepancyRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long discrepancyId;
        try {
            discrepancyId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid discrepancy id", StatusCodes.BAD_REQUEST);
            return;
        }

        DiscrepancyResolutionRequest request;
        try {
            request = parseBody(exchange, DiscrepancyResolutionRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getStatus() == null || !VALID_STATUSES.contains(request.getStatus())) {
            sendError(exchange, "status is required and must be resolved_accepted or resolved_rejected", StatusCodes.BAD_REQUEST);
            return;
        }

        RequestContext context = currentUser(exchange);
        String resolvedBy = String.valueOf(context.userAccountId());

        try {
            boolean resolved = discrepancyRepository.resolve(discrepancyId, request.getStatus(), resolvedBy, request.getNotes());
            if (!resolved) {
                sendError(exchange, "Discrepancy not found, or already resolved", StatusCodes.CONFLICT);
                return;
            }
            send(exchange, java.util.Map.of("message", "Discrepancy resolved"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Discrepancy resolution failed: " + e.getMessage(), e);
            sendError(exchange, "Discrepancy resolution failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
