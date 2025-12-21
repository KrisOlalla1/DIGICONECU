package com.payment.payment_processing.integration;

import com.payment.payment_processing.integration.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Cliente para comunicación con Account Balance Service
 */
@Slf4j
@Component
public class AccountBalanceClient {

    private final RestClient balanceClient;

    public AccountBalanceClient(@Qualifier("balanceClient") RestClient balanceClient) {
        this.balanceClient = balanceClient;
    }

    /**
     * Verifica si el banco origen tiene saldo suficiente
     */
    public VerificarSaldoResponse verificarSaldo(String bancoCodigo, BigDecimal monto) {
        log.info("Verificando saldo para banco: {}, monto: {}", bancoCodigo, monto);

        try {
            VerificarSaldoRequest request = new VerificarSaldoRequest();
            request.setBancoCodigo(bancoCodigo);
            request.setMonto(monto);

            return balanceClient.post()
                    .uri("/api/v2/balance/verificar")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        log.error("Error verificando saldo: {} {}", resp.getStatusCode(), resp.getStatusText());
                        throw new IntegrationException("BALANCE_ERROR",
                                "Error consultando Account Balance: " + resp.getStatusCode());
                    })
                    .body(VerificarSaldoResponse.class);
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error de conexión con Account Balance Service", e);
            throw new IntegrationException("BALANCE_UNAVAILABLE",
                    "No se pudo conectar con Account Balance Service");
        }
    }

    /**
     * Congela fondos para una transacción
     */
    public CongelarFondosResponse congelarFondos(String bancoCodigo, BigDecimal monto, UUID instructionId) {
        log.info("Congelando fondos para banco: {}, monto: {}, tx: {}", bancoCodigo, monto, instructionId);

        try {
            CongelarFondosRequest request = new CongelarFondosRequest();
            request.setBancoCodigo(bancoCodigo);
            request.setMonto(monto);
            request.setInstructionId(instructionId);

            return balanceClient.post()
                    .uri("/api/v2/balance/congelar")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new IntegrationException("FREEZE_ERROR",
                                "Error congelando fondos: " + resp.getStatusCode());
                    })
                    .body(CongelarFondosResponse.class);
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error congelando fondos", e);
            throw new IntegrationException("BALANCE_UNAVAILABLE",
                    "No se pudo conectar con Account Balance Service");
        }
    }

    /**
     * Completa la transferencia (debita origen, acredita destino)
     */
    public CompletarTransferenciaResponse completarTransferencia(
            String bancoOrigen, String bancoDestino, BigDecimal monto, UUID instructionId) {
        log.info("Completando transferencia: {} -> {}, monto: {}", bancoOrigen, bancoDestino, monto);

        try {
            CompletarTransferenciaRequest request = new CompletarTransferenciaRequest();
            request.setBancoOrigen(bancoOrigen);
            request.setBancoDestino(bancoDestino);
            request.setMonto(monto);
            request.setInstructionId(instructionId);

            return balanceClient.post()
                    .uri("/api/v2/balance/completar")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        throw new IntegrationException("TRANSFER_ERROR",
                                "Error completando transferencia: " + resp.getStatusCode());
                    })
                    .body(CompletarTransferenciaResponse.class);
        } catch (IntegrationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error completando transferencia", e);
            throw new IntegrationException("BALANCE_UNAVAILABLE",
                    "No se pudo conectar con Account Balance Service");
        }
    }

    /**
     * Libera fondos congelados (en caso de fallo)
     */
    public void liberarFondos(String bancoCodigo, BigDecimal monto, UUID instructionId) {
        log.info("Liberando fondos para banco: {}, monto: {}, tx: {}", bancoCodigo, monto, instructionId);

        try {
            var request = new java.util.HashMap<String, Object>();
            request.put("bancoCodigo", bancoCodigo);
            request.put("monto", monto);
            request.put("instructionId", instructionId);

            balanceClient.post()
                    .uri("/api/v2/balance/liberar")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Error liberando fondos (compensación fallida)", e);
        }
    }
}
