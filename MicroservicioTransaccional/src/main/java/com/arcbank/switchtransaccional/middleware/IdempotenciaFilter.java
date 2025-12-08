package com.arcbank.switchtransaccional.middleware;

import com.arcbank.switchtransaccional.service.IIdempotenciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Middleware de idempotencia para evitar procesamiento duplicado de transacciones.
 * PERSONA 2: Implementa la lógica de verificación usando IIdempotenciaService.
 * 
 * Este filtro intercepta todas las peticiones POST a /api/transacciones
 * y verifica si ya existe una respuesta en Redis antes de procesar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdempotenciaFilter implements Filter {

    private final IIdempotenciaService idempotenciaService;
    private final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Solo aplicar a POST /api/transacciones
        if ("POST".equalsIgnoreCase(httpRequest.getMethod()) && 
            httpRequest.getRequestURI().contains("/api/transacciones")) {
            
            log.debug("Verificando idempotencia para petición POST a {}", httpRequest.getRequestURI());
            
            // TODO PERSONA 2: Extraer EndToEnd del body
            // TODO PERSONA 2: Llamar a idempotenciaService.obtenerRespuestaPrevia()
            // TODO PERSONA 2: Si existe, retornar la respuesta cached
            // TODO PERSONA 2: Si no existe, continuar con chain.doFilter()
            
            // Por ahora, solo continúa el flujo normal
            chain.doFilter(request, response);
            
        } else {
            // Para otras peticiones, continuar normalmente
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("Inicializando IdempotenciaFilter");
    }

    @Override
    public void destroy() {
        log.info("Destruyendo IdempotenciaFilter");
    }
}
