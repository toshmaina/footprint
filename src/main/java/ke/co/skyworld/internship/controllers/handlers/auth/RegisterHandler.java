package ke.co.skyworld.internship.controllers.handlers.auth;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.auth.RegisterRequest;
import ke.co.skyworld.internship.domain.beans.auth.RegisterResponse;
import ke.co.skyworld.internship.repository.UserAccountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.Encryption;
import ke.co.skyworld.internship.util.validation.Validation;

import java.io.IOException;
import java.sql.SQLException;

public class RegisterHandler extends SkyInventoryManagementHttpHandler {

    private final UserAccountRepository userAccountRepository = new UserAccountRepository();

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        RegisterRequest request;
        try {
            request = parseBody(exchange, RegisterRequest.class);

        } catch (IOException e) {
                        Log.warning(RegisterHandler.class, "handleRequest",
                                        "Failed to parse register request body: " + e.getMessage());
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }
        if(request == null ) {
            sendError(exchange, "The request body is required", StatusCodes.BAD_REQUEST);
        return;
        }
        String validationError = Validation.validate(request);
        if (validationError != null) {
            sendError(exchange, validationError, StatusCodes.BAD_REQUEST);
            return;
        }
        try {
            if (userAccountRepository.userExists(request.username(), request.email())) {
                sendError(exchange, "Username or email is already taken", StatusCodes.CONFLICT);
                return;
            }

            String hashedPassword = Encryption.hashPassword(request.password());

            long userId = userAccountRepository.createUser(
                    request.username(),
                    request.email(),
                    hashedPassword,
                    request.fullName(),
                    request.warehouseId() // Nullable per the schema
            );


            send(exchange, new RegisterResponse(userId, "User registered successfully"), StatusCodes.CREATED);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "Username or email is already taken", StatusCodes.CONFLICT);
            } else {
                Log.error(getClass(), "handleRequest", "Registration failed: " + e.getMessage(), e);
                sendError(exchange, "Registration failed due to an internal error", StatusCodes.INTERNAL_SERVER_ERROR);
            }
        }
    }
}