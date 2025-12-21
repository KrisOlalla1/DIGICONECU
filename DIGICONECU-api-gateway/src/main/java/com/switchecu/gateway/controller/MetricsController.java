package com.switchecu.gateway.controller;

import com.switchecu.gateway.filter.MetricsFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller para exponer métricas del Gateway
 */
@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
public class MetricsController {

    private final MetricsFilter metricsFilter;

    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> metrics() {
        return ResponseEntity.ok(metricsFilter.getMetrics());
    }

    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> routes() {
        return ResponseEntity.ok(Map.of(
                "routes", Map.of(
                        "payment-processing", Map.of(
                                "path", "/api/v2/transfers/**",
                                "target", "http://payment-processing:8081",
                                "rateLimit", "100 req/s"),
                        "network-management", Map.of(
                                "path", "/api/v1/red/**",
                                "target", "http://network-management:8082",
                                "rateLimit", "50 req/s"),
                        "account-balance", Map.of(
                                "path", "/api/v1/saldos/**",
                                "target", "http://account-balance:8083",
                                "rateLimit", "100 req/s"),
                        "clearing-service", Map.of(
                                "path", "/api/v2/clearing/**",
                                "target", "http://clearing-service:8084",
                                "rateLimit", "20 req/s"),
                        "return-management", Map.of(
                                "path", "/api/v2/returns/**",
                                "target", "http://return-management:8085",
                                "rateLimit", "50 req/s"),
                        "notification-service", Map.of(
                                "path", "/api/v1/notifications/**",
                                "target", "http://notification-service:8086",
                                "rateLimit", "200 req/s"),
                        "error-mapping", Map.of(
                                "path", "/api/v1/errors/**",
                                "target", "http://error-mapping:8087",
                                "rateLimit", "100 req/s"))));
    }
}
