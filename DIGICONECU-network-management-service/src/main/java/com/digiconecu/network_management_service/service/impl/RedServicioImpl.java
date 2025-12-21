package com.digiconecu.network_management_service.service.impl;

import com.digiconecu.network_management_service.dto.*;
import com.digiconecu.network_management_service.exception.*;
import com.digiconecu.network_management_service.model.*;
import com.digiconecu.network_management_service.repository.*;
import com.digiconecu.network_management_service.service.CircuitBreakerService;
import com.digiconecu.network_management_service.service.RedServicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RedServicioImpl implements RedServicio {

    private final BancoRepository bancoRepositorio;
    private final EnrutamientoRepository enrutamientoRepositorio;
    private final CircuitBreakerService circuitBreaker;

    public RedServicioImpl(BancoRepository bancoRepositorio,
            EnrutamientoRepository enrutamientoRepositorio,
            CircuitBreakerService circuitBreaker) {
        this.bancoRepositorio = bancoRepositorio;
        this.enrutamientoRepositorio = enrutamientoRepositorio;
        this.circuitBreaker = circuitBreaker;
    }

    @Override
    @Transactional(readOnly = true)
    public EnrutamientoRespuestaDto resolverEnrutamiento(EnrutamientoSolicitudDto solicitud) {
        // 1. Validar que la cuenta tenga al menos 6 dígitos para el BIN
        if (solicitud.getCuentaDestino() == null || solicitud.getCuentaDestino().length() < 6) {
            throw new ExcepcionNegocio("El número de cuenta es demasiado corto para identificar el banco.", "AC01");
        }

        // 2. Validar que solo contenga números
        if (!solicitud.getCuentaDestino().matches("^[0-9]+$")) {
            throw new ExcepcionNegocio("El número de cuenta solo debe contener dígitos.", "AC01");
        }

        // 3. Extraer el BIN (primeros 6 dígitos)
        String bin = solicitud.getCuentaDestino().substring(0, 6);

        // 4. Buscar el rango en la base de datos
        Enrutamiento enrutamiento = enrutamientoRepositorio.findByBinInRange(bin)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("AC01",
                        "No existe un banco registrado para el BIN: " + bin));

        // 5. Mapear la entidad a la respuesta DTO
        Banco banco = enrutamiento.getBanco();

        // 6. Verificar Circuit Breaker
        if (!circuitBreaker.permiteTráfico(banco.getCodigo())) {
            throw new ExcepcionNegocio("Banco " + banco.getCodigo() + " no disponible (Circuit Breaker OPEN)", "AC06");
        }

        EnrutamientoRespuestaDto respuesta = new EnrutamientoRespuestaDto();
        respuesta.setBancoCodigo(banco.getCodigo());
        respuesta.setBancoNombre(banco.getNombre());
        respuesta.setPuntoEnlace(banco.getEndpoint());

        // Determinar estado basado en Circuit Breaker
        String estadoCircuito = banco.getEstadoCircuito();
        if ("OPEN".equals(estadoCircuito)) {
            respuesta.setEstado("OFFLINE");
        } else if ("HALF_OPEN".equals(estadoCircuito)) {
            respuesta.setEstado("DEGRADED");
        } else {
            respuesta.setEstado(banco.getEstado());
        }

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
            dto.setEndpoint(b.getEndpoint());
            dto.setEstado(b.getEstado());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BancoDto obtenerBancoPorCodigo(String codigo) {
        Banco b = bancoRepositorio.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("AC01",
                        "No se encontró el banco con código: " + codigo));
        BancoDto dto = new BancoDto();
        dto.setId(b.getId());
        dto.setCodigo(b.getCodigo());
        dto.setNombre(b.getNombre());
        dto.setEndpoint(b.getEndpoint());
        dto.setEstado(b.getEstado());
        return dto;
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
        b.setEndpoint(dto.getEndpoint());
        b.setEstado("Activo");
        b = bancoRepositorio.save(b);
        dto.setId(b.getId());
        return dto;
    }

    @Override
    @Transactional
    public void actualizarEstadoBanco(String codigo, String nuevoEstado) {
        // Validar estados permitidos
        if (!List.of("Activo", "Suspendido", "Mantenimiento").contains(nuevoEstado)) {
            throw new ExcepcionNegocio("Estado inválido. Valores permitidos: Activo, Suspendido, Mantenimiento",
                    "VALIDATION_ERROR");
        }
        Banco b = bancoRepositorio.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("AC01",
                        "No se encontró el banco con código: " + codigo));
        b.setEstado(nuevoEstado);
        bancoRepositorio.save(b);
    }

    @Override
    @Transactional
    public void agregarRangoBin(String bancoCodigo, String binInicio, String binFin) {
        // Validar formato de BIN (6 dígitos)
        if (binInicio == null || !binInicio.matches("^[0-9]{6}$")) {
            throw new ExcepcionNegocio("BIN inicio debe tener exactamente 6 dígitos numéricos", "VALIDATION_ERROR");
        }
        if (binFin == null || !binFin.matches("^[0-9]{6}$")) {
            throw new ExcepcionNegocio("BIN fin debe tener exactamente 6 dígitos numéricos", "VALIDATION_ERROR");
        }

        // Validar que binInicio <= binFin
        if (binInicio.compareTo(binFin) > 0) {
            throw new ExcepcionNegocio("BIN inicio debe ser menor o igual a BIN fin", "VALIDATION_ERROR");
        }

        // Verificar que no haya superposición con rangos existentes
        if (enrutamientoRepositorio.existsOverlappingRange(binInicio, binFin)) {
            throw new ExcepcionNegocio("El rango BIN se superpone con un rango existente", "DUPL");
        }

        Banco banco = bancoRepositorio.findByCodigo(bancoCodigo)
                .orElseThrow(() -> new RecursoNoEncontradoExcepcion("AC01",
                        "No se encontró el banco con código: " + bancoCodigo));

        Enrutamiento enrutamiento = new Enrutamiento();
        enrutamiento.setBanco(banco);
        enrutamiento.setBinInicio(binInicio);
        enrutamiento.setBinFin(binFin);
        enrutamiento.setActivo(true);
        enrutamientoRepositorio.save(enrutamiento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarBins(String bancoCodigo) {
        List<Enrutamiento> enrutamientos;
        if (bancoCodigo != null && !bancoCodigo.isEmpty()) {
            enrutamientos = enrutamientoRepositorio.findByBancoCodigo(bancoCodigo);
        } else {
            enrutamientos = enrutamientoRepositorio.findAll();
        }

        return enrutamientos.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("bancoCodigo", e.getBanco().getCodigo());
            map.put("bancoNombre", e.getBanco().getNombre());
            map.put("binInicio", e.getBinInicio());
            map.put("binFin", e.getBinFin());
            map.put("activo", e.getActivo());
            return map;
        }).collect(Collectors.toList());
    }

    // ============ CIRCUIT BREAKER METHODS ============

    @Override
    @Transactional
    public void registrarFalloBanco(String bancoCodigo, String tipoFallo, Long latenciaMs) {
        circuitBreaker.registrarFallo(bancoCodigo, tipoFallo, latenciaMs);
    }

    @Override
    @Transactional
    public void registrarExitoBanco(String bancoCodigo, Long latenciaMs) {
        circuitBreaker.registrarExito(bancoCodigo, latenciaMs);
    }

    @Override
    public boolean circuitoPermiteTráfico(String bancoCodigo) {
        return circuitBreaker.permiteTráfico(bancoCodigo);
    }

    @Override
    public String obtenerEstadoCircuito(String bancoCodigo) {
        return circuitBreaker.obtenerEstadoCircuito(bancoCodigo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerEstadisticasCircuitBreaker() {
        return bancoRepositorio.findAll().stream().map(banco -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("bancoCodigo", banco.getCodigo());
            stats.put("bancoNombre", banco.getNombre());
            stats.put("estado", banco.getEstado());
            stats.put("estadoCircuito", banco.getEstadoCircuito() != null ? banco.getEstadoCircuito() : "CLOSED");
            stats.put("fallosConsecutivos", banco.getFallosConsecutivos() != null ? banco.getFallosConsecutivos() : 0);
            stats.put("latenciaPromedioMs", banco.getLatenciaPromedioMs() != null ? banco.getLatenciaPromedioMs() : 0);
            stats.put("ultimoFallo", banco.getUltimoFallo() != null ? banco.getUltimoFallo().toString() : null);
            stats.put("ultimoHealthCheck",
                    banco.getUltimoHealthCheck() != null ? banco.getUltimoHealthCheck().toString() : null);
            return stats;
        }).collect(Collectors.toList());
    }
}