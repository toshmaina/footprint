package ke.co.skyworld.internship.controllers.handlers.returns;



import ke.co.skyworld.internship.domain.beans.returns.ReturnResponse;
import ke.co.skyworld.internship.repository.ReturnRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;


import java.sql.SQLException;
import java.util.Optional;

public class GetReturnHandler extends SkyInventoryManagementHttpHandler {

    private final ReturnRepository returnRepository = new ReturnRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        long returnId;
        try {
            returnId = Long.parseLong(getPathVar(exchange, "id"));
        } catch (NumberFormatException e) {
            sendError(exchange, "Invalid return id", StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            Optional<ReturnResponse> ret = returnRepository.findByIdWithLines(returnId);
            if (ret.isEmpty()) {
                sendError(exchange, "Return not found", StatusCodes.NOT_FOUND);
                return;
            }
            send(exchange, ret.get(), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Fetching return failed: " + e.getMessage(), e);
            sendError(exchange, "Fetching return failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}
