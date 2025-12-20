package com.digiconecu.network_management_service.controller;

import com.digiconecu.network_management_service.dto.BancoDto;
import com.digiconecu.network_management_service.dto.EnrutamientoSolicitudDto;
import com.digiconecu.network_management_service.dto.EnrutamientoRespuestaDto;
import com.digiconecu.network_management_service.service.RedServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/routing")
public class RedControlador {

    private final RedServicio redServicio;

    public RedControlador(RedServicio redServicio) {
        this.redServicio = redServicio;
    }

    /**
     * RF-02: Punto de entrada crítico para resolver la ruta de una transferencia.
     * Es consumido principalmente por el microservicio de Procesamiento de Pagos.
     */
    @PostMapping("/resolve")
    public ResponseEntity<EnrutamientoRespuestaDto> resolver(@RequestBody EnrutamientoSolicitudDto solicitud) {
        return ResponseEntity.ok(redServicio.resolverEnrutamiento(solicitud));
    }

    /**
     * Obtiene información de un banco específico por su código.
     */
    @GetMapping("/bancos/{codigo}")
    public ResponseEntity<BancoDto> obtenerBanco(@PathVariable String codigo) {
        return ResponseEntity.ok(redServicio.obtenerBancoPorCodigo(codigo));
    }

    /**
     * Obtiene el directorio completo de bancos participantes de la red.
     */
    @GetMapping("/bancos")
    public ResponseEntity<List<BancoDto>> obtenerTodos() {
        return ResponseEntity.ok(redServicio.obtenerTodosLosBancos());
    }

    /**
     * Permite registrar una nueva institución financiera en el ecosistema.
     */
    @PostMapping("/bancos")
    public ResponseEntity<BancoDto> crear(@RequestBody BancoDto bancoDto) {
        return ResponseEntity.ok(redServicio.crearBanco(bancoDto));
    }

    /**
     * RF-02: Permite poner un banco en modo 'Solo recibir' o 'Mantenimiento'.
     * Esto actualiza el estado operativo sin dar de baja al banco.
     */
    @PutMapping("/bancos/{codigo}/estado")
    public ResponseEntity<Void> actualizarEstado(
            @PathVariable String codigo,
            @RequestParam String nuevoEstado) {
        redServicio.actualizarEstadoBanco(codigo, nuevoEstado);
        return ResponseEntity.noContent().build();
    }

    /**
     * Agregar un nuevo rango BIN para un banco.
     */
    @PostMapping("/bins")
    public ResponseEntity<Map<String, Object>> agregarBin(@RequestBody Map<String, String> request) {
        String bancoCodigo = request.get("bancoCodigo");
        String binInicio = request.get("binInicio");
        String binFin = request.get("binFin");
        redServicio.agregarRangoBin(bancoCodigo, binInicio, binFin);
        return ResponseEntity.ok(Map.of("status", "OK", "mensaje", "Rango BIN agregado correctamente"));
    }

    /**
     * Listar rangos BIN de un banco específico.
     */
    @GetMapping("/bins")
    public ResponseEntity<List<Map<String, Object>>> listarBins(
            @RequestParam(required = false) String bancoCodigo) {
        return ResponseEntity.ok(redServicio.listarBins(bancoCodigo));
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "network-management-service",
                "timestamp", java.time.OffsetDateTime.now().toString()));
    }
}