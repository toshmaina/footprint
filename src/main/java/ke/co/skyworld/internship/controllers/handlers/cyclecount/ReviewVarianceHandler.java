package ke.co.skyworld.internship.controllers.handlers.cyclecount;

import ke.co.skyworld.internship.domain.beans.count_cycles.VarianceReviewRequest;
import ke.co.skyworld.internship.repository.CycleCountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Set;

/**
 * The approval gate - only 'approved' with a nonzero variance actually
 * posts to the ledger (inventory_adjustments + stock_movements). See
 * CycleCountRepository.reviewResultLine() for the full posting logic.
 */
public class ReviewVarianceHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_DECISIONS = Set.of("approved", "rejected", "recount_required");

    private final CycleCountRepository cycleCountRepository = new CycleCountRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long resultLineId;
        try {
            resultLineId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid result line id", StatusCodes.BAD_REQUEST);
            return;
        }

        VarianceReviewRequest request;
        try {
            request = parseBody(exchange, VarianceReviewRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getDecision() == null || !VALID_DECISIONS.contains(request.getDecision())) {
            sendError(exchange, "decision is required and must be approved, rejected, or recount_required", StatusCodes.BAD_REQUEST);
            return;
        }

        RequestContext context = currentUser(exchange);
        String reviewedBy = String.valueOf(context.userAccountId());

        try {
            long reviewId = cycleCountRepository.reviewResultLine(resultLineId, request.getDecision(), reviewedBy);
            send(exchange, java.util.Map.of("reviewId", reviewId), StatusCodes.CREATED);
        } catch (CycleCountRepository.InvalidStateException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Variance review failed: " + e.getMessage(), e);
            sendError(exchange, "Variance review failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}