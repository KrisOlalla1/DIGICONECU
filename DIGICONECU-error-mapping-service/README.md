# Error Mapping Service

## Descripción General
Microservicio 100% stateless que traduce códigos de error propietarios de bancos a códigos estándar ISO 20022. Carga configuración desde archivo YAML y mantiene mapeos en memoria con capacidad de actualización dinámica.

## Características
- ✅ Mapeo a códigos ISO 20022 (AM04, AC01, AC04, AC06, AG01, MS03, DUPL)
- ✅ Carga de configuración desde archivo YAML
- ✅ Cache en memoria con ConcurrentHashMap
- ✅ Fallback automático a código "UNKNOWN"
- ✅ Endpoints de administración (agregar mapeos, reload)
- ✅ Endpoint para listar todos los códigos ISO
- ✅ Logging de traducciones

## Tecnologías
- Java 21
- Spring Boot 3.5.9
- SnakeYAML
- Maven
- Lombok

## Puerto
**8087**

## Endpoints

### 1. Traducir Error
```
POST /api/v2/error-mapping/traducir
```

**Request:**
```json
{
  "bancoCodigo": "PICHINCHA",
  "codigoOriginal": "ERROR_99",
  "mensajeOriginal": "No hay plata en la cuenta"
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "codigoISO": "AM04",
    "descripcion": "Insufficient Funds",
    "mensajeEstandar": "El banco no tiene fondos suficientes para completar la transacción"
  }
}
```

### 2. Agregar Mapeos (Admin)
```
POST /api/v2/error-mapping/agregar
```

**Request:**
```json
{
  "bancoCodigo": "PRODUBANCO",
  "mapeos": {
    "ERR_100": "AM04",
    "ERR_200": "AC01",
    "ERR_TIMEOUT": "MS03"
  }
}
```

**Response:**
```json
{
  "status": "OK",
  "mensaje": "Mapeos agregados correctamente"
}
```

### 3. Listar Códigos ISO
```
GET /api/v2/error-mapping/codigos-iso
```

**Response:**
```json
[
  {
    "codigo": "AM04",
    "descripcion": "Insufficient Funds",
    "mensaje": "El banco no tiene fondos suficientes para completar la transacción"
  },
  {
    "codigo": "AC01",
    "descripcion": "Incorrect Account Number",
    "mensaje": "El número de cuenta destino es incorrecto o no existe"
  },
  ...
]
```

### 4. Recargar Configuración
```
POST /api/v2/error-mapping/reload
```

**Response:**
```json
{
  "status": "OK",
  "mensaje": "Configuración recargada"
}
```

### 5. Health Check
```
GET /health
```

**Response:** `"OK"`

## Configuración YAML

Archivo: `src/main/resources/error-mappings.yml`

```yaml
mapeos:
  PICHINCHA:
    ERROR_99: AM04
    CUENTA_NO_EXISTE: AC01
    CUENTA_CERRADA: AC04
    TIMEOUT_CORE: MS03
    CUENTA_BLOQUEADA: AC06
    
  GUAYAQUIL:
    ERR_SALDO: AM04
    ERR_CTA_BLOQ: AC04
    ERR_SISTEMA: MS03
    ERR_CUENTA_INVALIDA: AC01

codigos_iso:
  AM04:
    descripcion: "Insufficient Funds"
    mensaje: "El banco no tiene fondos suficientes para completar la transacción"
  AC01:
    descripcion: "Incorrect Account Number"
    mensaje: "El número de cuenta destino es incorrecto o no existe"
  ...
```

## Códigos ISO 20022 Soportados

| Código | Descripción | Uso |
|--------|-------------|-----|
| AM04 | Insufficient Funds | Fondos insuficientes |
| AC01 | Incorrect Account Number | Cuenta incorrecta/no existe |
| AC04 | Closed Account | Cuenta cerrada |
| AC06 | Blocked Account | Cuenta bloqueada |
| AG01 | Transaction Forbidden | Transacción prohibida |
| MS03 | Technical Error | Error técnico/timeout |
| DUPL | Duplicate Transaction | Transacción duplicada |
| UNKNOWN | Unknown Error | Error desconocido (fallback) |

## Variables de Entorno
```properties
PORT=8087
CONFIG_FILE=classpath:error-mappings.yml
ENABLE_ADMIN_ENDPOINTS=true
LOG_LEVEL=INFO
```

## Configuración
Ver `application.properties`:
```properties
error-mapping.config-file=classpath:error-mappings.yml
error-mapping.enable-admin-endpoints=true
```

## Ejecución

### Opción 1: Maven (Desarrollo)
```bash
cd errormappingservice
mvn spring-boot:run
```

