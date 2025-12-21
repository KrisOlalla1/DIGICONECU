package com.switchecu.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null) {
                return Mono.just(remoteAddress.getAddress().getHostAddress());
            }
            return Mono.just("anonymous");
        };
    }

    @Bean
    public KeyResolver clientIdKeyResolver() {
        return exchange -> {
            String clientId = exchange.getRequest().getHeaders().getFirst("X-Client-Id");
            if (clientId != null && !clientId.isEmpty()) {
                return Mono.just(clientId);
            }
            var remoteAddress = exchange.getRequest().getRemoteAddress();
            if (remoteAddress != null) {
                return Mono.just(remoteAddress.getAddress().getHostAddress());
            }
            return Mono.just("anonymous");
        };
    }
}
