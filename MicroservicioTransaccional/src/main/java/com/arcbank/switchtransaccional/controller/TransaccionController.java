package com.arcbank.switchtransaccional.controller;

import com.arcbank.switchtransaccional.model.dto.ActualizarEstadoRequest;
import com.arcbank.switchtransaccional.model.dto.TransaccionBaseResponse;
import com.arcbank.switchtransaccional.model.dto.TransaccionRequest;
import com.arcbank.switchtransaccional.model.entity.TransaccionEntity;
import com.arcbank.switchtransaccional.repository.TransaccionRepository;
import com.arcbank.switchtransaccional.service.IGestorEstadosService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.arcbank.switchtransaccional.model.dto.ConsultaEstadoTransaccionResponse;
import com.arcbank.switchtransaccional.model.dto.TransaccionBaseResponse;


@RestController
@RequestMapping("/api/v2/switch/transfers")
@RequiredArgsConstructor
public class TransaccionController {

    private final IGestorEstadosService gestorEstadosService;
    private final TransaccionRepository transaccionRepository;

    /**
     * =========================
     *  TAREA 1.1
     * =========================
     */
    @PostMapping
    public ResponseEntity<TransaccionBaseResponse> crearTransferencia(
            @Valid @RequestBody TransaccionRequest request) {

        try {
            TransaccionEntity entity = gestorEstadosService.crearTransaccionRecibida(request);

            TransaccionBaseResponse response = TransaccionBaseResponse.builder()
                    .success(true)
                    .idInstruccion(entity.getIdInstruccion())
                    .traceId(entity.getTraceId())
                    .estadoActual(entity.getEstadoActual())            // RECIBIDO
                    .mensaje("Transacción recibida y en proceso")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalStateException e) {
            // BANCO_ORIGEN_SUSPENDIDO / BANCO_DESTINO_SUSPENDIDO
            TransaccionBaseResponse response = TransaccionBaseResponse.builder()
                    .success(false)
                    .mensaje(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (IllegalArgumentException | EntityNotFoundException e) {
            TransaccionBaseResponse response = TransaccionBaseResponse.builder()
                    .success(false)
                    .mensaje(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * =========================
     *  TAREA 1.2
     *  Endpoint de apoyo para probar actualizarEstado
     * =========================
     */
    @PatchMapping("/{idInstruccion}/estado")
    public ResponseEntity<TransaccionBaseResponse> actualizarEstado(
            @PathVariable Integer idInstruccion,
            @RequestBody ActualizarEstadoRequest request) {

        try {
            gestorEstadosService.actualizarEstado(
                    idInstruccion,
                    request.getNuevoEstado(),
                    request.getCodigoRespuestaFinal()
            );

            TransaccionEntity entity = transaccionRepository.findById(idInstruccion)
                    .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada: " + idInstruccion));

            TransaccionBaseResponse response = TransaccionBaseResponse.builder()
                    .success(true)
                    .idInstruccion(entity.getIdInstruccion())
                    .traceId(entity.getTraceId())
                    .estadoActual(entity.getEstadoActual())
                    .mensaje("Estado actualizado correctamente")
                    .build();

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            TransaccionBaseResponse response = TransaccionBaseResponse.builder()
                    .success(false)
                    .mensaje(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (IllegalArgumentException e) {
            TransaccionBaseResponse response = TransaccionBaseResponse.builder()
                    .success(false)
                    .mensaje(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    /**
     * =========================
     *  TAREA 1.4 (RF-04)
     *  Consulta de estado por IdInstruccion
     * =========================
     */
    @GetMapping("/{idInstruccion}")
    public ResponseEntity<?> consultarEstadoTransaccion(
            @PathVariable Integer idInstruccion) {

        try {
            ConsultaEstadoTransaccionResponse detalle =
                    gestorEstadosService.consultarTransaccion(idInstruccion);

            return ResponseEntity.ok(detalle);

        } catch (EntityNotFoundException e) {
            // Reutilizamos el formato de error simple (success + mensaje)
            TransaccionBaseResponse error = TransaccionBaseResponse.builder()
                    .success(false)
                    .mensaje(e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

}
