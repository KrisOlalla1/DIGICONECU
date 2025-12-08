package com.arcbank.switchtransaccional.service.impl;

import com.arcbank.switchtransaccional.service.INormalizacionErroresService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NormalizacionErroresServiceImpl implements INormalizacionErroresService {

    @Override
    public String normalizarCodigoError(int statusCode, String errorMessage) {

        String mensaje = (errorMessage == null) ? "" : errorMessage.toLowerCase();

        log.debug("Normalizando error. statusCode={}, errorMessage={}", statusCode, errorMessage);

        // ============================
        // 1) ÉXITO → AC00 (COMPLETED)
        // ============================
        if (statusCode >= 200 && statusCode < 300) {
            return "AC00";  // Transacción exitosa y encolada para liquidación
        }

        // ========================================
        // 2) DUPLICADO → DUPL (Idempotencia RF-03)
        // ========================================
        // Podemos detectar duplicado por:
        // - Código HTTP 409 (Conflict)
        // - Mensaje que contenga "duplic" (duplicada, duplicate, dupl, etc.)
        if (statusCode == 409
                || mensaje.contains("duplic")) {
            return "DUPL"; // Infracción de Idempotencia
        }

        // =======================================================
        // 3) BANCO SUSPENDIDO / REGLA DE NEGOCIO → AG01 (Forbidden)
        // =======================================================
        if (mensaje.contains("banco suspendido")
                || mensaje.contains("suspendido")
                || mensaje.contains("forbidden")
                || mensaje.contains("monto excede")
                || mensaje.contains("límite")
                || mensaje.contains("limite")) {
            return "AG01"; // Transacción prohibida por reglas de negocio
        }

        // ==================================================
        // 4) CUENTA INCORRECTA → AC01 (Incorrect Account)
        // ==================================================
        if (statusCode == 404
                || mensaje.contains("cuenta no existe")
                || mensaje.contains("account not found")) {
            return "AC01"; // El número de cuenta destino no existe
        }

        // ==============================================
        // 5) CUENTA CERRADA → AC04 (Closed Account)
        // ==============================================
        if (statusCode == 403
                || mensaje.contains("cuenta cerrada")
                || mensaje.contains("closed account")) {
            return "AC04"; // La cuenta destino está cerrada
        }

        // =======================================================
        // 6) FONDOS INSUFICIENTES → AM04 (Insufficient Funds)
        // =======================================================
        if (mensaje.contains("saldo insuficiente")
                || mensaje.contains("fondos insuficientes")
                || mensaje.contains("insufficient funds")) {
            return "AM04"; // El Banco Origen no tiene cupo/garantía
        }

        // =============================================================
        // 7) TIMEOUT / ERROR TÉCNICO → MS03 (Technical Error)
        // =============================================================
        // Timeout definitivo, errores 500, 504, 408, etc.
        if (mensaje.contains("timeout")
                || mensaje.contains("tiempo de espera")
                || statusCode == 504
                || statusCode == 408
                || statusCode == 500) {
            return "MS03"; // Error técnico con el banco destino
        }

        // ==========================================
        // 8) Cualquier otro error → MS03 genérico
        // ==========================================
        return "MS03";
    }
}
