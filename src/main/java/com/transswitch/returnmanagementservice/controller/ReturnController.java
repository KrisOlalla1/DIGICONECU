package com.transswitch.returnmanagementservice.controller;

import com.transswitch.returnmanagementservice.dto.ApiResponse;
import com.transswitch.returnmanagementservice.dto.ReturnCreateRequest;
import com.transswitch.returnmanagementservice.model.Devoluciones;
import com.transswitch.returnmanagementservice.service.ReturnManagementService;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/returns")
public class ReturnController {

    private final ReturnManagementService service;

    public ReturnController(ReturnManagementService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> crear(@Valid @RequestBody ReturnCreateRequest request) {
        Devoluciones d = service.crear(request);
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(service.toResponse(d), ts));
    }

    @GetMapping("/{returnInstructionId}")
    public ResponseEntity<ApiResponse<Object>> consultar(@PathVariable String returnInstructionId) {
        UUID id = UUID.fromString(returnInstructionId);
        Devoluciones d = service.consultar(id);
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(service.toResponse(d), ts));
    }
}
