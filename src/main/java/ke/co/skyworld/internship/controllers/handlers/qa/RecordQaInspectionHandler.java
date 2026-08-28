package ke.co.skyworld.internship.controllers.handlers.qa;


import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.qainspection.QaInspectionRequest;
import ke.co.skyworld.internship.repository.QaRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;

import java.sql.SQLException;
import java.util.Set;

public class RecordQaInspectionHandler extends SkyInventoryManagementHttpHandler {

    private static final Set<String> VALID_DISPOSITIONS = Set.of("pass", "fail", "partial");
    private static final Set<String> VALID_METHODS = Set.of("visual", "measured", "tested");

    private final QaRepository qaRepository = new QaRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long licensePlateId;
        try {
            licensePlateId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid license plate id", StatusCodes.BAD_REQUEST);
            return;
        }

        QaInspectionRequest request;
        try {
            request = parseBody(exchange, QaInspectionRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getDisposition() == null
                || !VALID_DISPOSITIONS.contains(request.getDisposition())) {
            sendError(exchange, "disposition is required and must be one of pass, fail, partial", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request.getMethod() != null && !VALID_METHODS.contains(request.getMethod())) {
            sendError(exchange, "method must be one of visual, measured, tested", StatusCodes.BAD_REQUEST);
            return;
        }

        if ("partial".equals(request.getDisposition()) && request.getFailedQuantity() == null) {
            sendError(exchange, "failedQuantity is required for a partial disposition", StatusCodes.BAD_REQUEST);
            return;
        }

        RequestContext context = currentUser(exchange);
        String inspector = String.valueOf(context.userAccountId());

        try {
            QaRepository.QaOutcome outcome = qaRepository.recordInspectionWithResult(
                    licensePlateId, inspector, request.getSampleSize(), request.getMethod(),
                    request.getDisposition(), request.getFailedQuantity(), request.getNotes());

            send(exchange, java.util.Map.of(
                    "inspectionId", outcome.inspectionId(),
                    "resultId", outcome.resultId(),
                    "resultingLicensePlateIds", outcome.resultingLicensePlateIds()
            ), StatusCodes.CREATED);
        } catch (QaRepository.InvalidDispositionException e) {
            sendError(exchange, e.getMessage(), StatusCodes.CONFLICT);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "A license plate with one of the generated split codes already exists", StatusCodes.CONFLICT);
                return;
            }
            Log.error(getClass(), "handleRequest", "QA inspection failed: " + e.getMessage(), e);
            sendError(exchange, "QA inspection failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}