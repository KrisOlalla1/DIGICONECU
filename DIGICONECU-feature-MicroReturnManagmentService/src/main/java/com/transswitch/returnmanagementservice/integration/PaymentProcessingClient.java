package com.transswitch.returnmanagementservice.integration;

import com.transswitch.returnmanagementservice.dto.payment.TransferGetResponse;
import com.transswitch.returnmanagementservice.dto.payment.TransferCreateRequest;
import com.transswitch.returnmanagementservice.dto.payment.TransferCreateResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentProcessingClient {

    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingClient.class);

    private final RestClient restClient;

    public PaymentProcessingClient(RestClient paymentProcessingRestClient) {
        this.restClient = paymentProcessingRestClient;
    }

    public TransferGetResponse getTransfer(UUID instructionId) {
        return restClient.get()
                .uri("/api/v2/transfers/{id}", instructionId.toString())
                .retrieve()
                .body(TransferGetResponse.class);
    }

    /**
     * Crea una transacción inversa (devolución)
     * Invierte roles: el banco destino original se convierte en origen y viceversa
     */
    public TransferCreateResponse crearTransaccionInversa(
            String returnInstructionId,
            String originalInstructionId,
            String bancoOrigen,
            String bancoDestino,
            String cuentaOrigen,
            String cuentaDestino,
            BigDecimal monto,
            String moneda,
            String motivo) {

        log.info("Creando transacción inversa para devolución: {}", returnInstructionId);

        TransferCreateRequest request = new TransferCreateRequest();
        request.setInstructionId(returnInstructionId);
        request.setEndToEndId("RET-" + originalInstructionId);
        // Invertir roles: el que recibió ahora envía
        request.setBancoOrigen(bancoDestino);
        request.setCuentaOrigen(cuentaDestino);
        request.setCuentaDestino(cuentaOrigen);
        request.setMonto(monto);
        request.setMoneda(moneda != null ? moneda : "USD");
        request.setConcepto("DEVOLUCION: " + motivo + " - Ref: " + originalInstructionId);

        try {
            TransferCreateResponse response = restClient.post()
                    .uri("/api/v2/transfers")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> {
                        log.error("Error creando transacción inversa: {} {}", resp.getStatusCode(),
                                resp.getStatusText());
                    })
                    .body(TransferCreateResponse.class);

            log.info("Transacción inversa creada exitosamente: {}", returnInstructionId);
            return response;
        } catch (Exception e) {
            log.error("Error creando transacción inversa", e);
            throw new RuntimeException("Error creando transacción inversa: " + e.getMessage(), e);
        }
    }
}
