package com.transswitch.clearingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transswitch.clearingservice.dto.ClearingCycleResponse;
import com.transswitch.clearingservice.dto.PaymentTransferItem;
import com.transswitch.clearingservice.dto.PaymentTransfersResponse;
import com.transswitch.clearingservice.integration.PaymentProcessingClient;
import com.transswitch.clearingservice.model.CiclosCompensacion;
import com.transswitch.clearingservice.repository.CiclosCompensacionRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class ClearingService {

    private static final Logger log = LoggerFactory.getLogger(ClearingService.class);

    private final CiclosCompensacionRepository repository;
    private final PaymentProcessingClient paymentClient;
    private final ObjectMapper objectMapper;

    public ClearingService(CiclosCompensacionRepository repository,
            PaymentProcessingClient paymentClient,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.paymentClient = paymentClient;
        this.objectMapper = objectMapper;
    }

    public CiclosCompensacion ejecutar(LocalDate fechaCiclo) {
        log.info("Iniciando proceso de clearing para fecha: {}", fechaCiclo);

        if (repository.existsByFechaCiclo(fechaCiclo)) {
            throw new ServiceException("409", "CYCLE_ALREADY_EXISTS", "Ya existe ciclo para la fecha " + fechaCiclo);
        }

        PaymentTransfersResponse resp = paymentClient.getTransfersCompletadas(fechaCiclo);
        if (resp == null || !resp.isSuccess() || resp.getData() == null) {
            throw new ServiceException("503", "PAYMENT_SERVICE_UNAVAILABLE", "No se pudo consultar Payment Processing");
        }

        List<PaymentTransferItem> txs = resp.getData().getTransacciones();
        if (txs == null) {
            txs = List.of();
        }

        log.info("Procesando {} transacciones para clearing", txs.size());

        Map<String, BankTotals> neteos = new LinkedHashMap<>();

        for (PaymentTransferItem tx : txs) {
            if (tx == null || tx.getMonto() == null)
                continue;
            if (tx.getBancoOrigen() == null || tx.getBancoDestino() == null)
                continue;

            neteos.computeIfAbsent(tx.getBancoOrigen(),
                    k -> new BankTotals()).enviado = neteos.get(tx.getBancoOrigen()).enviado.add(tx.getMonto());

            neteos.computeIfAbsent(tx.getBancoDestino(),
                    k -> new BankTotals()).recibido = neteos.get(tx.getBancoDestino()).recibido.add(tx.getMonto());
        }

        Map<String, Map<String, Object>> posiciones = new LinkedHashMap<>();
        BigDecimal sumaNetos = BigDecimal.ZERO;
        BigDecimal volumenBruto = BigDecimal.ZERO;

        for (Map.Entry<String, BankTotals> e : neteos.entrySet()) {
            String banco = e.getKey();
            BankTotals t = e.getValue();
            BigDecimal neto = t.recibido.subtract(t.enviado);
            sumaNetos = sumaNetos.add(neto);
            volumenBruto = volumenBruto.add(t.enviado);

            Map<String, Object> pos = new LinkedHashMap<>();
            pos.put("Enviado", t.enviado);
            pos.put("Recibido", t.recibido);
            pos.put("Neto", neto);
            posiciones.put(banco, pos);

            log.info("Banco {}: Enviado={}, Recibido={}, Neto={}", banco, t.enviado, t.recibido, neto);
        }

        // Agregar totales
        Map<String, Object> totales = new LinkedHashMap<>();
        totales.put("VolumenBruto", volumenBruto);
        totales.put("SumaNetos", sumaNetos);
        posiciones.put("Totales", totales);

        if (sumaNetos.abs().compareTo(new BigDecimal("0.01")) > 0) {
            log.error("La suma de netos no cuadra: {}", sumaNetos);
            throw new ServiceException("422", "CLEARING_NOT_BALANCED", "La suma de netos no cuadra: " + sumaNetos);
        }

        String posicionesJson;
        try {
            posicionesJson = objectMapper.writeValueAsString(posiciones);
        } catch (Exception ex) {
            throw new ServiceException("500", "JSON_SERIALIZATION_ERROR", "No se pudo serializar posiciones netas");
        }

        CiclosCompensacion ciclo = new CiclosCompensacion();
        ciclo.setFechaCiclo(fechaCiclo);
        ciclo.setHoraCorte(LocalDateTime.now());
        ciclo.setTotalTransacciones(txs.size());
        ciclo.setPosicionesNetas(posicionesJson);
        ciclo.setEstado("Completado");
        ciclo.setArchivoLiquidacionUrl(null);

        CiclosCompensacion savedCiclo = repository.save(ciclo);
        log.info("Clearing completado exitosamente para fecha: {}. Total transacciones: {}", fechaCiclo, txs.size());

        return savedCiclo;
    }

    public CiclosCompensacion consultar(LocalDate fechaCiclo) {
        return repository.findByFechaCiclo(fechaCiclo)
                .orElseThrow(() -> new ServiceException("404", "CYCLE_NOT_FOUND",
                        "No existe ciclo para la fecha " + fechaCiclo));
    }

    public List<CiclosCompensacion> listarUltimosCiclos(int limit) {
        return repository.findTopByOrderByFechaCicloDesc(PageRequest.of(0, limit));
    }

    public CiclosCompensacion obtenerUltimoCiclo() {
        return repository.findFirstByOrderByFechaCicloDesc()
                .orElseThrow(() -> new ServiceException("404", "NO_CYCLES", "No hay ciclos de clearing registrados"));
    }

    public ClearingCycleResponse toResponse(CiclosCompensacion ciclo) {
        ClearingCycleResponse r = new ClearingCycleResponse();
        r.setFechaCiclo(ciclo.getFechaCiclo().toString());
        r.setHoraCorte(OffsetDateTime.now().toString());
        r.setTotalTransacciones(ciclo.getTotalTransacciones());
        r.setEstado(ciclo.getEstado());
        try {
            r.setPosicionesNetas(objectMapper.readValue(ciclo.getPosicionesNetas(), Object.class));
        } catch (Exception ex) {
            r.setPosicionesNetas(ciclo.getPosicionesNetas());
        }
        r.setArchivoLiquidacionUrl(ciclo.getArchivoLiquidacionUrl());
        return r;
    }

    private static class BankTotals {
        private BigDecimal enviado = BigDecimal.ZERO;
        private BigDecimal recibido = BigDecimal.ZERO;

        private BankTotals() {
        }
    }
}
