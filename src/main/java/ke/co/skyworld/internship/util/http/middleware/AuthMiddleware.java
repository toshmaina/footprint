package ke.co.skyworld.internship.util.http.middleware;

import ke.co.skyworld.internship.domain.enums.TokenType;
import ke.co.skyworld.internship.repository.PermissionRepository;
import ke.co.skyworld.internship.repository.TokenRepository;
import ke.co.skyworld.internship.repository.UserAccountRepository;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.sql.SQLException;
import java.util.Set;

/**
 * Resolves the bearer token to a RequestContext (warehouse scope +
 * permissions) and, when this instance was built with required
 * permissions, 403s any caller missing all of them. Required roles are
 * passed into the constructor per-route rather than annotation/reflection
 * based authorization, matching how routes get wired up elsewhere in
 * this codebase.
 */
public class AuthMiddleware implements HttpHandler {

    private final TokenRepository tokenRepository = new TokenRepository();
    private final PermissionRepository permissionRepository = new PermissionRepository();
    private final UserAccountRepository userAccountRepository = new UserAccountRepository();
    private final HttpHandler next;
    private final String[] requiredPermissions;

    public AuthMiddleware(HttpHandler next) {
        this(next, new String[0]);
    }

    public AuthMiddleware(HttpHandler next, String... requiredPermissions) {
        this.next = next;
        this.requiredPermissions = requiredPermissions;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String token = SkyInventoryManagementHttpHandler.getAuthToken(exchange);
        if (token == null || token.isBlank()) {
            SkyInventoryManagementHttpHandler.sendError(exchange, "Missing bearer access token", StatusCodes.UNAUTHORIZED);
            return;
        }

        RequestContext context;
        try {
            Long userAccountId = tokenRepository.getUserIdByToken(TokenType.ACCESS, token);
            if (userAccountId == null) {
                SkyInventoryManagementHttpHandler.sendError(exchange, "Invalid or expired access token", StatusCodes.UNAUTHORIZED);
                return;
            }

            UserAccountRepository.AuthSnapshot snapshot = userAccountRepository.findAuthSnapshot(userAccountId);
            if (snapshot == null) {
                // Token pointed at a user_account row that no longer exists -
                // treat identically to an invalid token, not a server error.
                SkyInventoryManagementHttpHandler.sendError(exchange, "Invalid or expired access token", StatusCodes.UNAUTHORIZED);
                return;
            }
            if (!"active".equalsIgnoreCase(snapshot.status())) {
                SkyInventoryManagementHttpHandler.sendError(exchange,
                        "This account is " + snapshot.status().toLowerCase() + " and cannot access the API",
                        StatusCodes.FORBIDDEN);
                return;
            }

            Set<String> permissions = permissionRepository.listPermissionsForUser(userAccountId);
            boolean superUser = permissions.contains(RequestContext.PERMISSION_FULL_ACCESS);

            context = new RequestContext(userAccountId, snapshot.defaultWarehouseId(), superUser, permissions);
        } catch (SQLException e) {
            Log.error(getClass(), "handleRequest", "Failed to resolve request context: " + e.getMessage(), e);
            SkyInventoryManagementHttpHandler.sendError(exchange, "Failed to validate access token",
                    StatusCodes.INTERNAL_SERVER_ERROR);
            return;
        }

        if (requiredPermissions.length > 0 && !context.hasAnyPermission(requiredPermissions)) {
            SkyInventoryManagementHttpHandler.sendError(exchange, "You don't have enough rights to perform this action",
                    StatusCodes.FORBIDDEN);
            return;
        }

        exchange.putAttachment(RequestContext.ATTACHMENT_KEY, context);
        next.handleRequest(exchange);
    }

}