package com.arcbank.switchtransaccional.service.impl;

import com.arcbank.switchtransaccional.model.dto.ConsultaEstadoTransaccionResponse;
import com.arcbank.switchtransaccional.model.dto.TransaccionRequest;
import com.arcbank.switchtransaccional.model.entity.EntidadBancariaEntity;
import com.arcbank.switchtransaccional.model.entity.TransaccionEntity;
import com.arcbank.switchtransaccional.repository.EntidadBancariaRepository;
import com.arcbank.switchtransaccional.repository.TransaccionRepository;
import com.arcbank.switchtransaccional.service.IGestorEstadosService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GestorEstadosServiceImpl implements IGestorEstadosService{

    private final TransaccionRepository transaccionRepository;
    private final EntidadBancariaRepository entidadBancariaRepository;

    @Override
    @Transactional
    public TransaccionEntity crearTransaccionRecibida(TransaccionRequest request) {

        if (request.getTransaccion() == null) {
            throw new IllegalArgumentException("El campo Transaccion es obligatorio");
        }

        TransaccionRequest.Transaccion t = request.getTransaccion();

        if (t.getEndToEnd() == null || t.getEndToEnd().isBlank()) {
            throw new IllegalArgumentException("El EndToEnd es obligatorio");
        }

        if (transaccionRepository.existsByEndToEnd(t.getEndToEnd())) {
            throw new IllegalArgumentException("Ya existe una transacción con EndToEnd " + t.getEndToEnd());
        }

        Integer idOrigen = t.getIdBancoOrigen();
        Integer idDestino = t.getIdBancoDestino();

        if (idOrigen == null) {
            throw new IllegalArgumentException("IdBancoOrigen es obligatorio");
        }
        if (idDestino == null) {
            throw new IllegalArgumentException("IdBancoDestino es obligatorio");
        }

        if (idOrigen.equals(idDestino)) {
            throw new IllegalArgumentException("IdBancoOrigen y IdBancoDestino no pueden ser iguales");
        }

        EntidadBancariaEntity bancoOrigen = entidadBancariaRepository.findById(idOrigen)
                .orElseThrow(() -> new EntityNotFoundException("IdBancoOrigen no existe: " + idOrigen));

        EntidadBancariaEntity bancoDestino = entidadBancariaRepository.findById(idDestino)
                .orElseThrow(() -> new EntityNotFoundException("IdBancoDestino no existe: " + idDestino));

        if ("SUSPENDIDO".equalsIgnoreCase(bancoOrigen.getEstado())) {
            throw new IllegalStateException("BANCO_ORIGEN_SUSPENDIDO");
        }

        if ("SUSPENDIDO".equalsIgnoreCase(bancoDestino.getEstado())) {
            throw new IllegalStateException("BANCO_DESTINO_SUSPENDIDO");
        }

        LocalDateTime fechaCreacion = t.getFechaCreacion();
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }

        String traceId = UUID.randomUUID().toString();

        TransaccionEntity entity = TransaccionEntity.builder()
                .endToEnd(t.getEndToEnd())
                .traceId(traceId)
                .idBancoOrigen(idOrigen)
                .idBancoDestino(idDestino)
                .cuentaOrigen(t.getCuentaOrigen())
                .cuentaDestino(t.getCuentaDestino())
                .monto(t.getMonto())
                .mensaje(t.getMensaje())
                .estadoActual("RECIBIDO")
                .fechaCreacion(fechaCreacion)
                .codigoRespuestaFinal(null)
                .build();


        TransaccionEntity guardada = transaccionRepository.save(entity);

        log.info("Transacción {} recibida y persistida con IdInstruccion={}",
                guardada.getEndToEnd(), guardada.getIdInstruccion());

        return guardada;
    }

    @Override
    @Transactional
    public void actualizarEstado(Integer idInstruccion, String nuevoEstado, String codigoRespuestaFinal) {

        TransaccionEntity entity = transaccionRepository.findById(idInstruccion)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada: " + idInstruccion));

        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new IllegalArgumentException("El nuevo estado es obligatorio");
        }

        log.info("Actualizando estado de IdInstruccion={} de {} a {}",
                idInstruccion, entity.getEstadoActual(), nuevoEstado);

        entity.setEstadoActual(nuevoEstado);


        if (codigoRespuestaFinal != null && !codigoRespuestaFinal.isBlank()) {
            entity.setCodigoRespuestaFinal(codigoRespuestaFinal);
        }

        if (esEstadoFinal(nuevoEstado)) {
            entity.setFechaProcesamiento(LocalDateTime.now());
        }

        transaccionRepository.save(entity);
    }

    private boolean esEstadoFinal(String estado) {
        String upper = estado.toUpperCase();
        return upper.equals("COMPLETADO")
                || upper.equals("EXITOSO")
                || upper.equals("FALLIDO")
                || upper.equals("TIMEOUT");
    }


    @Override
    @Transactional(readOnly = true)
    public ConsultaEstadoTransaccionResponse consultarTransaccion(Integer idInstruccion) {

        TransaccionEntity tx = transaccionRepository.findById(idInstruccion)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada: " + idInstruccion));

        EntidadBancariaEntity bancoOrigen = entidadBancariaRepository.findById(tx.getIdBancoOrigen())
                .orElseThrow(() -> new EntityNotFoundException("Banco origen no encontrado: " + tx.getIdBancoOrigen()));

        EntidadBancariaEntity bancoDestino = entidadBancariaRepository.findById(tx.getIdBancoDestino())
                .orElseThrow(() -> new EntityNotFoundException("Banco destino no encontrado: " + tx.getIdBancoDestino()));


        if ("TIMEOUT".equalsIgnoreCase(tx.getEstadoActual())) {
            log.warn("Transacción {} en estado TIMEOUT. Coordinar con Persona 2 / enrutamiento.",
                    tx.getIdInstruccion());
        }


        return ConsultaEstadoTransaccionResponse.builder()
                .idInstruccion(tx.getIdInstruccion())
                .endToEnd(tx.getEndToEnd())
                .traceId(tx.getTraceId())
                .estadoActual(tx.getEstadoActual())
                .codigoRespuestaFinal(tx.getCodigoRespuestaFinal())
                .fechaCreacion(tx.getFechaCreacion())
                .monto(tx.getMonto())
                .bancoOrigen(bancoOrigen.getNombre())
                .bancoDestino(bancoDestino.getNombre())
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public String obtenerEstado(Integer idInstruccion) {

        TransaccionEntity entity = transaccionRepository.findById(idInstruccion)
                .orElseThrow(() ->
                        new EntityNotFoundException("Transacción no encontrada: " + idInstruccion));

        return entity.getEstadoActual();
    }

}
