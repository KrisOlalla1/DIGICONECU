package com.transswitch.returnmanagementservice.integration;

import com.transswitch.returnmanagementservice.dto.payment.TransferGetResponse;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PaymentProcessingClient {

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
}
