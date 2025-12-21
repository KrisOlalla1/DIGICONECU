package com.microservices.notification.service;

import com.microservices.notification.dto.NotificationDataDTO;
import com.microservices.notification.dto.NotificationErrorDTO;
import com.microservices.notification.dto.NotificationResponseDTO;
import com.microservices.notification.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final List<Long> RETRY_DELAYS = List.of(0L, 800L, 2000L, 4000L);

    private final WebClient webClient;
    private final int defaultTimeoutMs;
    private final int maxRetries;

    public NotificationService(
            WebClient.Builder webClientBuilder,
            @Value("${notification.default-timeout-ms:5000}") int defaultTimeoutMs,
            @Value("${notification.max-retries:4}") int maxRetries) {
        this.webClient = webClientBuilder.build();
        this.defaultTimeoutMs = defaultTimeoutMs;
        this.maxRetries = maxRetries;
    }

    public NotificationResponseDTO enviar(NotificationRequest request) {
        long startTime = System.currentTimeMillis();
        int timeoutMs = request.getTimeoutMs() != null ? request.getTimeoutMs() : defaultTimeoutMs;
        int maxReintentos = request.getMaxReintentos() != null ? request.getMaxReintentos() : maxRetries;

        for (int intento = 0; intento < maxReintentos; intento++) {
            if (intento > 0) {
                long delay = RETRY_DELAYS.get(Math.min(intento, RETRY_DELAYS.size() - 1));
                log.info("Reintento #{} después de {}ms para endpoint: {}", intento + 1, delay, request.getEndpoint());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return buildErrorResponse("INTERRUPTED", intento + 1, System.currentTimeMillis() - startTime);
                }
            }

            log.info("Intento #{} - Enviando notificación a: {}", intento + 1, request.getEndpoint());

            try {
                Object response = webClient.post()
                        .uri(request.getEndpoint())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request.getPayload())
                        .retrieve()
                        .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                                Mono.error(new WebClientResponseException(
                                        clientResponse.statusCode().value(),
                                        "Client Error",
                                        null, null, null)))
                        .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                                Mono.error(new WebClientResponseException(
                                        clientResponse.statusCode().value(),
                                        "Server Error",
                                        null, null, null)))
                        .bodyToMono(Object.class)
                        .timeout(Duration.ofMillis(timeoutMs))
                        .block();

                long tiempoTotal = System.currentTimeMillis() - startTime;
                log.info("Notificación exitosa en intento #{}, tiempo total: {}ms", intento + 1, tiempoTotal);

                return NotificationResponseDTO.builder()
                        .success(true)
                        .data(NotificationDataDTO.builder()
                                .exitoso(true)
                                .codigoHttp(200)
                                .respuesta(response)
                                .intentosRealizados(intento + 1)
                                .tiempoTotal(tiempoTotal)
                                .build())
                        .build();

            } catch (WebClientResponseException e) {
                int statusCode = e.getStatusCode().value();
                log.warn("Intento #{} falló con código HTTP: {}", intento + 1, statusCode);

                if (statusCode >= 400 && statusCode < 500) {
                    long tiempoTotal = System.currentTimeMillis() - startTime;
                    log.error("Error 4xx - No se reintentará. Código: {}", statusCode);
                    return NotificationResponseDTO.builder()
                            .success(false)
                            .error(NotificationErrorDTO.builder()
                                    .exitoso(false)
                                    .error("HTTP_" + statusCode)
                                    .intentosRealizados(intento + 1)
                                    .tiempoTotal(tiempoTotal)
                                    .build())
                            .build();
                }

            } catch (Exception e) {
                log.warn("Intento #{} falló: {} - {}", intento + 1, e.getClass().getSimpleName(), e.getMessage());
            }
        }

        long tiempoTotal = System.currentTimeMillis() - startTime;
        log.error("Todos los reintentos fallaron. Total intentos: {}, tiempo: {}ms", maxReintentos, tiempoTotal);
        return buildErrorResponse("TIMEOUT", maxReintentos, tiempoTotal);
    }

    private NotificationResponseDTO buildErrorResponse(String errorCode, int intentos, long tiempoTotal) {
        return NotificationResponseDTO.builder()
                .success(false)
                .error(NotificationErrorDTO.builder()
                        .exitoso(false)
                        .error(errorCode)
                        .intentosRealizados(intentos)
                        .tiempoTotal(tiempoTotal)
                        .build())
                .build();
    }
}
