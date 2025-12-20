package com.transswitch.returnmanagementservice.service;

import com.transswitch.returnmanagementservice.config.ReturnRulesProperties;
import com.transswitch.returnmanagementservice.dto.ReturnCreateRequest;
import com.transswitch.returnmanagementservice.dto.ReturnResponse;
import com.transswitch.returnmanagementservice.dto.payment.TransferGetResponse;
import com.transswitch.returnmanagementservice.integration.PaymentProcessingClient;
import com.transswitch.returnmanagementservice.model.Devoluciones;
import com.transswitch.returnmanagementservice.repository.DevolucionesRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ReturnManagementService {

    private final DevolucionesRepository repository;
    private final PaymentProcessingClient paymentClient;
    private final ReturnRulesProperties rules;

    public ReturnManagementService(DevolucionesRepository repository,
                                   PaymentProcessingClient paymentClient,
                                   ReturnRulesProperties rules) {
        this.repository = repository;
        this.paymentClient = paymentClient;
        this.rules = rules;
    }

    public Devoluciones crear(ReturnCreateRequest req) {
        UUID returnId = parseUuid(req.getReturnInstructionId(), "RETURN_ID_INVALID");
        UUID originalId = parseUuid(req.getOriginalInstructionId(), "ORIGINAL_ID_INVALID");

        if (repository.existsByTransaccionOriginalInstructionId(originalId)) {
            throw new ServiceException("409", "RETURN_ALREADY_EXISTS", "Ya existe devolución para la transacción " + originalId);
        }

        TransferGetResponse txResp = paymentClient.getTransfer(originalId);
        if (txResp == null || !txResp.isSuccess() || txResp.getData() == null) {
            throw new ServiceException("404", "ORIGINAL_TRANSACTION_NOT_FOUND", "No existe transacción original " + originalId);
        }

        TransferGetResponse.TransferData tx = txResp.getData();
        if (tx.getEstado() == null || !tx.getEstado().equalsIgnoreCase("Completada")) {
            throw new ServiceException("422", "INVALID_STATE", "Solo se pueden devolver transacciones en estado Completada");
        }

        if (rules.isEnforceAmountMatch()) {
            BigDecimal montoOriginal = tx.getMonto();
            if (montoOriginal == null || req.getMonto() == null || montoOriginal.compareTo(req.getMonto()) != 0) {
                throw new ServiceException("422", "AMOUNT_MISMATCH", "El monto de la devolución no coincide con la transacción original");
            }
        }

        if (tx.getFechaCreacion() != null) {
            OffsetDateTime created = OffsetDateTime.parse(tx.getFechaCreacion());
            Duration diff = Duration.between(created, OffsetDateTime.now());
            if (diff.toHours() > rules.getMaxHours()) {
                throw new ServiceException("422", "TIME_WINDOW_EXCEEDED", "La devolución excede la ventana de " + rules.getMaxHours() + " horas");
            }
        }

        Devoluciones d = new Devoluciones();
        d.setReturnInstructionId(returnId);
        d.setTransaccionOriginalInstructionId(originalId);
        d.setBancoIniciadorCodigo(req.getBancoIniciador());
        d.setMotivo(req.getMotivo());
        d.setMonto(req.getMonto());
        d.setEstado("Completada");
        d.setFechaCreacion(LocalDateTime.now());

        return repository.save(d);
    }

    public Devoluciones consultar(UUID returnInstructionId) {
        return repository.findByReturnInstructionId(returnInstructionId)
                .orElseThrow(() -> new ServiceException("404", "RETURN_NOT_FOUND", "No existe devolución " + returnInstructionId));
    }

    public ReturnResponse toResponse(Devoluciones d) {
        ReturnResponse r = new ReturnResponse();
        r.setReturnInstructionId(d.getReturnInstructionId().toString());
        r.setOriginalInstructionId(d.getTransaccionOriginalInstructionId().toString());
        r.setEstado(d.getEstado());
        r.setTimestamp(OffsetDateTime.now().toString());
        return r;
    }

    private UUID parseUuid(String s, String code) {
        try {
            return UUID.fromString(s);
        } catch (Exception ex) {
            throw new ServiceException("400", code, "UUID inválido: " + s);
        }
    }
}
