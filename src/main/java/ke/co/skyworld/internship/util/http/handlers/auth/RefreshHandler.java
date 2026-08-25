package ke.co.skyworld.internship.util.http.handlers.auth;

import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.domain.beans.LoginResponse;
import ke.co.skyworld.internship.domain.beans.RefreshRequest;
import ke.co.skyworld.internship.domain.enums.TokenType;
import ke.co.skyworld.internship.repository.TokenRepository;
import ke.co.skyworld.internship.repository.UserAccountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.TokenTtl;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.time.Duration;

/**
 * Trades a valid refresh token for a new access+refresh pair. Rotates the
 * refresh token on every use - the old one is revoked and a new one issued,
 * rather than reusing the same refresh token indefinitely. This makes a
 * stolen-and-replayed refresh token detectable (it'll fail once the
 * legitimate client has already rotated past it), even though the alerting
 * for that scenario isn't built here.
 */
public class RefreshHandler extends SkyInventoryManagementHttpHandler {

    private final TokenRepository tokenRepository = new TokenRepository();
    private final UserAccountRepository userAccountRepository = new UserAccountRepository();
    private static final int REFRESH_TOKEN_LENGTH = Constants.getRefreshTokenLength();
    private static final int ACCESS_TOKEN_LENGTH = Constants.getAccessTokenLength();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        RefreshRequest request;
        try {
            request = parseBody(exchange, RefreshRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            sendError(exchange, "refreshToken is required", StatusCodes.BAD_REQUEST);
            return;
        }

        String suppliedRefreshToken = request.getRefreshToken();

        try {
            Long userAccountId = tokenRepository.getUserIdByToken(TokenType.REFRESH, suppliedRefreshToken);
            if (userAccountId == null) {
                sendError(exchange, "Invalid or expired refresh token", StatusCodes.UNAUTHORIZED);
                return;
            }

            UserAccountRepository.AuthSnapshot snapshot = userAccountRepository.findAuthSnapshot(userAccountId);
            if (snapshot == null || !"active".equalsIgnoreCase(snapshot.status())) {
                sendError(exchange, "This account cannot refresh a session", StatusCodes.FORBIDDEN);
                return;
            }

            // Rotate: revoke the presented refresh token, issue a fresh pair.
            tokenRepository.revokeToken(suppliedRefreshToken);

            Duration accessTtl = TokenTtl.accessTokenTtl();
            Duration refreshTtl = TokenTtl.refreshTokenTtl();

            String newAccessToken = tokenRepository.issueToken(userAccountId, TokenType.ACCESS, accessTtl,ACCESS_TOKEN_LENGTH);
            String newRefreshToken = tokenRepository.issueToken(userAccountId, TokenType.REFRESH, refreshTtl, REFRESH_TOKEN_LENGTH);

            send(exchange, new LoginResponse(newAccessToken, newRefreshToken, accessTtl.toSeconds()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Token refresh failed: " + e.getMessage(), e);
            sendError(exchange, "Token refresh failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}