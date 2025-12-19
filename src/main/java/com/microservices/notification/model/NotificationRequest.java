package com.microservices.notification.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String endpoint;
    private Object payload;
    private Integer timeoutMs;
    private Integer maxReintentos;
}
