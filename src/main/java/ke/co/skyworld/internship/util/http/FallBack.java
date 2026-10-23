
package ke.co.skyworld.internship.util.http;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.util.exception.ExceptionRepresentation;


public class FallBack extends SkyInventoryManagementHttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) {
        String path = exchange.getRequestURI();

        ExceptionRepresentation response =
                ExceptionRepresentation.builder()
                        .status(StatusCodes.NOT_FOUND)
                        .message("URI Not Found")
                        .path(path)
                        .error("URI " + path + " not found on server")
                        .method(exchange.getRequestMethod().toString())
                        .build();

        send(exchange, response, StatusCodes.NOT_FOUND);
    }
}
