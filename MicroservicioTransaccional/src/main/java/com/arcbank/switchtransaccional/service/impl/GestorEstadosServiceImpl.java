package com.arcbank.switch.service.impl;

import com.arcbank.switch.model.dto.TransaccionRequest;
import com.arcbank.switch.model.entity.EntidadBancariaEntity;
import com.arcbank.switch.model.entity.TransaccionEntity;
import com.arcbank.switch.repository.EntidadBancariaRepository;
import com.arcbank.switch.repository.TransaccionRepository;
import com.arcbank.switch.service.IGestorEstadosService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GestorEstadosServiceImpl implements IGestorEstadosService {

    private final TransaccionRepository transaccionRepository;
    private final EntidadBancariaRepository entidadBancariaRepository;

    @Override
    @Transactional
    public TransaccionEntity crearTransaccionRecibida(TransaccionRequest request) {

        // 1) Obtener el objeto interno Transaccion
        TransaccionRequest.Transaccion t = request.getTransaccion();

        // 2) Validaciones de negocio extra (además de las @Valid):
        if (t.getMonto().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a 0");
        }

        if (t.getIdBancoOrigen().equals(t.getIdBancoDestino())) {
            throw new IllegalArgumentException("IdBancoOrigen e IdBancoDestino no pueden ser iguales");
        }

        // 3) Verificar bancos en BD
        EntidadBancariaEntity bancoOrigen = entidadBancariaRepository.findById(t.getIdBancoOrigen())
                .orElseThrow(() -> new EntityNotFoundException("Banco origen no existe"));

        EntidadBancariaEntity bancoDestino = entidadBancariaRepository.findById(t.getIdBancoDestino())
                .orElseThrow(() -> new EntityNotFoundException("Banco destino no existe"));

        // 4) Validar estado de bancos
        if ("SUSPENDIDO".equalsIgnoreCase(bancoOrigen.getEstado())) {
            throw new IllegalStateException("BANCO_ORIGEN_SUSPENDIDO");
        }
        if ("SUSPENDIDO".equalsIgnoreCase(bancoDestino.getEstado())) {
            throw new IllegalStateException("BANCO_DESTINO_SUSPENDIDO");
        }

        // 5) Mapear al entity
        TransaccionEntity entity = new TransaccionEntity();
        entity.setEndToEnd(t.getEndToEnd());
        entity.setTraceId(UUID.randomUUID().toString());
        entity.setIdBancoOrigen(t.getIdBancoOrigen());
        entity.setIdBancoDestino(t.getIdBancoDestino());
        entity.setCuentaOrigen(t.getCuentaOrigen());
        entity.setCuentaDestino(t.getCuentaDestino());
        entity.setMonto(t.getMonto());
        entity.setMensaje(t.getMensaje());

        // Cambiar estado de "Enviado" -> "RECIBIDO"
        entity.setEstadoActual("RECIBIDO");

        // FechaCreacion: usar la que viene o la actual
        if (t.getFechaCreacion() != null) {
            entity.setFechaCreacion(t.getFechaCreacion());
        } else {
            entity.setFechaCreacion(LocalDateTime.now());
        }

        // CodigoRespuestaFinal se queda null al inicio
        entity.setCodigoRespuestaFinal(null);

        // 6) Guardar en BD (INSERT) y devolver con IdInstruccion generado
        return transaccionRepository.save(entity);
    }

    @Override
    @Transactional
    public void actualizarEstado(Integer idInstruccion, String nuevoEstado, String codigoRespuesta) {
        TransaccionEntity entity = transaccionRepository.findById(idInstruccion)
                .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada"));

        entity.setEstadoActual(nuevoEstado);

        if (codigoRespuesta != null) {
            entity.setCodigoRespuestaFinal(codigoRespuesta);
        }

        transaccionRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransaccionEntity> obtenerPorId(Integer idInstruccion) {
        return transaccionRepository.findById(idInstruccion);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransaccionEntity> obtenerPorEndToEnd(String endToEnd) {
        return transaccionRepository.findByEndToEnd(endToEnd);
    }
}
