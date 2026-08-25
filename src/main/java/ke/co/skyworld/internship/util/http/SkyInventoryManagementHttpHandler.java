package ke.co.skyworld.internship.util.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ke.co.skyworld.internship.domain.beans.ExceptionRepresentation;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.security.RequestContext;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StatusCodes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static ke.co.skyworld.internship.config.Constants.*;
import static ke.co.skyworld.internship.util.formatting.Converter.getObjectMapper;




public class SkyInventoryManagementHttpHandler implements HttpHandler {

    public static String getQueryParam(HttpServerExchange exchange, String key) {
        Deque<String> param = exchange.getQueryParameters().get(key);
        String paramStr = null;

        if (param != null && !param.getFirst().isEmpty()) {
            paramStr = param.getFirst();
        }

        return paramStr;
    }

    public static String getQueryParam(HttpServerExchange exchange, String key, String defaultValue) {
        String val = getQueryParam(exchange, key);
        return (val == null || val.isBlank()) ? defaultValue : val;
    }

    public static int getQueryParamInt(HttpServerExchange exchange, String key, int defaultValue) {
        String val = getQueryParam(exchange, key);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Long getQueryParamLong(HttpServerExchange exchange, String key) {
        String val = getQueryParam(exchange, key);
        if (val == null || val.isBlank()) return null;
        try {
            return Long.parseLong(val);
        } catch (Exception e) {
            return null;
        }
    }


    public static Long getQueryParamLong(HttpServerExchange exchange, String key, Long defaultValue) {
        Long val = getQueryParamLong(exchange, key);
        return val == null ? defaultValue : val;
    }

    public static Double getQueryParamDouble(HttpServerExchange exchange, String key, Double defaultValue) {
        String val = getQueryParam(exchange, key);
        if (val == null || val.isBlank()) return defaultValue;
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String safeClientMessage(String message, int status) {
        if (message == null || message.isBlank()) {
            return "Internal server error";
        }

        // Only sanitize 5xx errors
        if (status >= 500) {
            String lower = message.toLowerCase(Locale.ROOT);

            if (lower.contains("java.lang.")
                    || lower.contains("org.postgresql")
                    || lower.contains("exception:")
                    || lower.contains("\tat ")
                    || lower.contains("jdbc")
                    || lower.contains("sqlstate")
                    || lower.contains("syntax error")) {
                return "Internal server error";
            }
        }

        return message.trim();
    }

    public static void sendError(
            HttpServerExchange exchange,
            String message,
            int status
    ) {
        send(
                exchange,
                new ExceptionRepresentation(
                        safeClientMessage(message, status),
                        exchange.getRequestURI(),
                        "API Error",
                        status,
                        exchange.getRequestMethod()
                ),
                status
        );
    }

    public static void send(HttpServerExchange exchange, Object data, Integer status) {
        exchange.setStatusCode(status);

        String contentType = determineAccept(exchange);

        try {
            ObjectMapper objectMapper = getObjectMapper(contentType);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
            exchange.getResponseSender().send(objectMapper.writeValueAsString(data));
        } catch (JsonProcessingException e) {
            Log.error(SkyInventoryManagementHttpHandler.class, "send",
                    "Failed to serialize response body for path=" + exchange.getRequestURI()
                            + " status=" + status + ": " + e.getMessage(), e);
            exchange.setStatusCode(StatusCodes.INTERNAL_SERVER_ERROR);
            try {
                exchange.getResponseSender().send(
                        "{\"error\":\"Internal server error\",\"message\":\"Failed to serialize response\"}");
            } catch (Exception ignore) {
            }
        }

        try {
            exchange.getResponseChannel().shutdownWrites();
        } catch (IOException | NullPointerException ignore) {
        }

        exchange.endExchange();
    }


    public static void sendBytes(HttpServerExchange exchange, byte[] data, String contentType, String filename) {
        exchange.setStatusCode(StatusCodes.OK);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, contentType);
        exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION,
                "attachment; filename=\"" + filename.replace("\"", "") + "\"");
        exchange.getResponseSender().send(java.nio.ByteBuffer.wrap(data));
        exchange.endExchange();
    }

    private static String determineContentType(HttpServerExchange exchange) {
        try {
            return determineAorCt(exchange.getRequestHeaders()
                    .get("Content-Type").getFirst());
        } catch (NullPointerException e) {
            return determineAorCt(applicationJson);
        }
    }


    private static String determineAccept(HttpServerExchange exchange) {
        try {
            return determineAorCt(exchange.getRequestHeaders()
                    .get("Accept").getFirst());
        } catch (NullPointerException e) {
            return determineAorCt(applicationXml);
        }
    }

    private static String determineAorCt(String headerValue) {
        if (headerValue == null) return applicationXml;
        if (headerValue.contains(applicationJson)) return applicationJson;
        return applicationXml;
    }


