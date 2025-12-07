package com.arcbank.switch.controller;

import com.arcbank.switch.model.dto.TransaccionRequest;
import com.arcbank.switch.model.dto.TransaccionResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para recepción de transacciones.
 * PERSONA 1: Implementa los endpoints de recepción, validación y persistencia.
 * 
 * Endpoints a implementar:
 * - POST /api/transacciones : Recibir nueva transacción
 * - GET /api/transacciones/{id} : Consultar estado de transacción
 * - GET /api/transacciones/endtoend/{endtoend} : Consultar por EndToEnd
 */
@Slf4j
@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    // TODO PERSONA 1: Inyectar dependencias necesarias
    // - TransaccionRepository
    // - IGestorEstadosService (tu implementación)
    // - IEnrutamientoService (implementación de Persona 2)

    /**
     * Endpoint para recibir nuevas transacciones.
     * PERSONA 1: Implementar la lógica de validación, persistencia y llamada a enrutamiento.
     */
    @PostMapping
    public ResponseEntity<TransaccionResponse> recibirTransaccion(
            @Valid @RequestBody TransaccionRequest request) {
        
        log.info("Recibida nueva transacción con EndToEnd: {}", 
                 request.getTransaccion().getEndToEnd());
        
        // TODO PERSONA 1: Implementar lógica
        // 1. Validar datos del request
        // 2. Verificar que EndToEnd no exista (evitar duplicados)
        // 3. Persistir la transacción en estado "Enviado"
        // 4. Llamar a enrutamientoService.procesarTransaccion(idInstruccion)
        // 5. Retornar respuesta
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(TransaccionResponse.builder()
                        .mensajeRespuesta("TODO: Implementar por Persona 1")
                        .build());
    }

    /**
     * Endpoint para consultar una transacción por ID.
     * PERSONA 1: Implementar consulta.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransaccionResponse> consultarTransaccion(@PathVariable Integer id) {
        log.info("Consultando transacción con ID: {}", id);
        
        // TODO PERSONA 1: Implementar consulta
        
        return ResponseEntity.ok(TransaccionResponse.builder()
                .mensajeRespuesta("TODO: Implementar por Persona 1")
                .build());
    }

    /**
     * Endpoint para consultar una transacción por EndToEnd.
     * PERSONA 1: Implementar consulta.
     */
    @GetMapping("/endtoend/{endtoend}")
    public ResponseEntity<TransaccionResponse> consultarPorEndToEnd(@PathVariable String endtoend) {
        log.info("Consultando transacción con EndToEnd: {}", endtoend);
        
        // TODO PERSONA 1: Implementar consulta
        
        return ResponseEntity.ok(TransaccionResponse.builder()
                .mensajeRespuesta("TODO: Implementar por Persona 1")
                .build());
    }
}
