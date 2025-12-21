package com.digiconecu.network_management_service.service;

import com.digiconecu.network_management_service.model.Banco;
import com.digiconecu.network_management_service.repository.BancoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Servicio de Circuit Breaker para gestión de resiliencia
 * 
 * Estados del Circuito:
 * - CLOSED: Funcionamiento normal, tráfico permitido
 * - OPEN: Circuito abierto, NO se envía tráfico (después de 5 fallos
 * consecutivos)
 * - HALF_OPEN: Periodo de prueba, se permite tráfico limitado para verificar
 * recuperación
 * 
 * Reglas:
 * - 5 fallos consecutivos → OPEN
 * - Latencia > 4 segundos repetidamente → OPEN
 * - Health-check activo cada 30 segundos para bancos OPEN
 * - Si health-check exitoso → HALF_OPEN → CLOSED
 */
@Service
public class CircuitBreakerService {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerService.class);

    private final BancoRepository bancoRepository;

    @Value("${circuit-breaker.max-failures:5}")
    private int maxFailures;

    @Value("${circuit-breaker.max-latency-ms:4000}")
    private long maxLatencyMs;

    @Value("${circuit-breaker.recovery-timeout-seconds:60}")
    private int recoveryTimeoutSeconds;

    @Value("${circuit-breaker.health-check-timeout-ms:5000}")
    private int healthCheckTimeoutMs;

    public CircuitBreakerService(BancoRepository bancoRepository) {
        this.bancoRepository = bancoRepository;
    }

    /**
     * Registra un fallo para un banco
     * Si alcanza el límite de fallos consecutivos, abre el circuito
     */
    @Transactional
    public void registrarFallo(String bancoCodigo, String tipoFallo, Long latenciaMs) {
        bancoRepository.findByCodigo(bancoCodigo).ifPresent(banco -> {
            int fallos = banco.getFallosConsecutivos() != null ? banco.getFallosConsecutivos() : 0;
            banco.setFallosConsecutivos(fallos + 1);
            banco.setUltimoFallo(OffsetDateTime.now(ZoneOffset.UTC));

            if (latenciaMs != null) {
                long promedio = banco.getLatenciaPromedioMs() != null ? banco.getLatenciaPromedioMs() : 0;
                banco.setLatenciaPromedioMs((promedio + latenciaMs) / 2);
            }

            log.warn("Fallo registrado para banco {}: {} (Total fallos: {})",
                    bancoCodigo, tipoFallo, banco.getFallosConsecutivos());

            // Verificar si debe abrirse el circuito
            boolean debeAbrir = banco.getFallosConsecutivos() >= maxFailures;

            // También abrir si la latencia es muy alta repetidamente
            if (latenciaMs != null && latenciaMs > maxLatencyMs && fallos >= 3) {
                debeAbrir = true;
                log.warn("Latencia alta detectada para banco {}: {}ms", bancoCodigo, latenciaMs);
            }

            if (debeAbrir && !"OPEN".equals(banco.getEstadoCircuito())) {
                abrirCircuito(banco);
            }

            bancoRepository.save(banco);
        });
    }

    /**
     * Registra un éxito para un banco
     * Resetea contador de fallos y cierra el circuito si estaba HALF_OPEN
     */
    @Transactional
    public void registrarExito(String bancoCodigo, Long latenciaMs) {
        bancoRepository.findByCodigo(bancoCodigo).ifPresent(banco -> {
            banco.setFallosConsecutivos(0);

            if (latenciaMs != null) {
                long promedio = banco.getLatenciaPromedioMs() != null ? banco.getLatenciaPromedioMs() : 0;
                banco.setLatenciaPromedioMs((promedio + latenciaMs) / 2);
            }

            // Si estaba en HALF_OPEN, cerrar el circuito
            if ("HALF_OPEN".equals(banco.getEstadoCircuito())) {
                cerrarCircuito(banco);
            }

            bancoRepository.save(banco);
        });
    }

    /**
     * Verifica si el circuito permite tráfico para un banco
     */
    public boolean permiteTráfico(String bancoCodigo) {
        return bancoRepository.findByCodigo(bancoCodigo)
                .map(banco -> {
                    String estado = banco.getEstadoCircuito();
                    if (estado == null || "CLOSED".equals(estado)) {
                        return true;
                    }
                    if ("HALF_OPEN".equals(estado)) {
                        return true; // Permitir tráfico de prueba
                    }
                    if ("OPEN".equals(estado)) {
                        // Verificar si ya pasó el timeout de recuperación
                        if (banco.getCircuitoAbiertoDesde() != null) {
                            OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
                            if (ahora.isAfter(banco.getCircuitoAbiertoDesde().plusSeconds(recoveryTimeoutSeconds))) {
                                // Transicionar a HALF_OPEN
                                banco.setEstadoCircuito("HALF_OPEN");
                                bancoRepository.save(banco);
                                log.info("Circuito {} transicionando a HALF_OPEN", bancoCodigo);
                                return true;
                            }
                        }
                        return false;
                    }
                    return true;
                })
                .orElse(true);
    }

    /**
     * Obtiene el estado del circuito para un banco
     */
    public String obtenerEstadoCircuito(String bancoCodigo) {
        return bancoRepository.findByCodigo(bancoCodigo)
                .map(banco -> banco.getEstadoCircuito() != null ? banco.getEstadoCircuito() : "CLOSED")
                .orElse("UNKNOWN");
    }

    /**
     * Health-check activo para bancos con circuito OPEN
     * Se ejecuta cada 30 segundos
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void healthCheckActivo() {
        List<Banco> bancosAbiertos = bancoRepository.findByEstadoCircuito("OPEN");

        for (Banco banco : bancosAbiertos) {
            log.info("Ejecutando health-check activo para banco: {}", banco.getCodigo());

            boolean healthy = verificarSaludBanco(banco);
            banco.setUltimoHealthCheck(OffsetDateTime.now(ZoneOffset.UTC));

            if (healthy) {
                log.info("Health-check exitoso para banco {}, transicionando a HALF_OPEN", banco.getCodigo());
                banco.setEstadoCircuito("HALF_OPEN");
            } else {
                log.warn("Health-check fallido para banco {}", banco.getCodigo());
            }

            bancoRepository.save(banco);
        }
    }

    /**
     * Verifica la salud de un banco realizando una petición HTTP
     */
    private boolean verificarSaludBanco(Banco banco) {
        try {
            String endpoint = banco.getEndpoint();
            if (endpoint == null || endpoint.isEmpty()) {
                return false;
            }

            // Agregar /health al endpoint si no está presente
            String healthUrl = endpoint.endsWith("/") ? endpoint + "health" : endpoint + "/health";

            URI uri = URI.create(healthUrl);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(healthCheckTimeoutMs);
            conn.setReadTimeout(healthCheckTimeoutMs);

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode >= 200 && responseCode < 300;
        } catch (Exception e) {
            log.debug("Error en health-check para banco {}: {}", banco.getCodigo(), e.getMessage());
            return false;
        }
    }

    private void abrirCircuito(Banco banco) {
        banco.setEstadoCircuito("OPEN");
        banco.setCircuitoAbiertoDesde(OffsetDateTime.now(ZoneOffset.UTC));
        banco.setEstado("Suspendido"); // También marcar como suspendido
        log.error("CIRCUIT BREAKER ABIERTO para banco {}: {} fallos consecutivos",
                banco.getCodigo(), banco.getFallosConsecutivos());
    }

    private void cerrarCircuito(Banco banco) {
        banco.setEstadoCircuito("CLOSED");
        banco.setCircuitoAbiertoDesde(null);
        banco.setFallosConsecutivos(0);
        banco.setEstado("Activo");
        log.info("CIRCUIT BREAKER CERRADO para banco {}", banco.getCodigo());
    }
}
