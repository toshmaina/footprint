package ke.co.skyworld.internship.util.metrics;


import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class Metrics {

    public static final PrometheusMeterRegistry REGISTRY =
            new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);

    public static HttpHandler metricsHandler() {
        return (HttpServerExchange exchange) -> {
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain; version=0.0.4");
            exchange.getResponseSender().send(REGISTRY.scrape());
        };
    }
}
