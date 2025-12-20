package com.digiconecu.network_management_service.service;

import com.digiconecu.network_management_service.dto.BancoDto;
import com.digiconecu.network_management_service.dto.EnrutamientoSolicitudDto;
import com.digiconecu.network_management_service.dto.EnrutamientoRespuestaDto;
import java.util.List;

public interface RedServicio {
    // RF-02: Resuelve a qué banco enviar la transacción según el número de cuenta
    EnrutamientoRespuestaDto resolverEnrutamiento(EnrutamientoSolicitudDto solicitud);

    // Gestión del directorio de participantes
    List<BancoDto> obtenerTodosLosBancos();
    BancoDto crearBanco(BancoDto bancoDto);
    void actualizarEstadoBanco(String codigo, String nuevoEstado);
}