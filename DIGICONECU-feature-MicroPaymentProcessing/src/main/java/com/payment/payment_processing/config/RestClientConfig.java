package com.payment.payment_processing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Configuración de clientes HTTP para comunicación inter-servicios
 */
@Configuration
public class RestClientConfig {

    @Value("${network.service.url:http://localhost:8082}")
    private String networkServiceUrl;

    @Value("${balance.service.url:http://localhost:8083}")
    private String balanceServiceUrl;

    @Value("${notification.service.url:http://localhost:8086}")
    private String notificationServiceUrl;

    @Value("${error.mapping.url:http://localhost:8087}")
    private String errorMappingUrl;

    @Value("${switch.timeoutMs:3000}")
    private int timeoutMs;

    @Bean("networkClient")
    public RestClient networkClient() {
        return createRestClient(networkServiceUrl);
    }

    @Bean("balanceClient")
    public RestClient balanceClient() {
        return createRestClient(balanceServiceUrl);
    }

    @Bean("notificationHttpClient")
    public RestClient notificationRestClient() {
        return createRestClient(notificationServiceUrl);
    }

    @Bean("errorMappingClient")
    public RestClient errorMappingClient() {
        return createRestClient(errorMappingUrl);
    }

    private RestClient createRestClient(String baseUrl) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }
}
