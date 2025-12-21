package com.microservices.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDataDTO {
    private Boolean exitoso;
    private Integer codigoHttp;
    private Object respuesta;
    private Integer intentosRealizados;
    private Long tiempoTotal;
}
