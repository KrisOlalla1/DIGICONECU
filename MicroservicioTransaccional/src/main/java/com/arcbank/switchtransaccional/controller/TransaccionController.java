package com.arcbank.switch.controller;

import com.arcbank.switch.model.dto.TransaccionRequest;
import com.arcbank.switch.model.dto.TransaccionResponse;
import com.arcbank.switch.model.entity.TransaccionEntity;
import com.arcbank.switch.service.IGestorEstadosService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v2/switch/transfers")
@RequiredArgsConstructor
public class TransaccionController {

    private final IGestorEstadosService gestorEstadosService;

    @PostMapping
    public ResponseEntity<TransaccionResponse> crearTransferencia(
            @Valid @RequestBody TransaccionRequest request) {

        try {
            // Llama al servicio para validar y persistir
            TransaccionEntity entity = gestorEstadosService.crearTransaccionRecibida(request);

            // Armar respuesta
            TransaccionResponse response = TransaccionResponse.builder()
                    .idInstruccion(entity.getIdInstruccion())
                    .endToEnd(entity.getEndToEnd())
                    .traceId(entity.getTraceId())
                    .idBancoOrigen(entity.getIdBancoOrigen())
                    .idBancoDestino(entity.getIdBancoDestino())
                    .cuentaOrigen(entity.getCuentaOrigen())
                    .cuentaDestino(entity.getCuentaDestino())
                    .monto(entity.getMonto())
                    .mensaje(entity.getMensaje())
                    .estadoActual(entity.getEstadoActual()) // = RECIBIDO
                    .fechaCreacion(entity.getFechaCreacion())
                    .fechaProcesamiento(LocalDateTime.now())
                    .mensajeRespuesta("Transacción recibida y en proceso")
                    .exitoso(true)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException e) {
            // BANCO_ORIGEN_SUSPENDIDO / BANCO_DESTINO_SUSPENDIDO
            TransaccionResponse response = TransaccionResponse.builder()
                    .exitoso(false)
                    .mensajeRespuesta(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            TransaccionResponse response = TransaccionResponse.builder()
                    .exitoso(false)
                    .mensajeRespuesta(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
