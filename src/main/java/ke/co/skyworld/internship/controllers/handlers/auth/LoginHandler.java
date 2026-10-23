package ke.co.skyworld.internship.controllers.handlers.auth;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.auth.LoginRequest;
import ke.co.skyworld.internship.domain.beans.auth.LoginResponse;
import ke.co.skyworld.internship.domain.beans.auth.TokenPair;
import ke.co.skyworld.internship.domain.enums.UserAccountStatus;
import ke.co.skyworld.internship.repository.TokenRepository;
import ke.co.skyworld.internship.repository.UserAccountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.Encryption;
import ke.co.skyworld.internship.util.security.TokenTtl;
import ke.co.skyworld.internship.util.validation.Validation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class LoginHandler extends SkyInventoryManagementHttpHandler {


    private final UserAccountRepository userAccountRepository = new UserAccountRepository();
    private final TokenRepository tokenRepository = new TokenRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        LoginRequest request;
        try {
            request = parseBody(exchange, LoginRequest.class);
        } catch (IOException e) {
            Log.warning(LoginHandler.class, "handleRequest",
                    "Failed to parse login request body: " + e.getMessage());
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request == null) {
            sendError(exchange,"Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        String validationError = Validation.validate(request);

        if (validationError != null) {
            Log.warning(LoginHandler.class,"handleRequest",validationError);
            sendError(exchange, validationError, StatusCodes.BAD_REQUEST);
            return;
        }


        try {
            Optional<UserAccountRepository.LoginCredentials> credentials =
                    userAccountRepository.findLoginCredentials(request.username());
            if (credentials.isEmpty() || !Encryption.verifyPassword(
                    request.password(), credentials.get().passwordHash())) {
                sendError(exchange, "Invalid username or password", StatusCodes.UNAUTHORIZED);
                return;
            }


            if (credentials.get().status() != UserAccountStatus.ACTIVE) {
                sendError(exchange, "This account is " + credentials.get().status().name().toLowerCase()
                        + " and cannot log in", StatusCodes.FORBIDDEN);
                return;
            }
            TokenPair tokenPair = tokenRepository.issueTokenPair(credentials.get().userAccountId());
            send(exchange, new LoginResponse(tokenPair.accessToken(), tokenPair.refreshToken(), TokenTtl.accessTokenTtl().toSeconds()), StatusCodes.OK);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Login failed: " + e.getMessage(), e);
            sendError(exchange, "Login failed", StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }
}