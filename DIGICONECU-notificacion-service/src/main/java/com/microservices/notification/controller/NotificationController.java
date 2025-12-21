package com.microservices.notification.controller;

import com.microservices.notification.dto.NotificationRequestDTO;
import com.microservices.notification.dto.NotificationResponseDTO;
import com.microservices.notification.mapper.NotificationMapper;
import com.microservices.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/v2/notification")
public class NotificationController {

    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)([\\w.-]+)(:[0-9]+)?(/.*)?$",
            Pattern.CASE_INSENSITIVE
    );

    private final NotificationService notificationService;
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationService notificationService, NotificationMapper notificationMapper) {
        this.notificationService = notificationService;
        this.notificationMapper = notificationMapper;
    }

    @PostMapping("/enviar")
    public ResponseEntity<NotificationResponseDTO> enviar(@RequestBody NotificationRequestDTO request) {
        if (request.getEndpoint() == null || !URL_PATTERN.matcher(request.getEndpoint()).matches()) {
            return ResponseEntity.badRequest().body(NotificationResponseDTO.builder()
                    .success(false)
                    .error(com.microservices.notification.dto.NotificationErrorDTO.builder()
                            .exitoso(false)
                            .error("INVALID_URL")
                            .intentosRealizados(0)
                            .tiempoTotal(0L)
                            .build())
                    .build());
        }

        NotificationResponseDTO response = notificationService.enviar(notificationMapper.toModel(request));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
