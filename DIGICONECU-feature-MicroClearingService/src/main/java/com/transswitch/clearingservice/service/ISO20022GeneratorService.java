package com.transswitch.clearingservice.service;

import com.transswitch.clearingservice.model.CiclosCompensacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

/**
 * Servicio para generación de archivos ISO 20022 XML
 * Genera archivos de liquidación para el Banco Central
 * 
 * Formato: pain.002.001.03 (Payment Status Report)
 */
@Service
public class ISO20022GeneratorService {

    private static final Logger log = LoggerFactory.getLogger(ISO20022GeneratorService.class);

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter ISO_DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Value("${clearing.institution-id:SWITCHECU}")
    private String institutionId;

    @Value("${clearing.country-code:EC}")
    private String countryCode;

    @Value("${clearing.currency:USD}")
    private String currency;

    /**
     * Genera archivo ISO 20022 pain.002.001.03 (Payment Status Report)
     * para liquidación con Banco Central
     */
    public String generarArchivoLiquidacion(CiclosCompensacion ciclo,
            Map<String, Map<String, Object>> posicionesNetas) {
        log.info("Generando archivo ISO 20022 para ciclo: {}", ciclo.getFechaCiclo());

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        String messageId = "MSG-" + ciclo.getFechaCiclo().toString().replace("-", "") + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        StringWriter writer = new StringWriter();

        // XML Header
        writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pacs.002.001.03\">\n");
        writer.append("  <FIToFIPmtStsRpt>\n");

        // Group Header
        writer.append("    <GrpHdr>\n");
        writer.append("      <MsgId>").append(messageId).append("</MsgId>\n");
        writer.append("      <CreDtTm>").append(ahora.format(ISO_DATETIME)).append("</CreDtTm>\n");
        writer.append("      <InstgAgt>\n");
        writer.append("        <FinInstnId>\n");
        writer.append("          <BIC>").append(institutionId).append("</BIC>\n");
        writer.append("        </FinInstnId>\n");
        writer.append("      </InstgAgt>\n");
        writer.append("    </GrpHdr>\n");

        // Original Group Information
        writer.append("    <OrgnlGrpInfAndSts>\n");
        writer.append("      <OrgnlMsgId>CLR-").append(ciclo.getFechaCiclo().toString()).append("</OrgnlMsgId>\n");
        writer.append("      <OrgnlMsgNmId>pacs.008.001.02</OrgnlMsgNmId>\n");
        writer.append("      <OrgnlCreDtTm>").append(ciclo.getHoraCorte().format(ISO_DATETIME))
                .append("</OrgnlCreDtTm>\n");
        writer.append("      <OrgnlNbOfTxs>").append(String.valueOf(ciclo.getTotalTransacciones()))
                .append("</OrgnlNbOfTxs>\n");
        writer.append("      <GrpSts>ACCP</GrpSts>\n"); // ACCP = Accepted
        writer.append("    </OrgnlGrpInfAndSts>\n");

        // Posiciones Netas por Banco
        int txCount = 0;
        for (Map.Entry<String, Map<String, Object>> entry : posicionesNetas.entrySet()) {
            String banco = entry.getKey();
            if ("Totales".equals(banco))
                continue; // Skip totals row

            Map<String, Object> pos = entry.getValue();
            BigDecimal neto = toBigDecimal(pos.get("Neto"));
            BigDecimal enviado = toBigDecimal(pos.get("Enviado"));
            BigDecimal recibido = toBigDecimal(pos.get("Recibido"));

            txCount++;
            writer.append("    <TxInfAndSts>\n");
            writer.append("      <StsId>TXN-").append(String.format("%03d", txCount)).append("</StsId>\n");
            writer.append("      <OrgnlEndToEndId>E2E-").append(banco).append("-")
                    .append(ciclo.getFechaCiclo().toString()).append("</OrgnlEndToEndId>\n");
            writer.append("      <OrgnlTxId>CLR-").append(banco).append("-").append(ciclo.getFechaCiclo().toString())
                    .append("</OrgnlTxId>\n");
            writer.append("      <TxSts>ACCP</TxSts>\n");

            // Clearing System Member Identification
            writer.append("      <InstgAgt>\n");
            writer.append("        <FinInstnId>\n");
            writer.append("          <ClrSysMmbId>\n");
            writer.append("            <MmbId>").append(banco).append("</MmbId>\n");
            writer.append("          </ClrSysMmbId>\n");
            writer.append("        </FinInstnId>\n");
            writer.append("      </InstgAgt>\n");

            // Original Transaction Reference
            writer.append("      <OrgnlTxRef>\n");
            writer.append("        <Amt>\n");

            // Instrucción de débito/crédito basada en posición neta
            if (neto.compareTo(BigDecimal.ZERO) >= 0) {
                writer.append("          <InstdAmt Ccy=\"").append(currency).append("\">")
                        .append(neto.abs().toPlainString()).append("</InstdAmt>\n");
            } else {
                writer.append("          <InstdAmt Ccy=\"").append(currency).append("\">")
                        .append(neto.abs().toPlainString()).append("</InstdAmt>\n");
            }

            writer.append("        </Amt>\n");

            // Settlement Information
            writer.append("        <SttlmInf>\n");
            writer.append("          <SttlmMtd>CLRG</SttlmMtd>\n");
            writer.append("          <ClrSys>\n");
            writer.append("            <Cd>").append(countryCode).append("ACH</Cd>\n");
            writer.append("          </ClrSys>\n");
            writer.append("        </SttlmInf>\n");

            // Supplementary Data - Detalle de movimientos
            writer.append("        <SplmtryData>\n");
            writer.append("          <Envlp>\n");
            writer.append("            <BancoCodigo>").append(banco).append("</BancoCodigo>\n");
            writer.append("            <MontoEnviado>").append(enviado.toPlainString()).append("</MontoEnviado>\n");
            writer.append("            <MontoRecibido>").append(recibido.toPlainString()).append("</MontoRecibido>\n");
            writer.append("            <PosicionNeta>").append(neto.toPlainString()).append("</PosicionNeta>\n");
            writer.append("            <DireccionNeta>")
                    .append(neto.compareTo(BigDecimal.ZERO) >= 0 ? "CREDITOR" : "DEBTOR").append("</DireccionNeta>\n");
            writer.append("          </Envlp>\n");
            writer.append("        </SplmtryData>\n");

            writer.append("      </OrgnlTxRef>\n");
            writer.append("    </TxInfAndSts>\n");
        }

