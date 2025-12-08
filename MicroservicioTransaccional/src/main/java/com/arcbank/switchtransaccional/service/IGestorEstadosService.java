package com.arcbank.switch.service;

import com.arcbank.switch.model.dto.TransaccionRequest;
import com.arcbank.switch.model.entity.TransaccionEntity;

import java.util.Optional;

public interface IGestorEstadosService {

    // Crear transacción en estado RECIBIDO
    TransaccionEntity crearTransaccionRecibida(TransaccionRequest request);

    // Cambiar estado y opcionalmente código respuesta
    void actualizarEstado(Integer idInstruccion, String nuevoEstado, String codigoRespuesta);

    // Consultas
    Optional<TransaccionEntity> obtenerPorId(Integer idInstruccion);

    Optional<TransaccionEntity> obtenerPorEndToEnd(String endToEnd);
}
