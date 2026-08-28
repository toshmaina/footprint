package ke.co.skyworld.internship.controllers.handlers.auth;

import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.domain.beans.LoginRequest;
import ke.co.skyworld.internship.domain.beans.LoginResponse;
import ke.co.skyworld.internship.domain.enums.TokenType;
import ke.co.skyworld.internship.repository.TokenRepository;
import ke.co.skyworld.internship.repository.UserAccountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.Encryption;
import ke.co.skyworld.internship.util.security.TokenTtl;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.time.Duration;

public class LoginHandler extends SkyInventoryManagementHttpHandler {

    private final UserAccountRepository userAccountRepository = new UserAccountRepository();
    private final TokenRepository tokenRepository = new TokenRepository();
    private static final int REFRESH_TOKEN_LENGTH = Constants.getRefreshTokenLength();
    private static final int ACCESS_TOKEN_LENGTH = Constants.getAccessTokenLength();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        LoginRequest request;
        try {
            request = parseBody(exchange, LoginRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            sendError(exchange, "Username and password are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            UserAccountRepository.LoginCredentials credentials =
                    userAccountRepository.findLoginCredentials(request.getUsername());

            // Same generic message whether the username doesn't exist or the
            // password is wrong - never confirm which usernames are registered.
            if (credentials == null || !Encryption.verifyPassword(
                    request.getPassword(), credentials.passwordHash())) {
                sendError(exchange, "Invalid username or password", StatusCodes.UNAUTHORIZED);
                return;
            }

            if (!"active".equalsIgnoreCase(credentials.status())) {
                sendError(exchange, "This account is " + credentials.status().toLowerCase()
                        + " and cannot log in", StatusCodes.FORBIDDEN);
                return;
            }

            Duration accessTtl = TokenTtl.accessTokenTtl();
            Duration refreshTtl = TokenTtl.refreshTokenTtl();

            String accessToken = tokenRepository.issueToken(credentials.userAccountId(), TokenType.ACCESS, accessTtl,ACCESS_TOKEN_LENGTH);
            String refreshToken = tokenRepository.issueToken(credentials.userAccountId(), TokenType.REFRESH, refreshTtl,REFRESH_TOKEN_LENGTH);

            send(exchange, new LoginResponse(accessToken, refreshToken, accessTtl.toSeconds()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Login failed: " + e.getMessage(), e);
            sendError(exchange, "Login failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}