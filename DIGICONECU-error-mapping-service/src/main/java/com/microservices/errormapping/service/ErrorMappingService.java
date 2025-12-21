package com.microservices.errormapping.service;

import com.microservices.errormapping.dto.CodigoISODTO;
import com.microservices.errormapping.dto.ErrorDataDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ErrorMappingService {

    private static final Logger log = LoggerFactory.getLogger(ErrorMappingService.class);
    private static final String UNKNOWN_CODE = "UNKNOWN";

    private final Map<String, Map<String, String>> mapeosBancos = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> codigosISO = new ConcurrentHashMap<>();

    @Value("${error-mapping.config-file:classpath:error-mappings.yml}")
    private Resource configFile;

    @PostConstruct
    public void init() {
        cargarConfiguracion();
    }

    public void cargarConfiguracion() {
        try (InputStream inputStream = configFile.getInputStream()) {
            Yaml yaml = new Yaml();
            Map<String, Object> config = yaml.load(inputStream);

            if (config.containsKey("mapeos")) {
                Map<String, Map<String, String>> mapeos = (Map<String, Map<String, String>>) config.get("mapeos");
                mapeosBancos.clear();
                mapeosBancos.putAll(mapeos);
                log.info("Cargados {} mapeos de bancos", mapeosBancos.size());
            }

            if (config.containsKey("codigos_iso")) {
                Map<String, Map<String, String>> codigos = (Map<String, Map<String, String>>) config.get("codigos_iso");
                codigosISO.clear();
                codigosISO.putAll(codigos);
                log.info("Cargados {} códigos ISO", codigosISO.size());
            }

        } catch (Exception e) {
            log.error("Error al cargar configuración de mapeo de errores", e);
        }
    }

    public ErrorDataDTO traducirError(String bancoCodigo, String codigoOriginal) {
        log.info("Traduciendo error - Banco: {}, Código: {}", bancoCodigo, codigoOriginal);

        String codigoISO = obtenerCodigoISO(bancoCodigo, codigoOriginal);
        Map<String, String> isoInfo = codigosISO.getOrDefault(codigoISO, codigosISO.get(UNKNOWN_CODE));

        return ErrorDataDTO.builder()
                .codigoISO(codigoISO)
                .descripcion(isoInfo.get("descripcion"))
                .mensajeEstandar(isoInfo.get("mensaje"))
                .build();
    }

    private String obtenerCodigoISO(String bancoCodigo, String codigoOriginal) {
        Map<String, String> mapeoBanco = mapeosBancos.get(bancoCodigo);
        
        if (mapeoBanco != null && mapeoBanco.containsKey(codigoOriginal)) {
            return mapeoBanco.get(codigoOriginal);
        }

        log.warn("No se encontró mapeo para banco: {}, código: {}", bancoCodigo, codigoOriginal);
        return UNKNOWN_CODE;
    }

    public void agregarMapeo(String bancoCodigo, Map<String, String> nuevoMapeo) {
        Map<String, String> mapeoExistente = mapeosBancos.computeIfAbsent(bancoCodigo, k -> new HashMap<>());
        mapeoExistente.putAll(nuevoMapeo);
        log.info("Agregados {} mapeos para el banco {}", nuevoMapeo.size(), bancoCodigo);
    }

    public List<CodigoISODTO> listarCodigosISO() {
        return codigosISO.entrySet().stream()
                .map(entry -> CodigoISODTO.builder()
                        .codigo(entry.getKey())
                        .descripcion(entry.getValue().get("descripcion"))
                        .mensaje(entry.getValue().get("mensaje"))
                        .build())
                .collect(Collectors.toList());
    }
}
