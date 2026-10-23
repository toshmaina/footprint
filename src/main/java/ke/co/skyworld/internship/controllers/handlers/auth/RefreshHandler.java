package ke.co.skyworld.internship.controllers.handlers.auth;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.auth.LoginResponse;
import ke.co.skyworld.internship.domain.beans.auth.RefreshRequest;
import ke.co.skyworld.internship.domain.beans.auth.TokenPair;
import ke.co.skyworld.internship.domain.enums.TokenType;
import ke.co.skyworld.internship.domain.enums.UserAccountStatus;
import ke.co.skyworld.internship.repository.TokenRepository;
import ke.co.skyworld.internship.repository.UserAccountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.TokenTtl;
import ke.co.skyworld.internship.util.validation.Validation;

import java.io.IOException;
import java.sql.SQLException;

public class RefreshHandler extends SkyInventoryManagementHttpHandler {

    private final TokenRepository tokenRepository = new TokenRepository();
    private final UserAccountRepository userAccountRepository = new UserAccountRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        RefreshRequest request;
        try {
            request = parseBody(exchange, RefreshRequest.class);
        } catch (IOException e) {
            Log.warning(RefreshHandler.class, "handleRequest",
                    "Failed to parse refresh request body: " + e.getMessage());
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request == null) {
            sendError(exchange,"Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        String validationError = Validation.validate(request);
        if (validationError != null) {
            sendError(exchange, validationError, StatusCodes.BAD_REQUEST);
            return;
        }


        String suppliedRefreshToken = request.refreshToken();

        try {
            Long userAccountId = tokenRepository.getUserIdByToken(TokenType.REFRESH, suppliedRefreshToken);
            if (userAccountId == null) {
                sendError(exchange, "Invalid or expired refresh token", StatusCodes.UNAUTHORIZED);
                return;
            }

          UserAccountRepository.AuthSnapshot snapshot = userAccountRepository.findAuthSnapshot(userAccountId);
            if (snapshot == null || !UserAccountStatus.ACTIVE.equals(snapshot.status())) {
                sendError(exchange, "This account cannot refresh a session", StatusCodes.FORBIDDEN);
                return;
            }

            TokenPair newtokenPair = tokenRepository.issueTokenPair(userAccountId);
            // Rotate: revoke the presented refresh token.
            tokenRepository.revokeToken(suppliedRefreshToken);

            send(exchange, new LoginResponse(newtokenPair.accessToken(), newtokenPair.refreshToken(), TokenTtl.accessTokenTtl().toSeconds()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Token refresh failed: " + e.getMessage(), e);
            sendError(exchange, "Token refresh failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}