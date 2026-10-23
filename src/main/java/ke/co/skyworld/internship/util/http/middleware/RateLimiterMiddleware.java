package ke.co.skyworld.internship.util.http.middleware;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;
import ke.co.skyworld.internship.config.Constants;
import ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler;
import ke.co.skyworld.internship.util.logging.Log;
import ke.co.skyworld.internship.util.misc.ResolveDuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static ke.co.skyworld.internship.util.http.SkyInventoryManagementHttpHandler.send;

public class RateLimiterMiddleware implements HttpHandler {
    private static final int RATE_LIMIT_TOKEN_BUCKET_REFILL_TIME = Constants.getRateLimitTokenBucketRefillTime();
    private static final String RATE_LIMIT_TOKEN_BUCKET_REFILL_TIME_UNIT = Constants.getRateLimitTokenBucketRefillTimeUnit();
    private static final Long RATE_LIMIT_TOKEN_BUCKET_REFILL_AMOUNT = Constants.getRateLimitTokenBucketRefillAmount();
    private static final Long RATE_LIMIT_TOKEN_BUCKET_CAPACITY = Constants.getRateLimitTokenBucketCapacity();
    private final HttpHandler next;

    // Thread-safe cache to store rate-limit buckets per IP Address
    private static final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    public RateLimiterMiddleware(HttpHandler next) {
        this.next = next;
    }

    // Define the rate limit rule: Token Bucket with a defined capacity, refilling at a fixed rate.
    private static Bucket createNewBucket() {

        Bandwidth limit = Bandwidth.classic(RATE_LIMIT_TOKEN_BUCKET_CAPACITY, Refill.greedy(RATE_LIMIT_TOKEN_BUCKET_REFILL_AMOUNT, ResolveDuration.createDuration(RATE_LIMIT_TOKEN_BUCKET_REFILL_TIME,RATE_LIMIT_TOKEN_BUCKET_REFILL_TIME_UNIT)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String clientIp = new SkyInventoryManagementHttpHandler().getClientIp(exchange);
        if (clientIp == null || clientIp.isBlank()) {
            clientIp = "unknown-ip";
        }


        Bucket bucket = ipBuckets.computeIfAbsent(clientIp, k -> createNewBucket());
        if (bucket.tryConsume(1)) {
            next.handleRequest(exchange);
        } else {
            Log.warning(RateLimiterMiddleware.class,"handleRequest","Rate limit exceeded for: " + clientIp);
            send(exchange,Map.of("error","Rate limit exceeded. Please try again later."),StatusCodes.TOO_MANY_REQUESTS);
        }
    }
}