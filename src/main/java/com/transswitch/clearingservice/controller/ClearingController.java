package com.transswitch.clearingservice.controller;

import com.transswitch.clearingservice.dto.ApiResponse;
import com.transswitch.clearingservice.dto.ClearingExecuteRequest;
import com.transswitch.clearingservice.model.CiclosCompensacion;
import com.transswitch.clearingservice.service.ClearingService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.OffsetDateTime;
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
}
