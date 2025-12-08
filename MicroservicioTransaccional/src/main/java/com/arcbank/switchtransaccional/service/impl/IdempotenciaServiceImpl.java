package com.arcbank.switchtransaccional.service.impl;

import com.arcbank.switchtransaccional.service.IIdempotenciaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Implementación del servicio de idempotencia con Redis.
 * PERSONA 2: Completar la implementación.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotenciaServiceImpl implements IIdempotenciaService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${redis.idempotencia.ttl:86400}")
    private long ttlSegundos;

    @Override
    public String obtenerRespuestaPrevia(String endToEnd) {
        log.debug("Buscando respuesta previa para EndToEnd: {}", endToEnd);
        
        // TODO PERSONA 2: Implementar
        // 1. Buscar en Redis con clave "switch:idempotencia:{endToEnd}"
        // 2. Retornar el valor si existe, null si no
        
        throw new UnsupportedOperationException("TODO: Implementar por Persona 2");
    }

    @Override
    public void guardarRespuesta(String endToEnd, String payload) {
        log.debug("Guardando respuesta en Redis para EndToEnd: {}", endToEnd);
        
        // TODO PERSONA 2: Implementar
        // 1. Guardar en Redis con clave "switch:idempotencia:{endToEnd}"
        // 2. Establecer TTL de ttlSegundos
        
        throw new UnsupportedOperationException("TODO: Implementar por Persona 2");
    }

    @Override
    public boolean existeTransaccion(String endToEnd) {
        log.debug("Verificando existencia de transacción: {}", endToEnd);
        
        // TODO PERSONA 2: Implementar
        // 1. Verificar si existe la clave en Redis
        
        throw new UnsupportedOperationException("TODO: Implementar por Persona 2");
    }
}
