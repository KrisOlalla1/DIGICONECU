package com.transswitch.clearingservice.integration;

import com.transswitch.clearingservice.dto.PaymentTransfersResponse;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentProcessingClient {

    private final RestClient restClient;

    public PaymentProcessingClient(RestClient paymentProcessingRestClient) {
        this.restClient = paymentProcessingRestClient;
    }

    public PaymentTransfersResponse getTransfersCompletadas(LocalDate fecha) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v2/transfers")
                        .queryParam("estado", "Completada")
                        .queryParam("fecha", fecha.toString())
                        .build())
                .retrieve()
                .body(PaymentTransfersResponse.class);
    }
}
