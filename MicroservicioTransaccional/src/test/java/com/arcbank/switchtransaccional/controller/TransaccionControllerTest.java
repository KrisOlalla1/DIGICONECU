package com.arcbank.switchtransaccional.controller;

import com.arcbank.switchtransaccional.model.dto.TransaccionRequest;
import com.arcbank.switchtransaccional.model.entity.TransaccionEntity;
import com.arcbank.switchtransaccional.service.IGestorEstadosService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Desactiva filtros (como IdempotenciaFilter) en los tests
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Nuevo: usamos @MockitoBean en lugar de @MockBean (MockBean está deprecated)
    @MockitoBean
    private IGestorEstadosService gestorEstadosService;

    /**
     * Test 1: Validación exitosa
     * CrearTransferencia_DatosValidos_RetornaOk
     */
    @Test
    void CrearTransferencia_DatosValidos_RetornaOk() throws Exception {

        // 1. Preparamos el TransaccionEntity que el servicio devolverá
        TransaccionEntity entity = new TransaccionEntity();
        entity.setIdInstruccion(1);
        entity.setEndToEnd("E2E-TEST-OK-001");
        entity.setTraceId(UUID.randomUUID().toString());
        entity.setIdBancoOrigen(2);
        entity.setIdBancoDestino(1);
        entity.setCuentaOrigen("0102030405");
        entity.setCuentaDestino("9988776655");
        entity.setMonto(new BigDecimal("150.00"));
        entity.setEstadoActual("RECIBIDO");
        entity.setFechaCreacion(LocalDateTime.parse("2025-12-06T15:20:00"));

        Mockito.when(gestorEstadosService.crearTransaccionRecibida(any(TransaccionRequest.class)))
                .thenReturn(entity);

        // 2. JSON de entrada válido (formato que ya usas en Postman)
        String jsonRequest = """
                {
                  "Transaccion": {
                    "IdInstruccion": null,
                    "EndToEnd": "E2E-TEST-OK-001",
                    "IdBancoOrigen": 2,
                    "IdBancoDestino": 1,
                    "CuentaOrigen": "0102030405",
                    "CuentaDestino": "9988776655",
                    "Monto": 150.00,
                    "Mensaje": "Pago prueba",
                    "EstadoActual": "Enviado",
                    "FechaCreacion": "2025-12-06T15:20:00Z",
                    "CodigoRespuestaFinal": null
                  }
                }
                """;

        // 3. Ejecutar POST y validar respuesta
        mockMvc.perform(post("/api/v2/switch/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.IdInstruccion").value(1))
                .andExpect(jsonPath("$.EstadoActual").value("RECIBIDO"))
                .andExpect(jsonPath("$.Mensaje").value("Transacción recibida y en proceso"));
    }

    /**
     * Test 2: Monto negativo
     * CrearTransferencia_MontoNegativo_RetornaBadRequest
     */
    @Test
    void CrearTransferencia_MontoNegativo_RetornaBadRequest() throws Exception {

        String jsonRequest = """
                {
                  "Transaccion": {
                    "IdInstruccion": null,
                    "EndToEnd": "E2E-MONTO-NEG-TEST",
                    "IdBancoOrigen": 2,
                    "IdBancoDestino": 1,
                    "CuentaOrigen": "0102030405",
                    "CuentaDestino": "9988776655",
                    "Monto": -10.00,
                    "Mensaje": "Monto negativo",
                    "EstadoActual": "Enviado",
                    "FechaCreacion": "2025-12-06T15:20:00Z",
                    "CodigoRespuestaFinal": null
                  }
                }
                """;

        mockMvc.perform(post("/api/v2/switch/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.Mensaje").value("El monto debe ser mayor a 0"));

        // Aseguramos que el servicio NO fue llamado (falló antes por @Valid)
        verify(gestorEstadosService, never()).crearTransaccionRecibida(any(TransaccionRequest.class));
    }

    /**
     * Test 3: Banco origen suspendido
     * CrearTransferencia_BancoSuspendido_RetornaError
     */
    @Test
    void CrearTransferencia_BancoSuspendido_RetornaError() throws Exception {

        // Simulamos que el servicio lanza la excepción que ya implementaste:
        // IllegalStateException("BANCO_ORIGEN_SUSPENDIDO")
        Mockito.when(gestorEstadosService.crearTransaccionRecibida(any(TransaccionRequest.class)))
                .thenThrow(new IllegalStateException("BANCO_ORIGEN_SUSPENDIDO"));

        String jsonRequest = """
                {
                  "Transaccion": {
                    "IdInstruccion": null,
                    "EndToEnd": "E2E-ORIGEN-SUSP-TEST",
                    "IdBancoOrigen": 3,
                    "IdBancoDestino": 1,
                    "CuentaOrigen": "0102030405",
                    "CuentaDestino": "9988776655",
                    "Monto": 100.00,
                    "Mensaje": "Origen suspendido",
                    "EstadoActual": "Enviado",
                    "FechaCreacion": "2025-12-06T15:20:00Z",
                    "CodigoRespuestaFinal": null
                  }
                }
                """;

        mockMvc.perform(post("/api/v2/switch/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.Mensaje").value("BANCO_ORIGEN_SUSPENDIDO"));
    }
}
