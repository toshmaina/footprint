package ke.co.skyworld.internship.util.http;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import ke.co.skyworld.internship.util.logging.Log;


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
