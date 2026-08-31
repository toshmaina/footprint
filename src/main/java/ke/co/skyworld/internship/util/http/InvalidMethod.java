package ke.co.skyworld.internship.util.http;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.domain.beans.ExceptionRepresentation;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.http)
 * Created by: oloo
 * On: 8/12/26. 5:35 PM
 * Description:
 **/

public class InvalidMethod extends SkyInventoryManagementHttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) {

        send(exchange, new ExceptionRepresentation(
                "Method Not Allowed",
                exchange.getRequestURI(),
                "Method " + exchange.getRequestMethod() + " not allowed",
                StatusCodes.METHOD_NOT_ALLOWED,
                exchange.getRequestMethod()
        ), StatusCodes.METHOD_NOT_ALLOWED);
    }
}