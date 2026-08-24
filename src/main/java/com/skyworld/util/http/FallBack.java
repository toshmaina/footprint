package com.skyworld.util.http;

import com.skyworld.domain.beans.ExceptionRepresentation;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.http)
 * Created by: oloo
 * On: 8/12/26. 5:34 PM
 * Description:
 **/

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