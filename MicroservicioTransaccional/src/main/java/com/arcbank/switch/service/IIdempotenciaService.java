package com.arcbank.switch.service;

/**
 * Interfaz para el servicio de idempotencia con Redis.
 * PERSONA 2: Implementa la lógica de caché con Redis.
 * PERSONA 2: También crea el middleware que usa este servicio.
 */
public interface IIdempotenciaService {
    
    /**
     * Busca una respuesta previa en Redis usando el EndToEnd como clave.
     * Si existe, significa que esta transacción ya fue procesada.
     * 
     * @param endToEnd Identificador único de la transacción (EndToEnd)
     * @return JSON de la respuesta previa, o null si no existe
     */
    String obtenerRespuestaPrevia(String endToEnd);
    
    /**
     * Guarda la respuesta de una transacción en Redis.
     * La clave será el EndToEnd y el valor será el JSON de respuesta.
     * TTL recomendado: 24 horas.
     * 
     * @param endToEnd Identificador único de la transacción
     * @param payload JSON de respuesta a guardar
     */
    void guardarRespuesta(String endToEnd, String payload);
    
    /**
     * Verifica si una transacción ya fue procesada.
     * 
     * @param endToEnd Identificador único de la transacción
     * @return true si ya existe en Redis, false en caso contrario
     */
    boolean existeTransaccion(String endToEnd);
}
