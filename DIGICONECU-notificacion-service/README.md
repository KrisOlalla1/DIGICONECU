# Notification Service

## Descripción General
Microservicio 100% stateless que envía notificaciones HTTP a bancos destino con política de reintentos automáticos. Implementa un patrón de reintentos con delays exponenciales y manejo inteligente de códigos HTTP.

## Características
- ✅ Cliente HTTP real (WebClient)
- ✅ Política de reintentos: 4 intentos con delays (0ms, 800ms, 2000ms, 4000ms)
- ✅ Timeout configurable por request
- ✅ Validación de URL
- ✅ Manejo diferenciado de códigos HTTP (2xx, 4xx, 5xx)
- ✅ Logging detallado de cada intento
- ✅ Métricas de tiempo y reintentos

## Tecnologías
- Java 21
- Spring Boot 3.5.9
- WebFlux (WebClient)
- Maven
- Lombok
- MapStruct

## Puerto
**8086**

## Endpoint Principal
```
POST /api/v2/notification/enviar
```

## Estructura de Request
```json
{
  "endpoint": "https://api.bancob.com/incoming/credit",
  "payload": {
    "instructionId": "a5f2c8e1-4b3d-9876-abcd-1234567890ab",
    "amount": 1500.00,
    "currency": "USD",
    "debtorAccount": "1000001234",
    "creditorAccount": "2500005678",
    "debtorName": "Empresa S.A.",
    "creditorName": "Juan Pérez",
    "remittanceInfo": "Pago de factura"
  },
  "timeoutMs": 5000,
  "maxReintentos": 4
}
```

## Estructura de Response

### Éxito (HTTP 200)
```json
{
  "success": true,
  "data": {
    "exitoso": true,
    "codigoHttp": 200,
    "respuesta": { "status": "accepted" },
    "intentosRealizados": 1,
    "tiempoTotal": 234
  }
}
```

### Error por Timeout (HTTP 200)
```json
{
  "success": false,
  "error": {
    "exitoso": false,
    "error": "TIMEOUT",
    "intentosRealizados": 4,
    "tiempoTotal": 7500
  }
}
```

### Error 4xx - No reintentar (HTTP 200)
```json
{
  "success": false,
  "error": {
    "exitoso": false,
    "error": "HTTP_404",
    "intentosRealizados": 1,
    "tiempoTotal": 150
  }
}
```

## Política de Reintentos

| Intento | Delay Antes | Acumulado |
|---------|-------------|-----------|
| 1 | 0ms | 0ms |
| 2 | 800ms | 800ms |
| 3 | 2000ms | 2800ms |
| 4 | 4000ms | 6800ms |

### Reglas de Reintento
- ✅ **HTTP 5xx**: Reintentar (error de servidor)
- ✅ **Timeout de red**: Reintentar
- ✅ **Error de conexión**: Reintentar
- ❌ **HTTP 4xx**: NO reintentar (error de cliente)

## Health Check
```
GET /health
```
Response: `"OK"`

## Variables de Entorno
```properties
PORT=8086
LOG_LEVEL=INFO
DEFAULT_TIMEOUT_MS=5000
MAX_RETRIES=4
```

## Configuración
Ver `application.properties`:
```properties
notification.default-timeout-ms=5000
notification.max-retries=4
```

## Ejecución

### Opción 1: Maven (Desarrollo)
```bash
cd notificationservice
mvn spring-boot:run
```

### Opción 2: Docker
```bash
# Construir imagen
docker build -t notification-service:latest .

# Ejecutar contenedor
docker run -d \
  -p 8086:8086 \
  --name notification-service \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  notification-service:latest

# Ver logs
docker logs -f notification-service

# Detener
docker stop notification-service
docker rm notification-service
```

### Opción 3: Docker Compose (Recomendado para desarrollo)
Desde la raíz del proyecto:
```bash
docker-compose up -d notification-service
docker-compose logs -f notification-service
docker-compose down
```

## Ejemplos de Uso

### Envío exitoso
```bash
curl -X POST http://localhost:8086/api/v2/notification/enviar \
  -H "Content-Type: application/json" \
  -d '{
    "endpoint": "https://httpbin.org/post",
    "payload": {"test": "data"},
    "timeoutMs": 3000,
    "maxReintentos": 2
  }'
```

### URL inválida
```bash
curl -X POST http://localhost:8086/api/v2/notification/enviar \
  -H "Content-Type: application/json" \
  -d '{
    "endpoint": "invalid-url",
    "payload": {}
  }'
```

Response:
```json
{
  "success": false,
  "error": {
    "exitoso": false,
    "error": "INVALID_URL",
    "intentosRealizados": 0,
    "tiempoTotal": 0
  }
}
```

## Logging
Cada intento genera logs detallados:
```
INFO - Intento #1 - Enviando notificación a: https://api.banco.com
INFO - Notificación exitosa en intento #1, tiempo total: 234ms
```

O en caso de reintentos:
```
INFO - Intento #1 - Enviando notificación a: https://api.banco.com
WARN - Intento #1 falló con código HTTP: 503
INFO - Reintento #2 después de 800ms para endpoint: https://api.banco.com
INFO - Intento #2 - Enviando notificación a: https://api.banco.com
INFO - Notificación exitosa en intento #2, tiempo total: 1045ms
```

## Arquitectura Interna

### NotificationService
- Inyecta `WebClient.Builder` y configuración
- Implementa lógica de reintentos con delays
- Maneja timeouts y códigos HTTP
- Calcula métricas de tiempo

### NotificationController
- Valida URL con regex
- Retorna siempre HTTP 200 (el error va en el body)
- Expone health check

### DTOs
- `NotificationRequestDTO`: Entrada con endpoint, payload, timeoutMs, maxReintentos
- `NotificationResponseDTO`: Salida con success, data o error
- `NotificationDataDTO`: Datos de éxito con codigoHttp, respuesta, intentos, tiempo
- `NotificationErrorDTO`: Datos de error con código, intentos, tiempo

## Mejoras Futuras
- Circuit breaker (Resilience4j)
- Dead letter queue para errores persistentes
- Métricas Prometheus
- Distributed tracing

