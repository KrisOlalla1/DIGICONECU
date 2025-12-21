package com.switchecu.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filtro global para métricas y logging del Gateway
 */
@Slf4j
@Component
public class MetricsFilter implements GlobalFilter, Ordered {

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final Map<String, AtomicLong> requestsPerRoute = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> latencyPerRoute = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String routeId = getRouteId(exchange);

        totalRequests.incrementAndGet();
        requestsPerRoute.computeIfAbsent(routeId, k -> new AtomicLong()).incrementAndGet();

        // Add trace headers
        String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
        if (traceId == null) {
            traceId = java.util.UUID.randomUUID().toString();
        }

        String finalTraceId = traceId;
        exchange.getResponse().getHeaders().add("X-Trace-Id", traceId);
        exchange.getResponse().getHeaders().add("X-Gateway-Timestamp",
                OffsetDateTime.now(ZoneOffset.UTC).toString());

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> {
                    long duration = System.currentTimeMillis() - startTime;
                    latencyPerRoute.computeIfAbsent(routeId, k -> new AtomicLong()).addAndGet(duration);

                    HttpStatus status = (HttpStatus) exchange.getResponse().getStatusCode();
                    if (status != null && status.is2xxSuccessful()) {
                        successfulRequests.incrementAndGet();
                    }

                    log.info("Request completed: {} {} -> {} ({}ms) [trace={}]",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getPath(),
                            status,
                            duration,
                            finalTraceId);
                })
                .doOnError(error -> {
                    failedRequests.incrementAndGet();
                    log.error("Request failed: {} {} [trace={}] - {}",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getPath(),
                            finalTraceId,
                            error.getMessage());
                });
    }

    private String getRouteId(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        if (path.startsWith("/api/v2/transfers"))
            return "payment-processing";
        if (path.startsWith("/api/v1/red"))
            return "network-management";
        if (path.startsWith("/api/v1/saldos"))
            return "account-balance";
        if (path.startsWith("/api/v2/clearing"))
            return "clearing-service";
        if (path.startsWith("/api/v2/returns"))
            return "return-management";
        if (path.startsWith("/api/v1/notifications"))
            return "notification-service";
        if (path.startsWith("/api/v1/errors"))
            return "error-mapping";
        return "unknown";
    }

    public Map<String, Object> getMetrics() {
        return Map.of(
                "totalRequests", totalRequests.get(),
                "successfulRequests", successfulRequests.get(),
                "failedRequests", failedRequests.get(),
                "requestsPerRoute", requestsPerRoute,
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
    }

    @Override
    public int getOrder() {
        return -100; // Run early in the filter chain
    }
}
