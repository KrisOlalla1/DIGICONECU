package com.transswitch.clearingservice.controller;

import com.transswitch.clearingservice.dto.ApiResponse;
import com.transswitch.clearingservice.dto.ClearingExecuteRequest;
import com.transswitch.clearingservice.model.CiclosCompensacion;
import com.transswitch.clearingservice.service.ClearingService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/clearing")
public class ClearingController {

    private final ClearingService clearingService;

    public ClearingController(ClearingService clearingService) {
        this.clearingService = clearingService;
    }

    @PostMapping("/ejecutar")
    public ResponseEntity<ApiResponse<Object>> ejecutar(@Valid @RequestBody ClearingExecuteRequest request) {
        LocalDate fecha = LocalDate.parse(request.getFechaCiclo());
        CiclosCompensacion ciclo = clearingService.ejecutar(fecha);
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(clearingService.toResponse(ciclo), ts));
    }

    @GetMapping("/ciclos/{fecha}")
    public ResponseEntity<ApiResponse<Object>> consultar(@PathVariable String fecha) {
        LocalDate f = LocalDate.parse(fecha);
        CiclosCompensacion ciclo = clearingService.consultar(f);
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(clearingService.toResponse(ciclo), ts));
    }

    @GetMapping("/ciclos")
    public ResponseEntity<ApiResponse<Object>> listarCiclos(
            @RequestParam(defaultValue = "10") int limit) {
        List<CiclosCompensacion> ciclos = clearingService.listarUltimosCiclos(limit);
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(
                ciclos.stream().map(clearingService::toResponse).toList(), ts));
    }

    @GetMapping("/ultimo")
    public ResponseEntity<ApiResponse<Object>> obtenerUltimo() {
        CiclosCompensacion ciclo = clearingService.obtenerUltimoCiclo();
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(clearingService.toResponse(ciclo), ts));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "clearing-service",
                "timestamp", OffsetDateTime.now().toString()));
    }
}
