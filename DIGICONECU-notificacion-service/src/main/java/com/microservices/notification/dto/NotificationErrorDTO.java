package com.microservices.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationErrorDTO {
    private Boolean exitoso;
    private String error;
    private Integer intentosRealizados;
    private Long tiempoTotal;
}
