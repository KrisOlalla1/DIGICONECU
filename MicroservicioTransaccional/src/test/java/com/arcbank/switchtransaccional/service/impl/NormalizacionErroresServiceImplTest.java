package com.arcbank.switchtransaccional.service.impl;

import com.arcbank.switchtransaccional.service.INormalizacionErroresService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NormalizacionErroresServiceImplTest {

    private final INormalizacionErroresService normalizacionErroresService =
            new NormalizacionErroresServiceImpl();

    @Test
    void NormalizarError_Timeout_RetornaMS03() {

        int statusCode = 504;
        String errorMessage = "timeout en respuesta del banco destino";

        String codigo = normalizacionErroresService.normalizarCodigoError(statusCode, errorMessage);

        assertEquals("MS03", codigo, "Para timeout debe devolver MS03 (Technical Error)");
    }
}
