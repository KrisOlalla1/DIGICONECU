package com.microservices.notification.service;

import com.microservices.notification.model.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public void enviar(NotificationRequest request) {
        log.info("=== SIMULACION DE ENVIO HTTP ===");
        log.info("URL Destino: {}", request.getUrlDestino());
        log.info("Banco Destino: {}", request.getBancoDestino());
        log.info("Payload: {}", request.getPayload());
        log.info("=== FIN SIMULACION ===");
    }
}
