package com.arcbank.switchtransaccional.service.impl;

import com.arcbank.switchtransaccional.model.dto.TransaccionResponse;
import com.arcbank.switchtransaccional.service.IEnrutamientoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de enrutamiento de transacciones.
 * PERSONA 2: Completar la implementación.
 */
@Slf4j
@Service
public class EnrutamientoServiceImpl implements IEnrutamientoService {

    // TODO PERSONA 2: Inyectar dependencias necesarias
    // - TransaccionRepository
    // - EntidadBancariaRepository
    // - IGestorEstadosService
    // - IIdempotenciaService
    // - RestTemplate (para HTTP)

    @Override
    public TransaccionResponse procesarTransaccion(Integer idInstruccion) {
        log.info("Iniciando enrutamiento de transacción {}", idInstruccion);
        
        // TODO PERSONA 2: Implementar
        // 1. Obtener la transacción desde BD
        // 2. Actualizar estado a "Procesando" usando gestorEstadosService
        // 3. Obtener información del banco destino desde EntidadBancariaRepository
        // 4. Preparar payload HTTP para el banco
        // 5. Enviar petición HTTP al endpoint del banco usando RestTemplate
        // 6. Procesar respuesta del banco
        // 7. Actualizar estado según resultado (Exitoso/Fallido)
        // 8. Guardar respuesta en Redis para idempotencia
        // 9. Retornar TransaccionResponse
        
        throw new UnsupportedOperationException("TODO: Implementar por Persona 2");
    }
}
