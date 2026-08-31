package ke.co.skyworld.internship.util.http;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.domain.beans.OriginAllowlist;
import ke.co.skyworld.internship.util.logging.Log;


public class CorsHandler implements HttpHandler {
    private static final HttpString ACCESS_CONTROL_ALLOW_ORIGIN = new HttpString("Access-Control-Allow-Origin");
    private static final HttpString ACCESS_CONTROL_ALLOW_METHODS = new HttpString("Access-Control-Allow-Methods");
    private static final HttpString ACCESS_CONTROL_ALLOW_HEADERS = new HttpString("Access-Control-Allow-Headers");
    private static final HttpString ACCESS_CONTROL_ALLOW_CREDENTIALS = new HttpString("Access-Control-Allow-Credentials");
    private static final HttpString ACCESS_CONTROL_EXPOSE_HEADERS = new HttpString("Access-Control-Expose-Headers");
    private static final HttpString ACCESS_CONTROL_MAX_AGE = new HttpString("Access-Control-Max-Age");

    private static final OriginAllowlist ALLOWLIST = OriginAllowlist.fromConfig();
    private static final String ALLOWED_METHODS = Constants.getCorsAllowedMethods();
    private static final String ALLOWED_HEADERS = Constants.getCorsAllowedHeaders();
    private static final String EXPOSED_HEADERS = Constants.getCorsExposedHeaders();
    private static final boolean ALLOW_CREDENTIALS = Constants.getCorsAllowCredentials();
    private static final String MAX_AGE_SECONDS = String.valueOf(Constants.getCorsMaxAgeSeconds());

    private final HttpHandler next;

    public CorsHandler(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (isSsePath(exchange.getRequestPath())) {
            exchange.getResponseHeaders().put(new HttpString("Content-Type"), "text/event-stream");
            exchange.getResponseHeaders().put(new HttpString("Cache-Control"), "no-cache");
            exchange.getResponseHeaders().put(new HttpString("Connection"), "keep-alive");
            exchange.getResponseHeaders().put(new HttpString("X-Accel-Buffering"), "no"); // For Nginx
        } else {
            exchange.getResponseHeaders().put(new HttpString("Content-Type"), "application/json");
        }

        String origin = exchange.getRequestHeaders().getFirst("Origin");

        if (origin == null) {
            // Not a CORS request (curl, server-to-server calls, health checks, mobile
            // app HTTP clients)
            next.handleRequest(exchange);
            return;
        }

        String allowedOrigin = ALLOWLIST.match(origin);
        if (allowedOrigin == null) {
            Log.warning(CorsHandler.class, "handleRequest",
                    "Blocked disallowed CORS origin '" + origin + "' for path " + exchange.getRequestPath());
            blockRequest(exchange);
            return;
        }

        exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_ORIGIN, allowedOrigin);
        exchange.getResponseHeaders().put(new HttpString("Vary"), "Origin");
        exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
        exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
        exchange.getResponseHeaders().put(ACCESS_CONTROL_ALLOW_CREDENTIALS, String.valueOf(ALLOW_CREDENTIALS));
        if (!EXPOSED_HEADERS.isEmpty()) {
            exchange.getResponseHeaders().put(ACCESS_CONTROL_EXPOSE_HEADERS, EXPOSED_HEADERS);
        }

        if (exchange.getRequestMethod().equals(Methods.OPTIONS)) {
            exchange.getResponseHeaders().put(ACCESS_CONTROL_MAX_AGE, MAX_AGE_SECONDS);
            exchange.setStatusCode(204);
            exchange.endExchange();
            return;
        }

        next.handleRequest(exchange);
    }

    private boolean isSsePath(String path) {
        return path.contains("/notifications/subscribe")
                || path.endsWith("/preloads/dashboard_stream")
                || path.endsWith("/analytics/stream")
                || path.endsWith("/analytics/monthly_trends_stream");
    }

    private void blockRequest(HttpServerExchange exchange) {
        exchange.setStatusCode(403);
        exchange.getResponseSender().send("Forbidden: Unauthorized request");
        exchange.endExchange();
    }
}