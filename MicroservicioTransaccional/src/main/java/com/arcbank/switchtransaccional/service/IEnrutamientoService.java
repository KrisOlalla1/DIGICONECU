package com.arcbank.switchtransaccional.service;

import com.arcbank.switchtransaccional.model.dto.TransaccionResponse;

public interface IEnrutamientoService {
    
    /**
     * Procesa el enrutamiento de una transacción al banco destino.
     * Este método debe:
     * 1. Obtener la información del banco destino desde EntidadBancaria
     * 2. Preparar el payload HTTP según el formato del banco
     * 3. Enviar la petición HTTP al endpoint del banco
     * 4. Procesar la respuesta y actualizar el estado de la transacción
     *
     * @param idInstruccion ID de la transacción a procesar
     * @return TransaccionResponse con el resultado del enrutamiento
     * @throws RuntimeException si hay errores en la comunicación HTTP
     */
    TransaccionResponse procesarTransaccion(Integer idInstruccion);
}
