package com.payment.payment_processing.integration;

import com.payment.payment_processing.integration.dto.EnrutamientoRequest;
import com.payment.payment_processing.integration.dto.EnrutamientoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente para comunicación con Network Management Service
 */
@Slf4j
@Component
public class NetworkManagementClient {

    private final RestClient networkClient;

    public NetworkManagementClient(@Qualifier("networkClient") RestClient networkClient) {
        this.networkClient = networkClient;
    }

    /**
     * Resuelve el enrutamiento para una cuenta destino
     */
    public EnrutamientoResponse resolverEnrutamiento(String cuentaDestino) {
        log.info("Consultando enrutamiento para cuenta: {}", cuentaDestino);

        try {
            EnrutamientoRequest request = new EnrutamientoRequest();
            request.setCuentaDestino(cuentaDestino);

            return networkClient.post()
                    .uri("/api/v1/red/enrutamiento")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        log.error("Error en enrutamiento: {} {}", resp.getStatusCode(), resp.getStatusText());
                        throw new IntegrationException("NETWORK_ERROR",
                                "Error consultando Network Management: " + resp.getStatusCode());
                    })
                    .body(EnrutamientoResponse.class);
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error de conexión con Network Management", e);
            throw new IntegrationException("NETWORK_UNAVAILABLE",
                    "No se pudo conectar con Network Management Service");
        }
    }

    /**
     * Verifica si un banco está online
     */
    public boolean verificarEstadoBanco(String bancoCodigo) {
        try {
            var response = networkClient.get()
                    .uri("/api/v1/red/bancos/{codigo}", bancoCodigo)
                    .retrieve()
                    .body(java.util.Map.class);

            if (response != null && response.containsKey("estado")) {
                String estado = (String) response.get("estado");
                return "ONLINE".equalsIgnoreCase(estado) || "Activo".equalsIgnoreCase(estado);
            }
            return false;
        } catch (Exception e) {
            log.warn("No se pudo verificar estado del banco {}: {}", bancoCodigo, e.getMessage());
            return false;
        }
    }

    /**
     * Reporta un fallo al banco para Circuit Breaker
     */
    public void reportarFallo(String bancoCodigo, String tipoFallo) {
        try {
            networkClient.post()
                    .uri("/api/v1/red/bancos/{codigo}/fallos", bancoCodigo)
                    .body(java.util.Map.of("tipoFallo", tipoFallo, "timestamp",
                            java.time.OffsetDateTime.now().toString()))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Fallo reportado para banco {}: {}", bancoCodigo, tipoFallo);
        } catch (Exception e) {
            log.warn("No se pudo reportar fallo del banco {}: {}", bancoCodigo, e.getMessage());
        }
    }
}
