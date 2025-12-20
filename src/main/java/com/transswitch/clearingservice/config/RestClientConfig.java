package com.transswitch.clearingservice.config;

import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(IntegrationProperties.class)
public class RestClientConfig {

    @Bean
    public RestClient paymentProcessingRestClient(IntegrationProperties props) {
        JdkClientHttpRequestFactory rf = new JdkClientHttpRequestFactory();
        rf.setConnectTimeout(Duration.ofMillis(props.getPaymentProcessing().getConnectTimeoutMs()));
        rf.setReadTimeout(Duration.ofMillis(props.getPaymentProcessing().getReadTimeoutMs()));
        return RestClient.builder()
                .baseUrl(props.getPaymentProcessing().getBaseUrl())
                .requestFactory(rf)
                .build();
    }
}
