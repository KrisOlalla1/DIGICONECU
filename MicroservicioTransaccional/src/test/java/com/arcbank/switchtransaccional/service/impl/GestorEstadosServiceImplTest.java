package com.arcbank.switchtransaccional.service.impl;

import com.arcbank.switchtransaccional.model.entity.TransaccionEntity;
import com.arcbank.switchtransaccional.repository.EntidadBancariaRepository;
import com.arcbank.switchtransaccional.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GestorEstadosServiceImplTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private EntidadBancariaRepository entidadBancariaRepository;

    private GestorEstadosServiceImpl gestorEstadosService;

    @BeforeEach
    void setUp() {
        gestorEstadosService = new GestorEstadosServiceImpl(transaccionRepository, entidadBancariaRepository);
    }

    /**
     * Test 4: Actualización de estado
     * ActualizarEstado_TransaccionExiste_ActualizaCorrectamente
     */
    @Test
    void ActualizarEstado_TransaccionExiste_ActualizaCorrectamente() {

        // 1. Transacción existente en BD
        TransaccionEntity entity = new TransaccionEntity();
        entity.setIdInstruccion(10);
        entity.setEndToEnd("E2E-ESTADO-TEST");
        entity.setIdBancoOrigen(1);
        entity.setIdBancoDestino(2);
        entity.setCuentaOrigen("0102030405");
        entity.setCuentaDestino("9988776655");
        entity.setMonto(new BigDecimal("150.00"));
        entity.setEstadoActual("RECIBIDO");
        entity.setFechaCreacion(LocalDateTime.now().minusMinutes(5));

        when(transaccionRepository.findById(10)).thenReturn(Optional.of(entity));

        // 2. Ejecutar actualización de estado
        gestorEstadosService.actualizarEstado(10, "COMPLETADO", "AC00");

        // 3. Capturar lo que se guardó
        ArgumentCaptor<TransaccionEntity> captor = ArgumentCaptor.forClass(TransaccionEntity.class);
        verify(transaccionRepository, times(1)).save(captor.capture());

        TransaccionEntity guardada = captor.getValue();

        assertEquals("COMPLETADO", guardada.getEstadoActual());
        assertEquals("AC00", guardada.getCodigoRespuestaFinal());
        assertNotNull(guardada.getFechaProcesamiento(), "FechaProcesamiento debe setearse en estados finales");
    }
}
