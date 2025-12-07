package com.arcbank.switch.service.impl;

import com.arcbank.switch.service.IGestorEstadosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de gestión de estados.
 * PERSONA 1: Completar la implementación.
 */
@Slf4j
@Service
public class GestorEstadosServiceImpl implements IGestorEstadosService {

    // TODO PERSONA 1: Inyectar TransaccionRepository

    @Override
    public void actualizarEstado(Integer idInstruccion, String nuevoEstado, String codigoRespuesta) {
        log.info("Actualizando estado de transacción {} a {}", idInstruccion, nuevoEstado);
        
        // TODO PERSONA 1: Implementar
        // 1. Buscar la transacción por ID
        // 2. Actualizar el estado
        // 3. Si codigoRespuesta no es null, actualizar CodigoRespuestaFinal
        // 4. Guardar cambios
        
        throw new UnsupportedOperationException("TODO: Implementar por Persona 1");
    }

    @Override
    public String obtenerEstado(Integer idInstruccion) {
        log.info("Obteniendo estado de transacción {}", idInstruccion);
        
        // TODO PERSONA 1: Implementar
        // 1. Buscar la transacción por ID
        // 2. Retornar el estado actual
        
        throw new UnsupportedOperationException("TODO: Implementar por Persona 1");
    }
}
