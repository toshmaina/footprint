package com.skyworld.util.http;

import com.skyworld.util.logging.Log;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * sky-core (ke.co.skyworld.internship.skycore.util.http)
 * Created by: oloo
 * On: 8/12/26. 5:34 PM
 * Description:
 **/

public class Dispatcher implements HttpHandler {

    private volatile HttpHandler handler;

    public Dispatcher(HttpHandler handler) {
        this.handler = handler;
    }

    public Dispatcher() {
        this((HttpHandler) null);
    }

    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (exchange.isInIoThread()) {
            exchange.dispatch(this.handler);
        } else {
            Log.warning(Dispatcher.class, "handleRequest",
                    "Exchange not in IO thread - dispatching handler directly. path=" + exchange.getRequestPath());
            this.handler.handleRequest(exchange);
        }
    }
}
