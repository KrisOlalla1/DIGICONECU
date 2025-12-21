package com.transswitch.returnmanagementservice.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({ IntegrationProperties.class, ReturnRulesProperties.class })
public class RestClientConfig {

    @Bean
    public RestClient paymentProcessingRestClient(IntegrationProperties props) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(Duration.ofMillis(props.getPaymentProcessing().getConnectTimeoutMs()));
        rf.setReadTimeout(Duration.ofMillis(props.getPaymentProcessing().getReadTimeoutMs()));
        return RestClient.builder()
                .baseUrl(props.getPaymentProcessing().getBaseUrl())
                .requestFactory(rf)
                .build();
    }

    @Bean
    public RestClient accountBalanceRestClient(IntegrationProperties props) {
        SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
        rf.setConnectTimeout(Duration.ofMillis(props.getAccountBalance().getConnectTimeoutMs()));
        rf.setReadTimeout(Duration.ofMillis(props.getAccountBalance().getReadTimeoutMs()));
        return RestClient.builder()
                .baseUrl(props.getAccountBalance().getBaseUrl())
                .requestFactory(rf)
                .build();
    }
}
