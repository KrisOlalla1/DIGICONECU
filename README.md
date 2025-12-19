# Notification Service

## Descripción General
Este microservicio simula el envío de notificaciones HTTP a bancos destino (fire-and-forget). Es 100% stateless: no usa base de datos, no almacena nada, solo procesa y responde inmediatamente.

## Tecnologías
- Java 21
- Spring Boot 3.5.9
- Maven
- Lombok
- MapStruct

## Arquitectura
- **Modelo de dominio:** POJO `NotificationRequest` (sin anotaciones de persistencia)
- **DTOs:** `NotificationRequestDTO` y `NotificationResponseDTO` para entrada/salida
- **Mapper:** `NotificationMapper` (MapStruct) para convertir entre modelo y DTO
- **Servicio:** `NotificationService` simula el envío y escribe logs
- **Controlador:** `NotificationController` expone el endpoint REST

## Endpoint Principal
```
POST /api/v2/notification/enviar
```
- **Request:** JSON con `urlDestino`, `payload`, `bancoDestino`
- **Response:** JSON con status OK y mensaje

## Ejemplo de Request
```json
{
  "urlDestino": "https://banco-destino.com/webhook",
  "payload": "{\"transaccion\": \"TX123\", \"monto\": 1000}",
  "bancoDestino": "BANCO_DESTINO"
}
```

## Ejemplo de Response
```json
{
  "status": "OK",
  "mensaje": "Notificacion enviada correctamente"
}
```

## Explicación del Código
- **NotificationRequest:** POJO con los datos de la notificación. Usa Lombok para reducir código repetitivo.
- **NotificationRequestDTO/NotificationResponseDTO:** Separan el contrato de la API del modelo interno.
- **NotificationMapper:** MapStruct genera el código de conversión entre modelo y DTO automáticamente.
- **NotificationService:** Simula el envío de la notificación escribiendo logs (no realiza ninguna llamada HTTP real ni guarda nada).
- **NotificationController:** Expone el endpoint POST, usa inyección por constructor y responde siempre 200 OK.

## Stateless
- No hay base de datos ni archivos externos.
- El servicio no guarda nada, solo procesa y responde.
- Puede escalar horizontalmente sin problemas de sincronización.

## Ejecución
```
cd notificationservice
mvn spring-boot:run
```

## Mejoras Futuras
- Implementar envío HTTP real (WebClient).
- Validación de entrada con anotaciones Bean Validation.
- Documentación OpenAPI/Swagger.
- Tests unitarios y de integración.
