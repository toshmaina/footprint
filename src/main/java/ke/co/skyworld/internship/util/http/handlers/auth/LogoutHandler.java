package ke.co.skyworld.internship.util.http.handlers.auth;

import ke.co.skyworld.internship.domain.beans.LogoutRequest;
import ke.co.skyworld.internship.repository.TokenRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;

/**
 * Sits behind AuthMiddleware - requires a valid access token to call at all.
 * Revokes that access token, and also revokes the refresh token if the
 * caller supplies one in the body.
 * <p>
 * Known gap: access and refresh tokens aren't linked in the schema (no
 * shared session id), so a logout call that only has the access token
 * can't automatically find and revoke its paired refresh token - the
 * client is responsible for sending both if it wants a complete logout.
 * A "logout everywhere" (revoke all tokens for this user) is a separate,
 * not-yet-built feature.
 */
public class LogoutHandler extends SkyInventoryManagementHttpHandler {

    private final TokenRepository tokenRepository = new TokenRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String accessToken = getAuthToken(exchange);

        try {
            if (accessToken != null && !accessToken.isBlank()) {
                tokenRepository.revokeToken(accessToken);
            }

            // Body is optional - a client that only sends the access token
            // still gets a clean logout of that token.
            LogoutRequest request = tryParseBody(exchange);
            if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
                tokenRepository.revokeToken(request.getRefreshToken());
            }

            send(exchange, java.util.Map.of("message", "Logged out"), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Logout failed: " + e.getMessage(), e);
            sendError(exchange, "Logout failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private LogoutRequest tryParseBody(HttpServerExchange exchange) {
        try {
            return parseBody(exchange, LogoutRequest.class);
        } catch (Exception e) {
            return null; // no body, or malformed - logout still proceeds on the access token alone
        }
    }
}