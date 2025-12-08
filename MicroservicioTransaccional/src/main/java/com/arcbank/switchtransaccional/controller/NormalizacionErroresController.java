package com.arcbank.switchtransaccional.controller;

import com.arcbank.switchtransaccional.model.dto.NormalizarErrorResponse;
import com.arcbank.switchtransaccional.service.INormalizacionErroresService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/switch/errors")
@RequiredArgsConstructor
public class NormalizacionErroresController {

    private final INormalizacionErroresService normalizacionErroresService;

    /**
     * Endpoint de apoyo para probar la normalización de errores (TAREA 1.3).
     *
     * Ejemplo:
     * GET /api/v2/switch/errors/normalize?statusCode=404&errorMessage=Cuenta%20no%20existe
     */
    @GetMapping("/normalize")
    public ResponseEntity<NormalizarErrorResponse> normalizarError(
            @RequestParam int statusCode,
            @RequestParam(required = false) String errorMessage
    ) {

        String codigo = normalizacionErroresService.normalizarCodigoError(statusCode, errorMessage);

        NormalizarErrorResponse response = NormalizarErrorResponse.builder()
                .codigoRespuesta(codigo)
                .descripcion(descripcionCodigo(codigo))
                .build();

        return ResponseEntity.ok(response);
    }

    private String descripcionCodigo(String codigo) {
        if (codigo == null) return "Desconocido";

        return switch (codigo) {
            case "AC00" -> "COMPLETED - Transacción exitosa y encolada para liquidación.";
            case "AC01" -> "RJCT (Incorrect Account) - La cuenta destino no existe.";
            case "AC04" -> "RJCT (Closed Account) - La cuenta destino está cerrada.";
            case "AM04" -> "RJCT (Insufficient Funds) - El banco origen no tiene cupo/garantía.";
            case "AG01" -> "RJCT (Forbidden) - Transacción bloqueada por reglas de negocio.";
            case "MS03" -> "FAILED (Technical Error) - Error técnico/timeout con el banco destino.";
            case "DUPL" -> "RJCT (Duplicate) - Infracción de idempotencia (RF-03).";
            default -> "Error técnico.";
        };
    }
}


//Esto es independiente de tus endpoints de transacciones (/api/v2/switch/transfers).
//No afecta nada de Tarea 1.1 ni 1.2.