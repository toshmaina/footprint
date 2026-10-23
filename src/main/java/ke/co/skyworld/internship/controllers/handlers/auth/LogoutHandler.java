package ke.co.skyworld.internship.controllers.handlers.auth;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.auth.LogoutRequest;
import ke.co.skyworld.internship.repository.TokenRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;

import java.sql.SQLException;
import java.util.Map;

public class LogoutHandler extends SkyInventoryManagementHttpHandler {

    private final TokenRepository tokenRepository = new TokenRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String accessToken = getAuthToken(exchange);

        try {
                    if (accessToken == null || accessToken.isBlank()) {
                           sendError(exchange, "Access token is required", StatusCodes.UNAUTHORIZED);
                            return;
                       }
            LogoutRequest request = tryParseBody(exchange);
            if (request != null && request.refreshToken() != null && !request.refreshToken().isBlank()) {
                tokenRepository.revokeToken(request.refreshToken());
            }

            send(exchange, Map.of("message", "Logged out"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Logout failed: " + e.getMessage(), e);
            sendError(exchange, "Logout failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private LogoutRequest tryParseBody(HttpServerExchange exchange) {
        try {
            return parseBody(exchange, LogoutRequest.class);
        } catch (Exception e) {
            return null;
        }
    }
}