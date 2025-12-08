package com.arcbank.switchtransaccional.service;

import com.arcbank.switchtransaccional.model.dto.ConsultaEstadoTransaccionResponse;
import com.arcbank.switchtransaccional.model.dto.TransaccionRequest;
import com.arcbank.switchtransaccional.model.entity.TransaccionEntity;

public interface IGestorEstadosService {

    TransaccionEntity crearTransaccionRecibida(TransaccionRequest request);

    void actualizarEstado(Integer idInstruccion, String nuevoEstado, String codigoRespuestaFinal);

    String obtenerEstado(Integer idInstruccion);

    ConsultaEstadoTransaccionResponse consultarTransaccion(Integer idInstruccion);
}
