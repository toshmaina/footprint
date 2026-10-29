package ke.co.skyworld.internship.controllers.handlers.auth;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.auth.LoginResponse;
import ke.co.skyworld.internship.domain.beans.auth.TokenPair;
import ke.co.skyworld.internship.repository.TokenRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.memory.InMemoryCache;
import ke.co.skyworld.internship.util.security.TokenTtl;
import ke.co.skyworld.internship.util.validation.Validation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;

public class VerifyOtpHandler extends SkyInventoryManagementHttpHandler {

    private static final String FAILED_OTP_VERIFICATION_MESSAGE = "OTP Verification failed";
    private final TokenRepository tokenRepository = new TokenRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        VerifyOtpRequest request;
        try {
            request = parseBody(exchange, VerifyOtpRequest.class);
        } catch (IOException e) {
            Log.warning(LoginHandler.class, "handleRequest",
                    "Failed to parse verify otp request body: " + e.getMessage());
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if (request == null) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        String validationError = Validation.validate(request);

        if (validationError != null) {
            Log.warning(LoginHandler.class, "handleRequest", validationError);
            sendError(exchange, validationError, StatusCodes.BAD_REQUEST);
            return;
        }
        if (request.key() == null || request.key().isBlank()) {
            sendError(exchange, "Key is required", StatusCodes.UNAUTHORIZED);
            return;
        }

        String cacheKey = "OTP_KEY:" + request.key();

        MemoryCacheOtpStore store = InMemoryCache.get(cacheKey);

        if (store == null || store.otp().isBlank()) {
            Log.debug(LoginHandler.class, "handleRequest", "Failed to verify otp debug");
            sendError(exchange, FAILED_OTP_VERIFICATION_MESSAGE, StatusCodes.UNAUTHORIZED);
            return;
        }

        //use the constant time comparison to avoid side channel attack
        boolean match = MessageDigest.isEqual(
                store.otp().getBytes(StandardCharsets.UTF_8),
                request.otp().getBytes(StandardCharsets.UTF_8));

        if (!match) {
            sendError(exchange, FAILED_OTP_VERIFICATION_MESSAGE, StatusCodes.UNAUTHORIZED);
            return;
        }
        if (!InMemoryCache.remove(cacheKey)) {
            sendError(exchange, FAILED_OTP_VERIFICATION_MESSAGE, StatusCodes.UNAUTHORIZED);
            return;
        }

        try {

            TokenPair tokenPair = tokenRepository.issueTokenPair(Long.parseLong(store.userAccountId()));

            send(exchange, new LoginResponse(tokenPair.accessToken(), tokenPair.refreshToken(), TokenTtl.accessTokenTtl().toSeconds()), StatusCodes.OK);

        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", FAILED_OTP_VERIFICATION_MESSAGE + " :" + e.getMessage(), e);
            sendError(exchange, FAILED_OTP_VERIFICATION_MESSAGE, StatusCodes.INTERNAL_SERVER_ERROR);
        }
    }


    public record MemoryCacheOtpStore(String userAccountId, String key, String otp) {
    }

    private static record VerifyOtpRequest(String key, String otp) {
    }
}