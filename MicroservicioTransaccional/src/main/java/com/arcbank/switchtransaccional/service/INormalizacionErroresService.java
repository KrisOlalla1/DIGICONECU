package com.arcbank.switchtransaccional.service;

/**
 * Servicio para normalizar errores técnicos (HTTP + mensaje)
 * a códigos estándar del switch:
 *
 * AC00, AC01, AC04, AM04, AG01, MS03, DUPL.
 */
public interface INormalizacionErroresService {

    /**
     * Normaliza errores técnicos a códigos estándar.
     *
     * @param statusCode   Código HTTP devuelto por el banco destino (o simulado)
     * @param errorMessage Mensaje técnico devuelto (puede ser null)
     * @return Código normalizado: AC00, AC01, AC04, AM04, AG01, MS03, DUPL
     */
    String normalizarCodigoError(int statusCode, String errorMessage);
}