### Opción 2: Docker
```bash
# Construir imagen
docker build -t error-mapping-service:latest .

# Ejecutar contenedor
docker run -d \
  -p 8087:8087 \
  --name error-mapping-service \
  -e JAVA_OPTS="-Xmx512m -Xms256m" \
  error-mapping-service:latest

# Ver logs
docker logs -f error-mapping-service

# Detener
docker stop error-mapping-service
docker rm error-mapping-service
```

### Opción 3: Docker Compose (Recomendado para desarrollo)
Desde la raíz del proyecto:
```bash
docker-compose up -d error-mapping-service
docker-compose logs -f error-mapping-service
docker-compose down
```

## Ejemplos de Uso

### Traducir error de Pichincha
```bash
curl -X POST http://localhost:8087/api/v2/error-mapping/traducir \
  -H "Content-Type: application/json" \
  -d '{
    "bancoCodigo": "PICHINCHA",
    "codigoOriginal": "ERROR_99",
    "mensajeOriginal": "Saldo insuficiente"
  }'
```

### Código no encontrado (fallback a UNKNOWN)
```bash
curl -X POST http://localhost:8087/api/v2/error-mapping/traducir \
  -H "Content-Type: application/json" \
  -d '{
    "bancoCodigo": "BANCO_NUEVO",
    "codigoOriginal": "XYZ_999",
    "mensajeOriginal": "Error desconocido"
  }'
```

Response:
```json
{
  "success": true,
  "data": {
    "codigoISO": "UNKNOWN",
    "descripcion": "Unknown Error",
    "mensajeEstandar": "Error desconocido del banco"
  }
}
```

### Agregar mapeos dinámicamente
```bash
curl -X POST http://localhost:8087/api/v2/error-mapping/agregar \
  -H "Content-Type: application/json" \
  -d '{
    "bancoCodigo": "BOLIVARIANO",
    "mapeos": {
      "E001": "AM04",
      "E002": "AC01",
      "E999": "MS03"
    }
  }'
```

### Listar todos los códigos ISO
```bash
curl http://localhost:8087/api/v2/error-mapping/codigos-iso
```

### Recargar configuración desde archivo
```bash
curl -X POST http://localhost:8087/api/v2/error-mapping/reload
```

## Arquitectura Interna

### ErrorMappingService
- Carga archivo YAML al iniciar (`@PostConstruct`)
- Almacena mapeos en `ConcurrentHashMap` (thread-safe)
- Método `traducirError()` busca en mapeo del banco
- Fallback a "UNKNOWN" si no encuentra mapeo
- Método `agregarMapeo()` para agregar dinámicamente
- Método `cargarConfiguracion()` para reload

### ErrorMappingController
- Endpoint `/traducir`: Siempre retorna HTTP 200 con success=true
- Endpoint `/agregar`: Administración de mapeos
- Endpoint `/codigos-iso`: Lista completa de códigos ISO
- Endpoint `/reload`: Recarga desde archivo YAML
- Health check endpoint

### DTOs
- `ErrorRequestDTO`: Entrada con bancoCodigo, codigoOriginal, mensajeOriginal
- `ErrorResponseDTO`: Salida con success y data
- `ErrorDataDTO`: codigoISO, descripcion, mensajeEstandar
- `AddMappingRequestDTO`: Para agregar mapeos dinámicamente
- `CodigoISODTO`: Para listar códigos ISO

## Logging
```
INFO - Cargados 6 mapeos de bancos
INFO - Cargados 8 códigos ISO
INFO - Traduciendo error - Banco: PICHINCHA, Código: ERROR_99
WARN - No se encontró mapeo para banco: BANCO_NUEVO, código: XYZ_999
INFO - Agregados 3 mapeos para el banco BOLIVARIANO
```

## Comportamiento de Fallback

1. **Banco existe, código existe**: Retorna el código ISO mapeado
2. **Banco existe, código NO existe**: Retorna "UNKNOWN"
3. **Banco NO existe**: Retorna "UNKNOWN"

**Siempre retorna HTTP 200** con `success: true` - nunca 404.

## Casos de Uso

### Switch Transaccional
1. Banco destino rechaza transacción con código propietario "ERROR_99"
2. Switch llama a `/traducir` con bancoCodigo="PICHINCHA" y codigoOriginal="ERROR_99"
3. Servicio retorna codigoISO="AM04" (Insufficient Funds)
4. Switch estandariza la respuesta usando el código ISO

### Configuración Dinámica
1. Se integra un nuevo banco "PRODUBANCO"
2. Admin llama a `/agregar` con los mapeos del nuevo banco
3. Mapeos se agregan a memoria sin reiniciar el servicio
4. Opcionalmente, se actualiza el archivo YAML y se llama a `/reload`

## Mejoras Futuras
- Soporte para regex en códigos (ERR_* → MS03)
- Mapeos globales (aplican a todos los bancos)
- Persistencia de mapeos dinámicos
- Cache con expiración
- Documentación OpenAPI/Swagger
- Tests unitarios y de integración

