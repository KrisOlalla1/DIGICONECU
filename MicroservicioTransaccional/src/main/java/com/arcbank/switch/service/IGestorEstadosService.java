package com.arcbank.switch.service;

/**
 * Interfaz para el servicio de gestión de estados de transacciones.
 * PERSONA 1: Implementa la gestión de estados y persistencia.
 * PERSONA 2: Llama a este servicio para actualizar estados después de comunicarse con bancos.
 */
public interface IGestorEstadosService {
    
    /**
     * Actualiza el estado de una transacción en la base de datos.
     * Estados posibles: "Enviado", "Procesando", "Exitoso", "Fallido", "Timeout"
     * 
     * @param idInstruccion ID de la transacción a actualizar
     * @param nuevoEstado Nuevo estado de la transacción
     * @param codigoRespuesta Código de respuesta del banco (opcional, puede ser null)
     * @throws RuntimeException si la transacción no existe
     */
    void actualizarEstado(Integer idInstruccion, String nuevoEstado, String codigoRespuesta);
    
    /**
     * Obtiene el estado actual de una transacción.
     * 
     * @param idInstruccion ID de la transacción
     * @return Estado actual de la transacción
     * @throws RuntimeException si la transacción no existe
     */
    String obtenerEstado(Integer idInstruccion);
}