    public static String getFormData(HttpServerExchange exchange, String key) {
        FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
        Deque<FormData.FormValue> formValueDeque = formData.get(key);
        String value = null;

        if (formValueDeque != null && !formValueDeque.getFirst().getValue().isEmpty()) {
            value = formValueDeque.getFirst().getValue();
        }
        if (dumpRequest()) {
            Log.debug(SkyInventoryManagementHttpHandler.class, "Request Body (Form Data)", "key=" + key + ", value=" + value);
        }
        return value;
    }

    public static HashMap<String, String> getFormData(HttpServerExchange exchange, String... keys) {
        FormData formData = exchange.getAttachment(FormDataParser.FORM_DATA);
        HashMap<String, String> values = new HashMap<>();
        Deque<FormData.FormValue> formValueDeque = null;

        for (String key : keys) {
            formValueDeque = formData.get(key);
            if (key != null) {
                values.put(key, formValueDeque.getFirst().getValue());
            }
        }
        if (dumpRequest()) System.out.println("Request Body (Form Data): " + formData + "\n");
        return values;
    }

    public static <T> T parseBody(HttpServerExchange exchange, Class<T> clazz) throws IOException {
        String body = getRequestBody(exchange);

        if (body == null || body.trim().isEmpty()) {
            return null;
        }

        try {
            String contentType = determineContentType(exchange);
            ObjectMapper objectMapper = getObjectMapper(contentType);

            if (dumpRequest()) {
                String prettyBody = body;

                try {
                    Object parsed = objectMapper.readValue(body, Object.class);
                    prettyBody = objectMapper
                            .writerWithDefaultPrettyPrinter()
                            .writeValueAsString(parsed);
                } catch (Exception ignore) {
                }

                Log.debug(SkyInventoryManagementHttpHandler.class, "Request Body", String.format("%n========== %s ==========%n%s%n===========================%n%n", contentType, prettyBody));
            }

            return objectMapper.readValue(body, clazz);
        } catch (JsonProcessingException e) {
            throw new IOException("Failed to parse request body: " + e.getOriginalMessage(), e);
        }
    }

    public static String getRequestBody(HttpServerExchange exchange) throws IOException {
        exchange.startBlocking();
        try (InputStream inputStream = exchange.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            return bytes.length > 0 ? new String(bytes, StandardCharsets.UTF_8) : null;
        }
    }

    public static RequestContext currentUser(HttpServerExchange exchange) {
        return exchange.getAttachment(RequestContext.ATTACHMENT_KEY);
    }

    protected static boolean isSuperUser(HttpServerExchange exchange) {
        RequestContext context = currentUser(exchange);
        return context != null && context.superUser();
    }

//    protected static boolean isSelfOrSuperUser(HttpServerExchange exchange, Long targetMemberId) {
//        RequestContext context = currentUser(exchange);
//        return context != null && context.isSelfOrSuperUser(targetMemberId);
//    }

    public static String getAuthToken(HttpServerExchange exchange) {
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        if (authHeader != null && !authHeader.isBlank()) {
            return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        }
        return getQueryParam(exchange, "token");
    }

    protected HashMap<String, String> getPathVars(HttpServerExchange exchange, String... pathVarIds) {
        PathTemplateMatch pathMatch =
                exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);

        HashMap<String, String> pathVars = new HashMap<>(pathVarIds.length);

        for (String pathVarId : pathVarIds) {
            pathVars.put(pathVarId, pathMatch.getParameters().get(pathVarId));
        }

        return pathVars;
    }

    public String getPathVar(HttpServerExchange exchange, String pathVarId) {
        PathTemplateMatch pathMatch =
                exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);

        return pathMatch.getParameters().get(pathVarId);
    }

    protected HashMap<String, String> getQueryParams(HttpServerExchange exchange, String... keys) {

        HashMap<String, String> params = new HashMap<>();
        Deque<String> param = null;

        for (String key : keys) {
            param = exchange.getQueryParameters().get(key);

            if (param != null && !param.getFirst().equals("")) {
                params.put(key, param.getFirst());
            }
        }


        return params;
    }

    protected Map<String, String> getQueryParams(HttpServerExchange exchange) {
        Map<String, String> params = new HashMap<>();
        for (Map.Entry<String, Deque<String>> entry : exchange.getQueryParameters().entrySet()) {
            params.put(entry.getKey(), entry.getValue().getFirst());
        }
        return params;
    }

    protected int[] getPageAndPageSize(HttpServerExchange exchange) {

        int[] pageAndPageSize = new int[]{1, 10};

        Deque<String> page = exchange.getQueryParameters().get("page");
        Deque<String> pageSize = exchange.getQueryParameters().get("pageSize");

        if (page != null) {
            try {
                pageAndPageSize[0] = Integer.parseInt(page.getFirst());
            } catch (Exception ignore) {

            }
        }

        if (pageSize != null) {
            try {
                if (Integer.parseInt(pageSize.getFirst()) > 0) {
                    pageAndPageSize[1] = Integer.parseInt(pageSize.getFirst());
                }

            } catch (Exception ignore) {

            }
        }
        return pageAndPageSize;
    }

    public String getClientIp(HttpServerExchange exchange) {
        String xff = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        var addr = exchange.getSourceAddress();
        return addr != null ? addr.getAddress().getHostAddress() : null;
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) {

    }

}

