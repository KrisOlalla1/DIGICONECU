package com.arcbank.switchtransaccional.config;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración del cliente HTTP para comunicación con bancos externos.
 * PERSONA 2: Usa este RestTemplate en IEnrutamientoService para enviar peticiones HTTP.
 */
@Configuration
public class HttpClientConfig {

    @Value("${http.client.connection.timeout:5000}")
    private int connectionTimeout;

    @Value("${http.client.socket.timeout:10000}")
    private int socketTimeout;

    @Value("${http.client.max.connections:100}")
    private int maxConnections;

    @Value("${http.client.max.per.route:20}")
    private int maxPerRoute;

    @Bean
    public HttpClient httpClient() {
        // Pool de conexiones
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(maxConnections);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);

        // Configuración de timeouts
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofMilliseconds(connectionTimeout))
                .setResponseTimeout(Timeout.ofMilliseconds(socketTimeout))
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    @Bean
    public RestTemplate restTemplate(HttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(factory);
    }
}
