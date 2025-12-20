package com.digiconecu.network_management_service.service.impl;

import com.digiconecu.network_management_service.dto.*;
import com.digiconecu.network_management_service.exception.*;
import com.digiconecu.network_management_service.model.*;
import com.digiconecu.network_management_service.repository.*;
import com.digiconecu.network_management_service.service.RedServicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RedServicioImpl implements RedServicio {

    private final BancoRepository bancoRepositorio;
    private final EnrutamientoRepository enrutamientoRepositorio;

    public RedServicioImpl(BancoRepository bancoRepositorio, EnrutamientoRepository enrutamientoRepositorio) {
        this.bancoRepositorio = bancoRepositorio;
        this.enrutamientoRepositorio = enrutamientoRepositorio;
    }

    @Override
    @Transactional(readOnly = true)
    public EnrutamientoRespuestaDto resolverEnrutamiento(EnrutamientoSolicitudDto solicitud) {
        // 1. Validar que la cuenta tenga al menos 6 dígitos para el BIN [cite: 122]
        if (solicitud.getCuentaDestino() == null || solicitud.getCuentaDestino().length() < 6) {
            throw new RecursoNoEncontradoExcepcion("El número de cuenta es demasiado corto para identificar el banco.");
        }

        // 2. Extraer el BIN (primeros 6 dígitos) [cite: 122]
        String bin = solicitud.getCuentaDestino().substring(0, 6);

        // 3. Buscar el rango en la base de datos [cite: 107]
        Enrutamiento enrutamiento = enrutamientoRepositorio.findByBinInRange(bin)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("No existe un banco registrado para el BIN: " + bin));

        // 4. Mapear la entidad a la respuesta DTO
        Banco banco = enrutamiento.getBanco();
        EnrutamientoRespuestaDto respuesta = new EnrutamientoRespuestaDto();
        respuesta.setBancoCodigo(banco.getCodigo());
        respuesta.setBancoNombre(banco.getNombre());
        respuesta.setPuntoEnlace(banco.getEndpoint());
        respuesta.setEstado(banco.getEstado());

        return respuesta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BancoDto> obtenerTodosLosBancos() {
        return bancoRepositorio.findAll().stream().map(b -> {
            BancoDto dto = new BancoDto();
            dto.setId(b.getId());
            dto.setCodigo(b.getCodigo());
            dto.setNombre(b.getNombre());
            dto.setPuntoEnlace(b.getEndpoint());
            dto.setEstado(b.getEstado());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BancoDto crearBanco(BancoDto dto) {
        if (bancoRepositorio.existsByCodigo(dto.getCodigo())) {
            throw new ExcepcionNegocio("El código de banco ya se encuentra registrado.", "DUPL");
        }
        Banco b = new Banco();
        b.setCodigo(dto.getCodigo());
        b.setNombre(dto.getNombre());
        b.setEndpoint(dto.getPuntoEnlace());
        b.setEstado("Activo"); // Por defecto se crea activo [cite: 162]
        b = bancoRepositorio.save(b);
        dto.setId(b.getId());
        return dto;
    }

    @Override
    @Transactional
    public void actualizarEstadoBanco(String codigo, String nuevoEstado) {
        Banco b = bancoRepositorio.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("No se encontró el banco con código: " + codigo));
        b.setEstado(nuevoEstado); // Permite 'Suspendido' o 'Mantenimiento' [cite: 163]
        bancoRepositorio.save(b);
    }
}