        writer.append("  </FIToFIPmtStsRpt>\n");
        writer.append("</Document>\n");

        String xml = writer.toString();
        log.info("Archivo ISO 20022 generado exitosamente. Tamaño: {} bytes", xml.length());

        return xml;
    }

    /**
     * Genera archivo plano para liquidación con Banco Central
     * Formato: CSV con posiciones netas
     */
    public String generarArchivoPlano(CiclosCompensacion ciclo, Map<String, Map<String, Object>> posicionesNetas) {
        log.info("Generando archivo plano para ciclo: {}", ciclo.getFechaCiclo());

        OffsetDateTime ahora = OffsetDateTime.now(ZoneOffset.UTC);
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("# ARCHIVO DE LIQUIDACION - SWITCH TRANSACCIONAL ECUADOR\n");
        sb.append("# Fecha Ciclo: ").append(ciclo.getFechaCiclo()).append("\n");
        sb.append("# Hora Corte: ").append(ciclo.getHoraCorte()).append("\n");
        sb.append("# Generado: ").append(ahora.format(ISO_DATETIME)).append(" UTC\n");
        sb.append("# Total Transacciones: ").append(ciclo.getTotalTransacciones()).append("\n");
        sb.append("#\n");
        sb.append("BANCO_CODIGO,MONTO_ENVIADO,MONTO_RECIBIDO,POSICION_NETA,DIRECCION\n");

        BigDecimal totalEnviado = BigDecimal.ZERO;
        BigDecimal totalRecibido = BigDecimal.ZERO;
        BigDecimal sumaNetos = BigDecimal.ZERO;

        for (Map.Entry<String, Map<String, Object>> entry : posicionesNetas.entrySet()) {
            String banco = entry.getKey();
            if ("Totales".equals(banco))
                continue;

            Map<String, Object> pos = entry.getValue();
            BigDecimal enviado = toBigDecimal(pos.get("Enviado"));
            BigDecimal recibido = toBigDecimal(pos.get("Recibido"));
            BigDecimal neto = toBigDecimal(pos.get("Neto"));

            totalEnviado = totalEnviado.add(enviado);
            totalRecibido = totalRecibido.add(recibido);
            sumaNetos = sumaNetos.add(neto);

            String direccion = neto.compareTo(BigDecimal.ZERO) >= 0 ? "ACREEDOR" : "DEUDOR";

            sb.append(banco).append(",");
            sb.append(enviado.toPlainString()).append(",");
            sb.append(recibido.toPlainString()).append(",");
            sb.append(neto.toPlainString()).append(",");
            sb.append(direccion).append("\n");
        }

        // Footer con totales
        sb.append("#\n");
        sb.append("# TOTALES\n");
        sb.append("# Total Enviado: ").append(totalEnviado.toPlainString()).append("\n");
        sb.append("# Total Recibido: ").append(totalRecibido.toPlainString()).append("\n");
        sb.append("# Suma Netos (debe ser 0): ").append(sumaNetos.toPlainString()).append("\n");
        sb.append("# FIN ARCHIVO\n");

        String contenido = sb.toString();
        log.info("Archivo plano generado exitosamente. Líneas: {}", contenido.split("\n").length);

        return contenido;
    }

    /**
     * Genera nombre de archivo según convención ISO 20022
     */
    public String generarNombreArchivo(LocalDate fechaCiclo, String formato) {
        String fecha = fechaCiclo.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String timestamp = OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("HHmmss"));

        if ("xml".equalsIgnoreCase(formato)) {
            return String.format("pacs.002.%s.%s.%s.xml", institutionId, fecha, timestamp);
        } else {
            return String.format("CLR_%s_%s_%s.csv", institutionId, fecha, timestamp);
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null)
            return BigDecimal.ZERO;
        if (value instanceof BigDecimal)
            return (BigDecimal) value;
        if (value instanceof Number)
            return BigDecimal.valueOf(((Number) value).doubleValue());
        try {
            return new BigDecimal(value.toString());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
