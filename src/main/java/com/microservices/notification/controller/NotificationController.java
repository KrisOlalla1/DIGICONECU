package com.microservices.notification.controller;

import com.microservices.notification.dto.NotificationRequestDTO;
import com.microservices.notification.dto.NotificationResponseDTO;
import com.microservices.notification.mapper.NotificationMapper;
import com.microservices.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @PostMapping("/enviar")
    public ResponseEntity<NotificationResponseDTO> enviar(@RequestBody NotificationRequestDTO request) {
        notificationService.enviar(notificationMapper.toModel(request));
        return ResponseEntity.ok(NotificationResponseDTO.builder()
                .status("OK")
                .mensaje("Notificacion enviada correctamente")
                .build());
    }
}
