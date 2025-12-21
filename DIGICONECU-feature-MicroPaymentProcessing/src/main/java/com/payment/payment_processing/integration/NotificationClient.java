package com.payment.payment_processing.integration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Cliente para comunicación con Notification Service
 */
@Slf4j
@Component
public class NotificationClient {

    private final RestClient notificationClient;

    public NotificationClient(@Qualifier("notificationHttpClient") RestClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    /**
     * Notifica el resultado de una transacción al banco destino
     */
    public void notificarTransaccion(String bancoDestino, String instructionId,
            String estado, BigDecimal monto, String cuentaDestino) {
        log.info("Notificando transacción {} al banco {}", instructionId, bancoDestino);

        try {
            Map<String, Object> request = Map.of(
                    "destino", bancoDestino,
                    "tipoNotificacion", "TRANSFERENCIA_" + estado,
                    "payload", Map.of(
                            "instructionId", instructionId,
                            "estado", estado,
                            "monto", monto,
                            "cuentaDestino", cuentaDestino),
                    "maxReintentos", 4);

            notificationClient.post()
                    .uri("/api/v1/notifications")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Notificación enviada exitosamente a {}", bancoDestino);
        } catch (Exception e) {
            log.warn("Error enviando notificación a {}: {}", bancoDestino, e.getMessage());
            // Las notificaciones son fire-and-forget, no fallan la transacción
        }
    }

    /**
     * Notifica un timeout
     */
    public void notificarTimeout(String bancoOrigen, String instructionId) {
        log.info("Notificando timeout de TX {} al banco {}", instructionId, bancoOrigen);

        try {
            Map<String, Object> request = Map.of(
                    "destino", bancoOrigen,
                    "tipoNotificacion", "TRANSFERENCIA_TIMEOUT",
                    "payload", Map.of(
                            "instructionId", instructionId,
                            "estado", "TIMEOUT",
                            "mensaje", "La transacción excedió el tiempo de espera"));

            notificationClient.post()
                    .uri("/api/v1/notifications")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Error enviando notificación de timeout: {}", e.getMessage());
        }
    }
}
