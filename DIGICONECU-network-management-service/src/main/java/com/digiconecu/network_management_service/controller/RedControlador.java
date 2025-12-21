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
@RequestMapping("/api/v1/red")
public class RedControlador {

    private final RedServicio redServicio;

    public RedControlador(RedServicio redServicio) {
        this.redServicio = redServicio;
    }

    /**
     * RF-02: Punto de entrada crítico para resolver la ruta de una transferencia.
     * Es consumido principalmente por el microservicio de Procesamiento de Pagos.
     */
    @PostMapping("/enrutamiento")
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

    // ============ CIRCUIT BREAKER ENDPOINTS ============

    /**
     * Registra un fallo para un banco (usado por Payment Processing)
     */
    @PostMapping("/bancos/{codigo}/fallos")
    public ResponseEntity<Map<String, Object>> registrarFallo(
            @PathVariable String codigo,
            @RequestBody Map<String, Object> request) {
        String tipoFallo = (String) request.getOrDefault("tipoFallo", "UNKNOWN");
        Long latenciaMs = request.containsKey("latenciaMs") ? ((Number) request.get("latenciaMs")).longValue() : null;

        redServicio.registrarFalloBanco(codigo, tipoFallo, latenciaMs);

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "mensaje", "Fallo registrado",
                "estadoCircuito", redServicio.obtenerEstadoCircuito(codigo)));
    }

    /**
     * Registra un éxito para un banco (resetea contador de fallos)
     */
    @PostMapping("/bancos/{codigo}/exito")
    public ResponseEntity<Map<String, Object>> registrarExito(
            @PathVariable String codigo,
            @RequestBody(required = false) Map<String, Object> request) {
        Long latenciaMs = request != null && request.containsKey("latenciaMs")
                ? ((Number) request.get("latenciaMs")).longValue()
                : null;

        redServicio.registrarExitoBanco(codigo, latenciaMs);

        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "mensaje", "Éxito registrado",
                "estadoCircuito", redServicio.obtenerEstadoCircuito(codigo)));
    }

    /**
     * Obtiene el estado del Circuit Breaker de un banco
     */
    @GetMapping("/bancos/{codigo}/circuit-breaker")
    public ResponseEntity<Map<String, Object>> estadoCircuitBreaker(@PathVariable String codigo) {
        return ResponseEntity.ok(Map.of(
                "bancoCodigo", codigo,
                "estadoCircuito", redServicio.obtenerEstadoCircuito(codigo),
                "permiteTráfico", redServicio.circuitoPermiteTráfico(codigo)));
    }

    /**
     * Obtiene estadísticas de Circuit Breaker de todos los bancos
     */
    @GetMapping("/circuit-breaker/stats")
    public ResponseEntity<List<Map<String, Object>>> estadisticasCircuitBreaker() {
        return ResponseEntity.ok(redServicio.obtenerEstadisticasCircuitBreaker());
    }

    // ============ BIN MANAGEMENT ============

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