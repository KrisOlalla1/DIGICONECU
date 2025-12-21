package com.nexus.ms_transacciones.service;

import com.nexus.ms_transacciones.dto.*;

public interface TransaccionService {
    RespuestaTransferenciaDTO realizarTransferencia(SolicitudTransferenciaDTO solicitud);
    void procesarPagoEntrante(SwitchTransaccionDTO dto);
}