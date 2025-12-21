package com.payment.payment_processing.service;

import com.payment.payment_processing.config.SwitchProperties;
import com.payment.payment_processing.dto.TransferRequest;
import com.payment.payment_processing.dto.TransferResponse;
import com.payment.payment_processing.integration.*;
import com.payment.payment_processing.integration.dto.*;
import com.payment.payment_processing.model.IdempotenciaCache;
import com.payment.payment_processing.model.Transaccion;
import com.payment.payment_processing.repository.IdempotenciaRepository;
import com.payment.payment_processing.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceV2 {

    private final TransaccionRepository txRepo;
    private final IdempotenciaRepository idemRepo;
    private final IdempotenciaRedisService redisIdempotencia; // RF-03: Redis TTL 24h
    private final NetworkManagementClient networkClient;
    private final AccountBalanceClient balanceClient;
    private final NotificationClient notificationClient;
    private final SwitchProperties switchProps;

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private static final java.util.concurrent.atomic.AtomicLong totalTransactions = new java.util.concurrent.atomic.AtomicLong(
            0);
    private static final java.util.concurrent.atomic.AtomicLong totalLatencyMs = new java.util.concurrent.atomic.AtomicLong(
            0);

    @Transactional
    public TransferResponse procesarTransferencia(TransferRequest req) {
        long startTime = System.currentTimeMillis();
        OffsetDateTime timestampUTC = OffsetDateTime.now(ZoneOffset.UTC);

        UUID instructionId = req.getInstructionId() != null ? req.getInstructionId() : UUID.randomUUID();
        req.setInstructionId(instructionId);

        log.info("[{}] Iniciando procesamiento TX: {}", timestampUTC, instructionId);

        // RF-03: Verificación de idempotencia en Redis (TTL 24h) + PostgreSQL
        Optional<TransferResponse> cachedResponse = verificarIdempotencia(instructionId);
        if (cachedResponse.isPresent()) {
            log.info("RF-03: Intento duplicado TX {} - Retornando respuesta cacheada SIN reprocesar", instructionId);
            return cachedResponse.get();
        }

        TransferResponse validationError = validarReglasNegocio(req);
        if (validationError != null) {
            return validationError;
        }

        // Resolver enrutamiento ANTES de crear la transacción
        EnrutamientoResponse enrutamiento;
        try {
            enrutamiento = resolverEnrutamientoConReintentos(req.getCuentaDestino());
            if (enrutamiento == null || enrutamiento.getBancoCodigo() == null) {
                log.warn("No se pudo resolver enrutamiento para cuenta destino: {}", req.getCuentaDestino());
                return TransferResponse.builder()
                        .success(false)
                        .error(TransferResponse.ErrorBody.builder()
                                .code("RC01")
                                .message("No se pudo determinar el banco destino para la cuenta")
                                .build())
                        .build();
            }
        } catch (Exception e) {
            log.error("Error resolviendo enrutamiento: {}", e.getMessage());
            return TransferResponse.builder()
                    .success(false)
                    .error(TransferResponse.ErrorBody.builder()
                            .code("RC01")
                            .message("Error al resolver enrutamiento: " + e.getMessage())
                            .build())
                    .build();
        }

        Transaccion tx = crearTransaccionInicial(req, enrutamiento.getBancoCodigo(), timestampUTC);
        log.info("TX {} creada con estado RECIBIDA, banco destino: {}", instructionId, enrutamiento.getBancoCodigo());

        try {
            if (!"ONLINE".equalsIgnoreCase(enrutamiento.getEstado()) &&
                    !"Activo".equalsIgnoreCase(enrutamiento.getEstado())) {
                log.warn("Banco destino {} está OFFLINE", enrutamiento.getBancoCodigo());
                return marcarFallida(tx, "AC06", "Banco destino no disponible");
            }

            tx.setEstado("Enrutada");
            txRepo.save(tx);
            log.info("TX {} enrutada a banco {}", instructionId, enrutamiento.getBancoCodigo());

            VerificarSaldoResponse saldoResp = balanceClient.verificarSaldo(req.getBancoOrigen(), req.getMonto());
            if (!saldoResp.isAprobado()) {
                log.warn("Saldo insuficiente para TX {}: {}", instructionId, saldoResp.getMensaje());
                return marcarFallida(tx, saldoResp.getCodigoError() != null ? saldoResp.getCodigoError() : "AM04",
                        saldoResp.getMensaje());
            }

            CongelarFondosResponse freezeResp = balanceClient.congelarFondos(
                    req.getBancoOrigen(), req.getMonto(), instructionId);
            if (!freezeResp.isExito()) {
                log.warn("No se pudieron congelar fondos para TX {}", instructionId);
                return marcarFallida(tx, "AM04", freezeResp.getMensaje());
            }

            tx.setEstado("EsperandoRespuesta");
            txRepo.save(tx);
            log.info("TX {} en estado EsperandoRespuesta", instructionId);

            TransferResponse destinoResp = enviarABancoDestinoConTimeout(tx, enrutamiento);

            if (destinoResp != null && !destinoResp.isSuccess()) {
                balanceClient.liberarFondos(req.getBancoOrigen(), req.getMonto(), instructionId);
                return destinoResp;
            }

            CompletarTransferenciaResponse completeResp = balanceClient.completarTransferencia(
                    req.getBancoOrigen(), tx.getBancoDestinoCodigo(), req.getMonto(), instructionId);

            if (!completeResp.isExito()) {
                log.error("Error completando transferencia: {}", completeResp.getMensaje());
                return marcarFallida(tx, "MS03", completeResp.getMensaje());
            }

            tx.setEstado("Completada");
            txRepo.save(tx);
            log.info("TX {} COMPLETADA exitosamente", instructionId);

            notificationClient.notificarTransaccion(tx.getBancoDestinoCodigo(), instructionId.toString(),
                    "Completada", req.getMonto(), req.getCuentaDestino());

            TransferResponse successResponse = construirRespuestaExito(instructionId, "Completada",
                    tx.getBancoDestinoCodigo());

            // RF-03: Guardar en Redis (TTL 24h) + PostgreSQL
            guardarIdempotencia(instructionId, "Completada", successResponse);

            registrarMetricas(startTime);

            return successResponse;

        } catch (TimeoutException e) {
            log.error("TIMEOUT procesando TX: {}", instructionId);
            return marcarTimeout(tx);
        } catch (IntegrationException e) {
            log.error("Error de integración TX {}: {} - {}", instructionId, e.getCode(), e.getMessage());
            networkClient.reportarFallo(tx.getBancoDestinoCodigo(), e.getCode());
            return marcarFallida(tx, e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Error inesperado procesando TX: {}", instructionId, e);
            return marcarFallida(tx, "MS03", "Error interno: " + e.getMessage());
        }
    }

    private EnrutamientoResponse resolverEnrutamientoConReintentos(String cuentaDestino) throws TimeoutException {
        List<Long> delays = switchProps.getRetryDelays();
        int maxRetries = switchProps.getMaxRetries();
        Exception lastException = null;

        for (int intento = 0; intento < maxRetries; intento++) {
            try {
                if (intento < delays.size() && delays.get(intento) > 0) {
                    Thread.sleep(delays.get(intento));
                }

                log.debug("Intento {} de enrutamiento para cuenta {}", intento + 1, cuentaDestino);
                return networkClient.resolverEnrutamiento(cuentaDestino);

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new TimeoutException("Reintentos interrumpidos");
            } catch (Exception e) {
                lastException = e;
                log.warn("Intento {} fallido: {}", intento + 1, e.getMessage());
            }
        }

        throw new IntegrationException("ROUTING_FAILED",
                "Enrutamiento falló después de " + maxRetries + " intentos: " +
                        (lastException != null ? lastException.getMessage() : "desconocido"));
    }

    private TransferResponse enviarABancoDestinoConTimeout(Transaccion tx, EnrutamientoResponse enrutamiento)
            throws TimeoutException {

        Future<TransferResponse> future = executor.submit(() -> {
            log.info("Enviando TX {} al banco destino: {}", tx.getInstructionId(), enrutamiento.getPuntoEnlace());

            return TransferResponse.builder()
                    .success(true)
                    .data(TransferResponse.DataBody.builder()
                            .instructionId(tx.getInstructionId())
                            .estado("ACK")
                            .build())
                    .build();
        });

        try {
            return future.get(switchProps.getTimeoutMs(), TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new TimeoutException("Banco destino no respondió en " + switchProps.getTimeoutMs() + "ms");
        } catch (Exception e) {
            throw new IntegrationException("DESTINATION_ERROR",
                    "Error comunicando con banco destino: " + e.getMessage());
        }
    }

    private TransferResponse marcarTimeout(Transaccion tx) {
        tx.setEstado("Timeout");
        tx.setCodigoError("TIMEOUT");
        tx.setMensajeError("El banco destino no respondió en " + switchProps.getTimeoutMs() + "ms");
        txRepo.save(tx);

        notificationClient.notificarTimeout(tx.getBancoOrigenCodigo(), tx.getInstructionId().toString());

        log.warn("TX {} marcada como TIMEOUT", tx.getInstructionId());

        return TransferResponse.builder()
                .success(false)
                .error(TransferResponse.ErrorBody.builder()
                        .code("504")
                        .message("Gateway Timeout - El banco destino no respondió en el tiempo esperado")
                        .build())
                .build();
    }

    public TransferResponse consultarEstado(UUID instructionId) {
        Optional<Transaccion> txOpt = txRepo.findByInstructionId(instructionId);

        if (txOpt.isEmpty()) {
            return TransferResponse.builder()
                    .success(false)
                    .error(TransferResponse.ErrorBody.builder()
                            .code("404")
                            .message("Transacción no encontrada")
                            .build())
                    .build();
        }

        Transaccion tx = txOpt.get();

        if ("Timeout".equals(tx.getEstado()) || "EsperandoRespuesta".equals(tx.getEstado())) {
            log.info("Ejecutando consulta de sondeo para TX {} en estado {}", instructionId, tx.getEstado());

            try {
                if (tx.getBancoDestinoCodigo() != null) {
                    boolean bancoOnline = networkClient.verificarEstadoBanco(tx.getBancoDestinoCodigo());
                    if (bancoOnline) {
                        log.info("Banco {} está online, TX {} podría haberse completado",
                                tx.getBancoDestinoCodigo(), instructionId);
                    }
                }
            } catch (Exception e) {
                log.warn("Error en consulta de sondeo para TX {}: {}", instructionId, e.getMessage());
            }
        }

        return TransferResponse.builder()
                .success(true)
                .data(TransferResponse.DataBody.builder()
                        .instructionId(tx.getInstructionId())
                        .estado(tx.getEstado())
                        .monto(tx.getMonto())
                        .bancoOrigen(tx.getBancoOrigenCodigo())
                        .bancoDestino(tx.getBancoDestinoCodigo())
                        .fechaCreacion(tx.getFechaCreacion())
                        .build())
                .build();
    }

    public List<TransferResponse> listarTransacciones(String estado, String fecha) {
        List<Transaccion> transacciones;

        if (estado != null && fecha != null) {
            LocalDate fechaLocal = LocalDate.parse(fecha);
            transacciones = txRepo.findByEstadoAndFecha(estado, fechaLocal);
        } else if (estado != null) {
            transacciones = txRepo.findByEstado(estado);
        } else if (fecha != null) {
            LocalDate fechaLocal = LocalDate.parse(fecha);
            transacciones = txRepo.findByFecha(fechaLocal);
        } else {
            transacciones = txRepo.findAll();
        }

        return transacciones.stream()
                .map(tx -> TransferResponse.builder()
                        .success(true)
                        .data(TransferResponse.DataBody.builder()
                                .instructionId(tx.getInstructionId())
                                .estado(tx.getEstado())
                                .monto(tx.getMonto())
                                .bancoOrigen(tx.getBancoOrigenCodigo())
                                .bancoDestino(tx.getBancoDestinoCodigo())
                                .fechaCreacion(tx.getFechaCreacion())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    public java.util.Map<String, Object> obtenerMetricas() {
        long total = totalTransactions.get();
        long avgLatency = total > 0 ? totalLatencyMs.get() / total : 0;

        return java.util.Map.of(
                "totalTransacciones", total,
                "latenciaPromedioMs", avgLatency,
                "latenciaMaximaTolerableMs", switchProps.getMaxLatencyMs(),
                "tpsObjetivo", switchProps.getTargetTps(),
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString());
    }

    private TransferResponse validarReglasNegocio(TransferRequest req) {
        if (req.getMonto().compareTo(BigDecimal.valueOf(switchProps.getMaxTransactionAmount())) > 0) {
            log.warn("Monto excede límite máximo: {} > {}", req.getMonto(), switchProps.getMaxTransactionAmount());
            return TransferResponse.builder()
                    .success(false)
                    .error(TransferResponse.ErrorBody.builder()
                            .code("AM02")
                            .message("Monto excede límite máximo permitido: " + switchProps.getMaxTransactionAmount())
                            .build())
                    .build();
        }

        if (!switchProps.getAllowedCurrencies().contains(req.getMoneda())) {
            log.warn("Moneda no permitida: {}", req.getMoneda());
            return TransferResponse.builder()
                    .success(false)
                    .error(TransferResponse.ErrorBody.builder()
                            .code("AM03")
                            .message("Moneda no permitida. Permitidas: " + switchProps.getAllowedCurrencies())
                            .build())
                    .build();
        }

        return null;
    }

    private Transaccion crearTransaccionInicial(TransferRequest req, String bancoDestinoCodigo,
            OffsetDateTime timestamp) {
        Transaccion tx = new Transaccion();
        tx.setInstructionId(req.getInstructionId());
        tx.setEndToEndId(req.getEndToEndId());
        tx.setBancoOrigenCodigo(req.getBancoOrigen());
        tx.setBancoDestinoCodigo(bancoDestinoCodigo);
        tx.setCuentaOrigen(req.getCuentaOrigen());
        tx.setCuentaDestino(req.getCuentaDestino());
        tx.setMonto(req.getMonto());
        tx.setMoneda(req.getMoneda());
        tx.setEstado("Recibida");
        tx.setFechaCreacion(timestamp.toLocalDateTime());
        return txRepo.save(tx);
    }

    private TransferResponse marcarFallida(Transaccion tx, String codigo, String mensaje) {
        tx.setEstado("Fallida");
        tx.setCodigoError(codigo);
        tx.setMensajeError(mensaje);
        txRepo.save(tx);

        log.warn("TX {} marcada como FALLIDA: {} - {}", tx.getInstructionId(), codigo, mensaje);

        return TransferResponse.builder()
                .success(false)
                .error(TransferResponse.ErrorBody.builder()
                        .code(codigo)
                        .message(mensaje)
                        .build())
                .build();
    }

    /**
     * RF-03: Verificación de idempotencia en DOS capas
     * 1. Redis (caché rápida con TTL 24h)
     * 2. PostgreSQL (persistencia para recuperar respuesta original)
     */
    private Optional<TransferResponse> verificarIdempotencia(UUID instructionId) {
        // Capa 1: Verificar en Redis (rápido, TTL automático 24h)
        try {
            Optional<TransferResponse> redisResponse = redisIdempotencia.verificarYRecuperar(instructionId);
            if (redisResponse.isPresent()) {
                log.info("RF-03: InstructionId {} encontrado en REDIS (TTL 24h)", instructionId);
                return redisResponse;
            }
        } catch (Exception e) {
            log.warn("RF-03: Error consultando Redis, fallback a PostgreSQL: {}", e.getMessage());
        }

        // Capa 2: Verificar en PostgreSQL (persistente)
        if (idemRepo.existsById(instructionId)) {
            log.info("RF-03: InstructionId {} encontrado en PostgreSQL", instructionId);
            return Optional.of(recuperarRespuestaDePostgres(instructionId));
        }

        return Optional.empty();
    }

    private TransferResponse recuperarRespuestaDePostgres(UUID instructionId) {
        Optional<Transaccion> txOpt = txRepo.findByInstructionId(instructionId);
        if (txOpt.isPresent()) {
            Transaccion tx = txOpt.get();
            return construirRespuestaExito(tx.getInstructionId(), tx.getEstado(), tx.getBancoDestinoCodigo());
        }
        return construirRespuestaExito(instructionId, "CACHE-HIT", "UNKNOWN");
    }

    /**
     * RF-03: Guardar idempotencia en AMBAS capas
     * - Redis con TTL 24h (para verificación rápida)
     * - PostgreSQL (para persistencia y recuperar respuesta)
     */
    private void guardarIdempotencia(UUID instructionId, String estado, TransferResponse response) {
        // Capa 1: Guardar en Redis con TTL 24h
        try {
            redisIdempotencia.guardarRespuesta(instructionId, response);
            log.info("RF-03: Respuesta guardada en REDIS con TTL 24h para TX {}", instructionId);
        } catch (Exception e) {
            log.warn("RF-03: Error guardando en Redis (continuando con PostgreSQL): {}", e.getMessage());
        }

        // Capa 2: Guardar en PostgreSQL (persistente)
        IdempotenciaCache cache = new IdempotenciaCache();
        cache.setInstructionId(instructionId);
        cache.setRespuestaJson("{\"success\":true,\"estado\":\"" + estado + "\"}");
        cache.setFechaCreacion(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime());
        idemRepo.save(cache);
        log.info("RF-03: Respuesta guardada en PostgreSQL para TX {}", instructionId);
    }

    private void registrarMetricas(long startTime) {
        long latency = System.currentTimeMillis() - startTime;
        totalTransactions.incrementAndGet();
        totalLatencyMs.addAndGet(latency);

        if (latency > switchProps.getMaxLatencyMs()) {
            log.warn("Latencia alta detectada: {}ms (máximo: {}ms)", latency, switchProps.getMaxLatencyMs());
        }
    }

    private TransferResponse construirRespuestaExito(UUID id, String estado, String bancoDest) {
        return TransferResponse.builder()
                .success(true)
                .data(TransferResponse.DataBody.builder()
                        .instructionId(id)
                        .estado(estado)
                        .bancoDestino(bancoDest)
                        .timestamp(OffsetDateTime.now(ZoneOffset.UTC).toLocalDateTime())
                        .build())
                .build();
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void limpiarCacheIdempotencia() {
        var limite = OffsetDateTime.now(ZoneOffset.UTC).minusHours(24).toLocalDateTime();
        int eliminados = idemRepo.deleteByFechaCreacionBefore(limite);
        if (eliminados > 0) {
            log.info("Limpieza de idempotencia: {} registros eliminados", eliminados);
        }
    }
}
