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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ReturnManagementService {

    private static final Logger log = LoggerFactory.getLogger(ReturnManagementService.class);

    // Códigos de motivo ISO válidos
    private static final Set<String> VALID_REASON_CODES = Set.of(
            "MS03", "FR01", "AC04", "AM05", "AC01", "AC06", "AG01");

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
        log.info("Procesando solicitud de devolución: returnId={}, originalId={}",
                req.getReturnInstructionId(), req.getOriginalInstructionId());

        UUID returnId = parseUuid(req.getReturnInstructionId(), "RETURN_ID_INVALID");
        UUID originalId = parseUuid(req.getOriginalInstructionId(), "ORIGINAL_ID_INVALID");

        // Validar código de motivo ISO
        if (req.getMotivo() == null || !VALID_REASON_CODES.contains(req.getMotivo())) {
            throw new ServiceException("400", "INVALID_REASON_CODE",
                    "Código de motivo inválido. Valores permitidos: " + VALID_REASON_CODES);
        }

        if (repository.existsByTransaccionOriginalInstructionId(originalId)) {
            log.warn("Ya existe devolución para transacción: {}", originalId);
            throw new ServiceException("409", "RETURN_ALREADY_EXISTS",
                    "Ya existe devolución para la transacción " + originalId);
        }

        TransferGetResponse txResp = paymentClient.getTransfer(originalId);
        if (txResp == null || !txResp.isSuccess() || txResp.getData() == null) {
            log.warn("Transacción original no encontrada: {}", originalId);
            throw new ServiceException("404", "ORIGINAL_TRANSACTION_NOT_FOUND",
                    "No existe transacción original " + originalId);
        }

        TransferGetResponse.TransferData tx = txResp.getData();
        if (tx.getEstado() == null || !tx.getEstado().equalsIgnoreCase("Completada")) {
            log.warn("Transacción no está en estado Completada: {}", tx.getEstado());
            throw new ServiceException("422", "INVALID_STATE",
                    "Solo se pueden devolver transacciones en estado Completada");
        }

        if (rules.isEnforceAmountMatch()) {
            BigDecimal montoOriginal = tx.getMonto();
            if (montoOriginal == null || req.getMonto() == null || montoOriginal.compareTo(req.getMonto()) != 0) {
                throw new ServiceException("422", "AMOUNT_MISMATCH",
                        "El monto de la devolución no coincide con la transacción original");
            }
        }

        if (tx.getFechaCreacion() != null) {
            OffsetDateTime created = OffsetDateTime.parse(tx.getFechaCreacion());
            Duration diff = Duration.between(created, OffsetDateTime.now());
            if (diff.toHours() > rules.getMaxHours()) {
                log.warn("Devolución excede ventana de tiempo: {} horas vs máximo {} horas", diff.toHours(),
                        rules.getMaxHours());
                throw new ServiceException("422", "TIME_WINDOW_EXCEEDED",
                        "La devolución excede la ventana de " + rules.getMaxHours() + " horas");
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

        Devoluciones saved = repository.save(d);
        log.info("Devolución creada exitosamente: {}", saved.getReturnInstructionId());

        // TODO: Llamar a Payment Processing para crear transacción inversa
        // y actualizar estado de transacción original a REVERTIDA

        return saved;
    }

    public Devoluciones consultar(UUID returnInstructionId) {
        return repository.findByReturnInstructionId(returnInstructionId)
                .orElseThrow(() -> new ServiceException("404", "RETURN_NOT_FOUND",
                        "No existe devolución " + returnInstructionId));
    }

    public List<Devoluciones> buscarPorTransaccionOriginal(UUID originalInstructionId) {
        return repository.findByTransaccionOriginalInstructionId(originalInstructionId);
    }

    public List<Devoluciones> listarTodas() {
        return repository.findAll();
    }

    public ReturnResponse toResponse(Devoluciones d) {
        ReturnResponse r = new ReturnResponse();
        r.setReturnInstructionId(d.getReturnInstructionId().toString());
        r.setOriginalInstructionId(d.getTransaccionOriginalInstructionId().toString());
        r.setEstado(d.getEstado());
        r.setMotivo(d.getMotivo());
        r.setMonto(d.getMonto());
        r.setBancoIniciador(d.getBancoIniciadorCodigo());
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
