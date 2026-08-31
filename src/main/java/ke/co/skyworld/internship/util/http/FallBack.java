package ke.co.skyworld.internship.util.http;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.ExceptionRepresentation;


public class FallBack extends SkyInventoryManagementHttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) {

        send(exchange, new ExceptionRepresentation(
                "URI Not Found",
                exchange.getRequestURI(),
                "URI " + exchange.getRequestURI() + " not found on server",
                StatusCodes.NOT_FOUND,
                exchange.getRequestMethod()
        ), StatusCodes.NOT_FOUND);
    }
}