//package com.skyworld.util.http.middleware;
//
//import io.undertow.server.HttpHandler;
//import io.undertow.server.HttpServerExchange;
//import io.undertow.util.StatusCodes;
//import ke.co.skyworld.internship.skycore.domain.entities.Member;
//import ke.co.skyworld.internship.skycore.domain.enums.TokenType;
//import ke.co.skyworld.internship.skycore.repository.MemberRepository;
//import ke.co.skyworld.internship.skycore.repository.PermissionRepository;
//import ke.co.skyworld.internship.skycore.repository.TokenRepository;
//import ke.co.skyworld.internship.skycore.util.http.SkyCoreHttpHandler;
//import ke.co.skyworld.internship.skycore.util.logging.Log;
//import ke.co.skyworld.internship.skycore.util.security.RequestContext;
//
//import java.sql.SQLException;
//import java.util.Set;
//
///**
// * sky-core (ke.co.skyworld.internship.skycore.util.http.middleware)
// * Created by: oloo
// * On: 8/12/26.
// * Description: resolves the bearer token to a RequestContext (permissions + member
// * status) and, when this instance was built with required permissions, 403s any caller
// * missing all of them. Borrowed from simple-survey-api's pattern of passing required
// * roles into the AuthMiddleware constructor per-route rather than annotation/reflection
// * based authorization.
// **/
//
//public class AuthMiddleware implements HttpHandler {
//
//    private final TokenRepository tokenRepository = new TokenRepository();
//    private final MemberRepository memberRepository = new MemberRepository();
//    private final PermissionRepository permissionRepository = new PermissionRepository();
//    private final HttpHandler next;
//    private final String[] requiredPermissions;
//
//    public AuthMiddleware(HttpHandler next) {
//        this(next, new String[0]);
//    }
//
//    public AuthMiddleware(HttpHandler next, String... requiredPermissions) {
//        this.next = next;
//        this.requiredPermissions = requiredPermissions;
//    }
//
//    @Override
//    public void handleRequest(HttpServerExchange exchange) throws Exception {
//        String token = SkyCoreHttpHandler.getAuthToken(exchange);
//        if (token == null || token.isBlank()) {
//            SkyCoreHttpHandler.sendError(exchange, "Missing bearer access token", StatusCodes.UNAUTHORIZED);
//            return;
//        }
//
//        Long userAccountId;
//        RequestContext context;
//        try {
//            userAccountId = tokenRepository.getUserIdByToken(TokenType.ACCESS, token);
//            if (userAccountId == null) {
//                SkyCoreHttpHandler.sendError(exchange, "Invalid or expired access token", StatusCodes.UNAUTHORIZED);
//                return;
//            }
//
//            Set<String> permissions = permissionRepository.listPermissionsForUser(userAccountId);
//            boolean superUser = permissions.contains(RequestContext.PERMISSION_FULL_ACCESS);
//            Member member = memberRepository.findByUserAccountId(userAccountId).orElse(null);
//            Long memberId = member != null ? member.memberId() : null;
//            context = new RequestContext(userAccountId, memberId, superUser, permissions,
//                    member != null ? member.memberStatus() : null);
//        } catch (SQLException e) {
//            Log.error(getClass(), "handleRequest", "Failed to resolve request context: " + e.getMessage(), e);
//            SkyCoreHttpHandler.sendError(exchange, "Failed to validate access token",
//                    StatusCodes.INTERNAL_SERVER_ERROR);
//            return;
//        }
//
//        if (requiredPermissions.length > 0) {
//            boolean allowed = context.superUser();
//            for (String permission : requiredPermissions) {
//                if (context.hasPermission(permission)) {
//                    allowed = true;
//                    break;
//                }
//            }
//            if (!allowed) {
//                SkyCoreHttpHandler.sendError(exchange, "You don't have enough rights to perform this action",
//                        StatusCodes.FORBIDDEN);
//                return;
//            }
//        }
//
//        exchange.putAttachment(RequestContext.ATTACHMENT_KEY, context);
//        next.handleRequest(exchange);
//    }
//
//}
