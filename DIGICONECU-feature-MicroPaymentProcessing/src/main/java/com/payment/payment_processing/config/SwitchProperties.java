package com.payment.payment_processing.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Propiedades de configuración del Switch Transaccional
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "switch")
public class SwitchProperties {

    /**
     * Timeout máximo para esperar respuesta del banco destino (en milisegundos)
     */
    private int timeoutMs = 3000;

    /**
     * Política de reintentos determinista (en milisegundos)
     * Default: [0, 800, 2000, 4000] = Intento inmediato, 800ms, 2s, 4s
     */
    private List<Long> retryDelays = List.of(0L, 800L, 2000L, 4000L);

    /**
     * Número máximo de reintentos
     */
    private int maxRetries = 4;

    /**
     * Límite máximo por transacción (USD)
     */
    private double maxTransactionAmount = 50000.00;

    /**
     * Monedas permitidas
     */
    private List<String> allowedCurrencies = List.of("USD", "EUR");

    /**
     * Latencia máxima tolerada (ms) - para monitoreo
     */
    private int maxLatencyMs = 200;

    /**
     * TPS objetivo mínimo
     */
    private int targetTps = 100;
}
