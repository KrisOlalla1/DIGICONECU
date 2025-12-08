package com.arcbank.switchtransaccional.service;

import com.arcbank.switchtransaccional.model.dto.ConsultaEstadoTransaccionResponse;
import com.arcbank.switchtransaccional.model.dto.TransaccionRequest;
import com.arcbank.switchtransaccional.model.entity.TransaccionEntity;

public interface IGestorEstadosService {

    /**
     * TAREA 1.1:
     * Valida la transacción entrante, verifica bancos, EndToEnd y persiste con estado RECIBIDO.
     */
    TransaccionEntity crearTransaccionRecibida(TransaccionRequest request);

    /**
     * TAREA 1.2:
     * Actualiza el estado y opcionalmente el código de respuesta final.
     */
    void actualizarEstado(Integer idInstruccion, String nuevoEstado, String codigoRespuestaFinal);

    /**
     * Se usará luego en TAREA 1.4 para consultar el estado.
     */
    String obtenerEstado(Integer idInstruccion);

    // 🔹 TAREA 1.4: detalle de la transacción
    ConsultaEstadoTransaccionResponse consultarTransaccion(Integer idInstruccion);
}
