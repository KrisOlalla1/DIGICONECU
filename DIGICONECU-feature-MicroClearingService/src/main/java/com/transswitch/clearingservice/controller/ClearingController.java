package com.transswitch.clearingservice.controller;

import com.transswitch.clearingservice.dto.ApiResponse;
import com.transswitch.clearingservice.dto.ClearingExecuteRequest;
import com.transswitch.clearingservice.model.CiclosCompensacion;
import com.transswitch.clearingservice.service.ClearingService;
import com.transswitch.clearingservice.service.ISO20022GeneratorService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/clearing")
public class ClearingController {

    private final ClearingService clearingService;
    private final ISO20022GeneratorService iso20022Generator;

    public ClearingController(ClearingService clearingService, ISO20022GeneratorService iso20022Generator) {
        this.clearingService = clearingService;
        this.iso20022Generator = iso20022Generator;
    }

    @PostMapping("/ejecutar")
    public ResponseEntity<ApiResponse<Object>> ejecutar(@Valid @RequestBody ClearingExecuteRequest request) {
        LocalDate fecha = LocalDate.parse(request.getFechaCiclo());
        CiclosCompensacion ciclo = clearingService.ejecutar(fecha);
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(clearingService.toResponse(ciclo), ts));
    }

    @GetMapping("/ciclos/{fecha}")
    public ResponseEntity<ApiResponse<Object>> consultar(@PathVariable String fecha) {
        LocalDate f = LocalDate.parse(fecha);
        CiclosCompensacion ciclo = clearingService.consultar(f);
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(clearingService.toResponse(ciclo), ts));
    }

    @GetMapping("/ciclos")
    public ResponseEntity<ApiResponse<Object>> listarCiclos(
            @RequestParam(defaultValue = "10") int limit) {
        List<CiclosCompensacion> ciclos = clearingService.listarUltimosCiclos(limit);
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(
                ciclos.stream().map(clearingService::toResponse).toList(), ts));
    }

    @GetMapping("/ultimo")
    public ResponseEntity<ApiResponse<Object>> obtenerUltimo() {
        CiclosCompensacion ciclo = clearingService.obtenerUltimoCiclo();
        String ts = OffsetDateTime.now().toString();
        return ResponseEntity.ok(ApiResponse.ok(clearingService.toResponse(ciclo), ts));
    }

    // ============ ARCHIVOS ISO 20022 ============

    /**
     * Descarga el archivo ISO 20022 XML para un ciclo de compensación
     */
    @GetMapping("/archivos/{fecha}/xml")
    public ResponseEntity<byte[]> descargarArchivoXml(@PathVariable String fecha) {
        LocalDate f = LocalDate.parse(fecha);
        CiclosCompensacion ciclo = clearingService.consultar(f);

        if (ciclo.getArchivoXmlContenido() == null) {
            return ResponseEntity.notFound().build();
        }

        String nombreArchivo = iso20022Generator.generarNombreArchivo(f, "xml");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_XML)
                .body(ciclo.getArchivoXmlContenido().getBytes());
    }

    /**
     * Descarga el archivo plano CSV para un ciclo de compensación
     */
    @GetMapping("/archivos/{fecha}/csv")
    public ResponseEntity<byte[]> descargarArchivoCsv(@PathVariable String fecha) {
        LocalDate f = LocalDate.parse(fecha);
        CiclosCompensacion ciclo = clearingService.consultar(f);

        if (ciclo.getArchivoCsvContenido() == null) {
            return ResponseEntity.notFound().build();
        }

        String nombreArchivo = iso20022Generator.generarNombreArchivo(f, "csv");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(ciclo.getArchivoCsvContenido().getBytes());
    }

    /**
     * Vista previa del archivo ISO 20022 XML (sin descargar)
     */
    @GetMapping("/archivos/{fecha}/xml/preview")
    public ResponseEntity<String> previewArchivoXml(@PathVariable String fecha) {
        LocalDate f = LocalDate.parse(fecha);
        CiclosCompensacion ciclo = clearingService.consultar(f);

        if (ciclo.getArchivoXmlContenido() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(ciclo.getArchivoXmlContenido());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "clearing-service",
                "timestamp", OffsetDateTime.now().toString()));
    }
}
