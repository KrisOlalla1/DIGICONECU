package com.switchecu.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RestController
@RequestMapping
public class FallbackController {

    @GetMapping("/fallback/payment")
    public ResponseEntity<Map<String, Object>> paymentFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "error", Map.of(
                                "code", "503",
                                "message", "Payment Processing Service is temporarily unavailable"),
                        "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()));
    }

    @GetMapping("/fallback/network")
    public ResponseEntity<Map<String, Object>> networkFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "error", Map.of(
                                "code", "503",
                                "message", "Network Management Service is temporarily unavailable"),
                        "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()));
    }

    @GetMapping("/fallback/balance")
    public ResponseEntity<Map<String, Object>> balanceFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "success", false,
                        "error", Map.of(
                                "code", "503",
                                "message", "Account Balance Service is temporarily unavailable"),
                        "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "api-gateway",
                "version", "1.0.0",
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()));
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "service", "Switch Transaccional Ecuador - API Gateway",
                "version", "1.0.0",
                "endpoints", Map.of(
                        "payments", "/api/v2/transfers",
                        "network", "/api/v1/red",
                        "balance", "/api/v1/saldos",
                        "clearing", "/api/v2/clearing",
                        "returns", "/api/v2/returns",
                        "notifications", "/api/v1/notifications",
                        "errors", "/api/v1/errors"),
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()));
    }
}
