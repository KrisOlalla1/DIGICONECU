package com.switchecu.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate Limiter Filter usando Token Bucket Algorithm nativo
 * Implementa Rate Limiting sin dependencias externas
 */
@Slf4j
@Component
public class RateLimiterGatewayFilterFactory
        extends AbstractGatewayFilterFactory<RateLimiterGatewayFilterFactory.Config> {

    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiterGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String clientId = extractClientId(exchange);
            String bucketKey = clientId + ":" + exchange.getRequest().getPath().toString();

            TokenBucket bucket = buckets.computeIfAbsent(bucketKey,
                    k -> new TokenBucket(config.getRequestsPerSecond(), config.getBurstCapacity()));

            if (bucket.tryConsume()) {
                // Request allowed
                long remainingTokens = bucket.getAvailableTokens();
                exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", String.valueOf(remainingTokens));
                exchange.getResponse().getHeaders().add("X-RateLimit-Limit",
                        String.valueOf(config.getRequestsPerSecond()));
                return chain.filter(exchange);
            } else {
                // Rate limit exceeded
                log.warn("Rate limit exceeded for client: {}, path: {}", clientId, exchange.getRequest().getPath());
                exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                exchange.getResponse().getHeaders().add("X-RateLimit-Retry-After", "1");
                exchange.getResponse().getHeaders().add("X-RateLimit-Limit",
                        String.valueOf(config.getRequestsPerSecond()));
                return exchange.getResponse().setComplete();
            }
        };
    }

    private String extractClientId(org.springframework.web.server.ServerWebExchange exchange) {
        // Try to get client ID from header first
        String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");
        if (clientId != null && !clientId.isEmpty()) {
            return clientId;
        }

        // Fall back to IP address
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "anonymous";
    }

    /**
     * Simple Token Bucket implementation
     */
    private static class TokenBucket {
        private final int refillRate;
        private final int capacity;
        private final AtomicLong tokens;
        private volatile long lastRefillTime;

        public TokenBucket(int refillRate, int capacity) {
            this.refillRate = refillRate;
            this.capacity = capacity;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        public long getAvailableTokens() {
            refill();
            return tokens.get();
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTime;

            if (elapsed >= 1000) { // Refill every second
                long tokensToAdd = (elapsed / 1000) * refillRate;
                long newTokens = Math.min(capacity, tokens.get() + tokensToAdd);
                tokens.set(newTokens);
                lastRefillTime = now;
            }
        }
    }

    public static class Config {
        private int requestsPerSecond = 100;
        private int burstCapacity = 200;

        public int getRequestsPerSecond() {
            return requestsPerSecond;
        }

        public void setRequestsPerSecond(int requestsPerSecond) {
            this.requestsPerSecond = requestsPerSecond;
        }

        public int getBurstCapacity() {
            return burstCapacity;
        }

        public void setBurstCapacity(int burstCapacity) {
            this.burstCapacity = burstCapacity;
        }
    }
}
