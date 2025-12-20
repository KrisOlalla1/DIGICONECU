package com.microservices.errormapping.controller;

import com.microservices.errormapping.dto.*;
import com.microservices.errormapping.service.ErrorMappingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/error-mapping")
public class ErrorMappingController {

    private final ErrorMappingService errorMappingService;

    public ErrorMappingController(ErrorMappingService errorMappingService) {
        this.errorMappingService = errorMappingService;
    }

    @PostMapping("/traducir")
    public ResponseEntity<ErrorResponseDTO> traducir(@RequestBody ErrorRequestDTO request) {
        ErrorDataDTO data = errorMappingService.traducirError(
                request.getBancoCodigo(), 
                request.getCodigoOriginal()
        );
        
        return ResponseEntity.ok(ErrorResponseDTO.builder()
                .success(true)
                .data(data)
                .build());
    }

    @PostMapping("/agregar")
    public ResponseEntity<Map<String, String>> agregarMapeos(@RequestBody AddMappingRequestDTO request) {
        errorMappingService.agregarMapeo(request.getBancoCodigo(), request.getMapeos());
        return ResponseEntity.ok(Map.of("status", "OK", "mensaje", "Mapeos agregados correctamente"));
    }

    @GetMapping("/codigos-iso")
    public ResponseEntity<List<CodigoISODTO>> listarCodigosISO() {
        return ResponseEntity.ok(errorMappingService.listarCodigosISO());
    }

    @PostMapping("/reload")
    public ResponseEntity<Map<String, String>> recargarConfiguracion() {
        errorMappingService.cargarConfiguracion();
        return ResponseEntity.ok(Map.of("status", "OK", "mensaje", "Configuración recargada"));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
