package com.microservices.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {
    private String endpoint;
    private Object payload;
    private Integer timeoutMs;
    private Integer maxReintentos;
}
