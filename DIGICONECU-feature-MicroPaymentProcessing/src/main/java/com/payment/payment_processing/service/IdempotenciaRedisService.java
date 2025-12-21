package com.payment.payment_processing.service;

import com.payment.payment_processing.dto.TransferResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de Idempotencia usando Redis con TTL de 24 horas (RF-03)
 * 
 * Almacena el Instruction-ID en caché Redis con TTL automático.
 * Ante petición duplicada, recupera la respuesta original sin reprocesar.
 */
@Slf4j
@Service
public class IdempotenciaRedisService {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "idempotencia:";

    @Value("${idempotencia.ttl.hours:24}")
    private int ttlHours;

    public IdempotenciaRedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Verifica si ya existe una transacción con el mismo instructionId
     */
    public boolean existeTransaccion(UUID instructionId) {
        String key = KEY_PREFIX + instructionId.toString();
        Boolean exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            log.info("RF-03: InstructionId {} encontrado en caché Redis (TTL 24h)", instructionId);
        }
        return Boolean.TRUE.equals(exists);
    }

    /**
     * Guarda la respuesta de una transacción con TTL de 24 horas
     * Formato: estado|bancoDestino|timestamp
     */
    public void guardarRespuesta(UUID instructionId, TransferResponse response) {
        String key = KEY_PREFIX + instructionId.toString();
        try {
            // Serialización simple: estado|bancoDestino
            String value = buildValue(response);
            redisTemplate.opsForValue().set(key, value, Duration.ofHours(ttlHours));
            log.info("RF-03: Respuesta guardada en Redis para {} con TTL de {} horas", instructionId, ttlHours);
        } catch (Exception e) {
            log.error("Error guardando en Redis: {}", e.getMessage());
        }
    }

    /**
     * Recupera la respuesta cacheada de una transacción duplicada
     */
    public Optional<TransferResponse> recuperarRespuesta(UUID instructionId) {
        String key = KEY_PREFIX + instructionId.toString();
        String value = redisTemplate.opsForValue().get(key);

        if (value == null) {
            return Optional.empty();
        }

        try {
            TransferResponse response = parseValue(instructionId, value);
            log.info("RF-03: Respuesta recuperada de Redis para {} (sin reprocesar)", instructionId);
            return Optional.of(response);
        } catch (Exception e) {
            log.error("Error parseando respuesta de Redis: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Verifica si existe y recupera la respuesta en una sola operación
     */
    public Optional<TransferResponse> verificarYRecuperar(UUID instructionId) {
        if (existeTransaccion(instructionId)) {
            return recuperarRespuesta(instructionId);
        }
        return Optional.empty();
    }

    /**
     * Obtiene estadísticas del caché de idempotencia
     */
    public long contarEntradasActivas() {
        try {
            var keys = redisTemplate.keys(KEY_PREFIX + "*");
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            log.warn("Error contando entradas de idempotencia: {}", e.getMessage());
            return -1;
        }
    }

    private String buildValue(TransferResponse response) {
        if (response == null || response.getData() == null) {
            return "UNKNOWN|UNKNOWN";
        }
        String estado = response.getData().getEstado() != null ? response.getData().getEstado() : "UNKNOWN";
        String banco = response.getData().getBancoDestino() != null ? response.getData().getBancoDestino() : "UNKNOWN";
        return estado + "|" + banco;
    }

    private TransferResponse parseValue(UUID instructionId, String value) {
        String[] parts = value.split("\\|");
        String estado = parts.length > 0 ? parts[0] : "CACHE-HIT";
        String banco = parts.length > 1 ? parts[1] : "UNKNOWN";

        return TransferResponse.builder()
                .success(true)
                .data(TransferResponse.DataBody.builder()
                        .instructionId(instructionId)
                        .estado(estado)
                        .bancoDestino(banco)
                        .build())
                .build();
    }
}
