package ke.co.skyworld.internship.controllers.handlers.auth;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.RegisterRequest;
import ke.co.skyworld.internship.domain.beans.RegisterResponse;
import ke.co.skyworld.internship.repository.UserAccountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.Encryption;

import java.sql.SQLException;

public class RegisterHandler extends SkyInventoryManagementHttpHandler {

    private final UserAccountRepository userAccountRepository = new UserAccountRepository();

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        RegisterRequest request;
        try {
            request = parseBody(exchange, RegisterRequest.class);
        } catch (Exception e) {
            sendError(exchange, "Malformed request body", StatusCodes.BAD_REQUEST);
            return;
        }

        // Null check happens before anything else touches `request` -
        // an empty body must never reach a .get*() call.
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())
                || isBlank(request.getFullName())) {
            sendError(exchange, "Username, password, and full name are required", StatusCodes.BAD_REQUEST);
            return;
        }

        try {
            // Check if user already exists to prevent leaking database constraint errors directly
            if (userAccountRepository.userExists(request.getUsername(), request.getEmail())) {
                sendError(exchange, "Username or email is already taken", StatusCodes.CONFLICT);
                return;
            }

            // Hash the password securely before passing it to the repository
            String hashedPassword = Encryption.hashPassword(request.getPassword());

            // Create the account. By default, the schema sets status to 'active'
            long userId = userAccountRepository.createUser(
                    request.getUsername(),
                    request.getEmail(),
                    hashedPassword,
                    request.getFullName(),
                    request.getWarehouseId() // Nullable per the schema
            );

            // Optional: Automatically assign a default role (e.g., 'staff') here or leave it to an admin API.

            send(exchange, new RegisterResponse(userId, "User registered successfully"), StatusCodes.CREATED);
        } catch (SQLException e) {
            // PostgreSQL unique violation is state 23505. Just in case our userExists check missed a race condition.
            if ("23505".equals(e.getSQLState())) {
                sendError(exchange, "Username or email is already taken", StatusCodes.CONFLICT);
            } else {
                Log.error(getClass(), "handleRequest", "Registration failed: " + e.getMessage(), e);
                sendError(exchange, "Registration failed due to an internal error", StatusCodes.INTERNAL_SERVER_ERROR);
            }
        }
    }
}