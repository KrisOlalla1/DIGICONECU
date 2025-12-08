# 🏦 MICROSERVICIO SWITCH TRANSACCIONAL – ARCBANK

Microservicio de **switch transaccional interbancario** desarrollado en **Spring Boot 3.5.7** con **PostgreSQL** y **Redis**.  
Implementa recepción, validación, persistencia, gestión de estados, normalización de errores y pruebas unitarias.

---

## 📁 ESTRUCTURA DEL PROYECTO

```text
MicroservicioTransaccional/
├── src/
│   ├── main/
│   │   ├── java/com/arcbank/switchtransaccional/
│   │   │   ├── SwitchTransaccionalApplication.java     # Clase principal Spring Boot
│   │   │   ├── config/                                 # Configuración técnica
│   │   │   │   ├── RedisConfig.java                    # Config Redis (Persona 2)
│   │   │   │   ├── HttpClientConfig.java               # Config HTTP Client (Persona 2)
│   │   │   │   └── CorsConfig.java                     # Config CORS
│   │   │   ├── controller/
│   │   │   │   ├── TransaccionController.java          # PERSONA 1: Recepción/transacciones
│   │   │   │   └── NormalizacionErroresController.java # PERSONA 1: Normalizar códigos de error
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java         # PERSONA 1: Manejo de errores y validaciones
│   │   │   ├── middleware/
│   │   │   │   └── IdempotenciaFilter.java             # PERSONA 2: Filtro de idempotencia
│   │   │   ├── model/
│   │   │   │   ├── dto/
│   │   │   │   │   ├── TransaccionRequest.java         # PERSONA 1: JSON entrada (bancos → switch)
│   │   │   │   │   ├── TransaccionResponse.java        # PERSONA 1: Respuesta general
│   │   │   │   │   ├── TransaccionBaseResponse.java    # PERSONA 1: success, mensaje, trace, etc.
│   │   │   │   │   ├── ActualizarEstadoRequest.java    # PERSONA 1: Body PATCH estado
│   │   │   │   │   ├── ConsultaEstadoTransaccionResponse.java # PERSONA 1: DTO consulta de estado
│   │   │   │   │   └── NormalizarErrorResponse.java    # PERSONA 1: DTO normalización de errores
│   │   │   │   └── entity/
│   │   │   │       ├── TransaccionEntity.java          # PERSONA 1: Entidad tabla "Transaccion"
│   │   │   │       └── EntidadBancariaEntity.java      # PERSONA 1: Entidad tabla "EntidadBancaria"
│   │   │   ├── repository/
│   │   │   │   ├── TransaccionRepository.java          # PERSONA 1: JPA repo transacciones
│   │   │   │   └── EntidadBancariaRepository.java      # PERSONA 1: JPA repo bancos
│   │   │   ├── service/
│   │   │   │   ├── IGestorEstadosService.java          # PERSONA 1: Gestión estados/ciclo de vida
│   │   │   │   ├── INormalizacionErroresService.java   # PERSONA 1: Normalizar códigos de error
│   │   │   │   ├── IEnrutamientoService.java           # PERSONA 2: Enrutamiento HTTP a bancos
│   │   │   │   └── IIdempotenciaService.java           # PERSONA 2: Redis idempotencia
│   │   │   │
│   │   │   │   └── impl/
│   │   │   │       ├── GestorEstadosServiceImpl.java   # PERSONA 1: implementación
│   │   │   │       ├── NormalizacionErroresServiceImpl.java # PERSONA 1: implementación
│   │   │   │       ├── EnrutamientoServiceImpl.java    # PERSONA 2: implementación pendiente
│   │   │   │       └── IdempotenciaServiceImpl.java    # PERSONA 2: implementación pendiente
│   │   └── resources/
│   │       ├── application.properties                  # Configuración Spring Boot
│   │       └── init-db.sql                             # Script SQL: tablas + datos de ejemplo
│   └── test/
│       └── java/com/arcbank/switchtransaccional/
│           ├── controller/
│           │   └── TransaccionControllerTest.java      # PERSONA 1: tests del controlador
│           └── service/impl/
│               ├── GestorEstadosServiceImplTest.java   # PERSONA 1: tests gestión de estados
│               └── NormalizacionErroresServiceImplTest.java # PERSONA 1: tests normalización errores
├── pom.xml                                             # Dependencias Maven
├── mvnw / mvnw.cmd                                     # Maven Wrapper
├── Dockerfile                                          # Imagen Docker (opcional)
├── init-db.sql                                         # Script creación BD / datos bancos
└── README.md                                           # Este archivo
```

---

## 👥 DIVISIÓN DE TRABAJO

### 🔹 PERSONA 1 – Recepción, Validación, Persistencia y Estados

**Responsabilidades implementadas:**

1. **Recepción de transacciones (RF-01)**  
   Archivo: `controller/TransaccionController.java`

   - Endpoint principal:

     ```http
     POST /api/v2/switch/transfers
     Content-Type: application/json
     ```

   - Request (formato desde los bancos):

     ```json
     {
       "Transaccion": {
         "IdInstruccion": null,
         "EndToEnd": "E2E-OK-001",
         "IdBancoOrigen": 2,
         "IdBancoDestino": 1,
         "CuentaOrigen": "0102030405",
         "CuentaDestino": "9988776655",
         "Monto": 170.75,
         "Mensaje": "Pago prueba",
         "EstadoActual": "Enviado",
         "FechaCreacion": "2025-12-06T15:20:00Z",
         "CodigoRespuestaFinal": null
       }
     }
     ```

   - Validaciones realizadas:

     - El objeto raíz **Transaccion** es obligatorio.
     - `Monto` > 0 (anotación `@DecimalMin("0.01")`).
     - `IdBancoOrigen` y `IdBancoDestino` existen en `EntidadBancaria`.
     - `IdBancoOrigen` ≠ `IdBancoDestino`.
     - El banco origen/destino no está **SUSPENDIDO**.
     - `EndToEnd` debe ser **único** (verificación previa con `TransaccionRepository`).
     - `FechaCreacion` en formato ISO `yyyy-MM-dd'T'HH:mm:ss'Z'`.

   - Datos generados por el switch:

     - `IdInstruccion`: generado automáticamente por la BD (`SERIAL`).
     - `TraceId`: `UUID` para trazabilidad.
     - `EstadoActual`: se fuerza a `"RECIBIDO"` (aunque el banco envíe "Enviado").

   - Persistencia:

     Se guarda en la tabla `"Transaccion"` con estado `"RECIBIDO"` usando `TransaccionEntity` + `TransaccionRepository`.

   - Respuesta inmediata simplificada:

     ```json
     {
       "success": true,
       "IdInstruccion": 123,
       "TraceId": "UUID-GENERADO",
       "EstadoActual": "RECIBIDO",
       "Mensaje": "Transacción recibida y en proceso"
     }
     ```

   - En caso de error de negocio (ej. banco suspendido, EndToEnd duplicado, etc.), se devuelve:

     ```json
     {
       "success": false,
       "Mensaje": "BANCO_ORIGEN_SUSPENDIDO"
     }
     ```

     con el **HTTP status** correspondiente (`400 Bad Request`).

---

2. **Gestión de estados (RF-02 / RF-03)**  
   Archivo: `service/impl/GestorEstadosServiceImpl.java`

   - Ciclo de vida soportado:

     ```text
     RECIBIDO → ENRUTADO → ESPERANDO_RESPUESTA → COMPLETADO
                                              → FALLIDO
                                              → TIMEOUT
     ```

   - Métodos principales (definidos en `IGestorEstadosService`):

     ```java
     TransaccionEntity crearTransaccionRecibida(TransaccionRequest request);

     TransaccionEntity actualizarEstado(
         Integer idInstruccion,
         String nuevoEstado,
         String codigoRespuestaFinal
     );

     ConsultaEstadoTransaccionResponse obtenerEstado(Integer idInstruccion);
     ```

   - Endpoint para **actualizar estado** (usado por Persona 2 o pruebas):

     ```http
     PATCH /api/v2/switch/transfers/{idInstruccion}/estado
     ```

     Body:

     ```json
     {
       "nuevoEstado": "COMPLETADO",
       "codigoRespuestaFinal": "AC00"
     }
     ```

     Respuesta:

     ```json
     {
       "success": true,
       "IdInstruccion": 123,
       "TraceId": "abc-123",
       "EstadoActual": "COMPLETADO",
       "Mensaje": "Estado actualizado correctamente"
     }
     ```

---

3. **Consulta de estado de una transacción (RF-04)**  
   Archivo: `TransaccionController.java` + `IGestorEstadosService.obtenerEstado`

   - Endpoint:

     ```http
     GET /api/v2/switch/transfers/{idInstruccion}
     ```

   - Respuesta:

     ```json
     {
       "IdInstruccion": 123,
       "EndToEnd": "E2E-123456789",
       "TraceId": "abc-123-xyz",
       "EstadoActual": "COMPLETADO",
       "CodigoRespuestaFinal": "AC00",
       "FechaCreacion": "2025-12-06T15:20:00Z",
       "Monto": 150.75,
       "BancoOrigen": "ArckBank",
       "BancoDestino": "Ecusol"
     }
     ```

   - Internamente:
     - Se consulta `TransaccionRepository`.
     - Se obtiene el nombre de bancos desde `EntidadBancariaRepository`.
     - Si no existe la transacción → `404` con mensaje amigable.

---

4. **Normalización de errores (RF-06)**  
   Archivos:  

   - `service/INormalizacionErroresService.java`  
   - `service/impl/NormalizacionErroresServiceImpl.java`  
   - `controller/NormalizacionErroresController.java`  
   - `model/dto/NormalizarErrorResponse.java`

   - Mapeo implementado:

     | Error técnico                                | Código | Descripción                      |
     |---------------------------------------------|--------|----------------------------------|
     | HTTP 404 – “Cuenta no existe”               | AC01   | Cuenta incorrecta                |
     | HTTP 403 – “Cuenta cerrada”                 | AC04   | Cuenta cerrada                   |
     | “saldo insuficiente” / “insufficient funds” | AM04   | Fondos insuficientes             |
     | Timeout (>5s) o mensaje contiene “timeout”  | MS03   | Error técnico                    |
     | HTTP 500                                    | MS03   | Error técnico                    |
     | Banco suspendido                            | AG01   | Transacción prohibida            |
     | Operación correcta                          | AC00   | Completada exitosamente          |
     | Duplicado / idempotencia                    | DUPL   | Transacción duplicada (RF-03)    |

   - Método de servicio:

     ```java
     String normalizarCodigoError(Integer statusCode, String errorMessage);
     ```

   - Endpoint de utilidad para probar normalización:

     ```http
     POST /api/v2/switch/errors/normalize
     Content-Type: application/json
     ```

     Body:

     ```json
     {
       "statusCode": 404,
       "errorMessage": "Cuenta destino no existe"
     }
     ```

     Respuesta:

     ```json
     {
       "codigo": "AC01",
       "descripcion": "Cuenta incorrecta"
     }
     ```

   > **Nota:** Este servicio será usado internamente por Persona 2 al procesar respuestas HTTP de los bancos destino.

---

5. **Manejo global de errores y validaciones**

   Archivo: `exception/GlobalExceptionHandler.java`

   - Captura:
     - Errores de validación (`MethodArgumentNotValidException`).
     - `IllegalArgumentException`, `IllegalStateException`.
     - Entidades no encontradas (`EntityNotFoundException`).
   - Devuelve siempre un JSON consistente con:

     ```json
     {
       "timestamp": "...",
       "status": 400,
       "error": "Bad Request",
       "message": "El monto debe ser mayor a 0",
       "path": "/api/v2/switch/transfers"
     }
     ```

---

6. **Pruebas unitarias (Tarea 1.5)**

   Archivos:

   - `test/java/com/arcbank/switchtransaccional/controller/TransaccionControllerTest.java`
   - `test/java/com/arcbank/switchtransaccional/service/impl/GestorEstadosServiceImplTest.java`
   - `test/java/com/arcbank/switchtransaccional/service/impl/NormalizacionErroresServiceImplTest.java`

   Cobertura de pruebas:

   - ✅ **CrearTransferencia_DatosValidos_RetornaOk**
   - ✅ **CrearTransferencia_MontoNegativo_RetornaBadRequest**
   - ✅ **CrearTransferencia_BancoSuspendido_RetornaError**
   - ✅ **ActualizarEstado_TransaccionExiste_ActualizaCorrectamente**
   - ✅ **NormalizarError_Timeout_RetornaMS03**

   Ejecución:

   ```bash
   # Desde la raíz del proyecto
   ./mvnw test           # Linux/Mac
   mvnw.cmd test         # Windows
   ```

---

### 🔹 PERSONA 2 – Idempotencia, Enrutamiento y Comunicación HTTP

**Responsabilidades a completar dentro del código ya preparado:**

1. `service/impl/IdempotenciaServiceImpl.java`  
   Implementar interface `IIdempotenciaService` usando Redis:

   - `boolean existeTransaccion(String endToEnd)`
   - `void guardarRespuesta(String endToEnd, TransaccionResponse respuesta)`
   - `TransaccionResponse obtenerRespuestaPrevia(String endToEnd)`

   Usar clave: `switch:idempotencia:{EndToEnd}` y TTL configurado en  
   `redis.idempotencia.ttl=86400` (24h).

2. `middleware/IdempotenciaFilter.java`  

   - Interceptar **POST /api/v2/switch/transfers**.
   - Leer el `EndToEnd` del body.
   - Si existe en Redis:
     - Retornar directamente la respuesta cached (`TransaccionResponse`).
   - Si no existe:
     - Dejar pasar la petición al `TransaccionController`.

3. `service/impl/EnrutamientoServiceImpl.java`  

   - Obtener la transacción desde la BD (`TransaccionRepository`).
   - Actualizar estado a `"ENRUTADO"` / `"ESPERANDO_RESPUESTA"` usando `IGestorEstadosService`.
   - Consultar el banco destino (`EntidadBancariaRepository`) para obtener el `EndpointProduccion`.
   - Construir y enviar request HTTP con `RestTemplate` (configurado en `HttpClientConfig`).
   - Medir timeout (5s aprox.) y mapear errores con `INormalizacionErroresService`.
   - Actualizar estado final:
     - `"COMPLETADO"` + `AC00`
     - `"FALLIDO"` + código de error
     - `"TIMEOUT"` + `MS03`
   - Guardar respuesta en Redis mediante `IIdempotenciaService`.

4. **Integración con Persona 1**

   - Persona 2 debe invocar:

     ```java
     gestorEstadosService.actualizarEstado(idInstruccion, "ENRUTADO", null);
     gestorEstadosService.actualizarEstado(idInstruccion, "ESPERANDO_RESPUESTA", null);
     gestorEstadosService.actualizarEstado(idInstruccion, "COMPLETADO", "AC00");
     // etc.
     ```

   - El `TransaccionController` de Persona 1 ya tiene el punto de integración para llamar a `IEnrutamientoService` (cuando se defina el flujo final acordado en el equipo).

---

## 🗄️ BASE DE DATOS – POSTGRESQL

### Tablas principales

```sql
CREATE TABLE IF NOT EXISTS "EntidadBancaria" (
    "IdBanco" SERIAL PRIMARY KEY,
    "Nombre" VARCHAR(100) NOT NULL,
    "EndpointProduccion" VARCHAR(255),
    "Estado" VARCHAR(20) NOT NULL,
    "Saldo" NUMERIC(15,2)
);

CREATE TABLE IF NOT EXISTS "Transaccion" (
    "IdInstruccion"       SERIAL PRIMARY KEY,
    "EndToEnd"            VARCHAR(50) UNIQUE NOT NULL,
    "TraceId"             VARCHAR(50),
    "IdBancoOrigen"       INTEGER NOT NULL,
    "IdBancoDestino"      INTEGER NOT NULL,
    "CuentaOrigen"        VARCHAR(20) NOT NULL,
    "CuentaDestino"       VARCHAR(20) NOT NULL,
    "Monto"               NUMERIC(15,2) NOT NULL,
    "Mensaje"             VARCHAR(200),
    "EstadoActual"        VARCHAR(20) NOT NULL,
    "FechaCreacion"       TIMESTAMP NOT NULL,
    "FechaProcesamiento"  TIMESTAMP NULL,
    "CodigoRespuestaFinal" VARCHAR(10),
    "MensajeRespuesta"    VARCHAR(255)
);
```

En `init-db.sql` se incluyen ejemplos de bancos:

```sql
INSERT INTO "EntidadBancaria" ("IdBanco", "Nombre", "EndpointProduccion", "Estado", "Saldo") VALUES
  (1, 'ArckBank',          'http://dummy-arckbank',       'ACTIVO',      1000000),
  (2, 'Ecusol',            'http://dummy-ecusol',         'ACTIVO',      1000000),
  (3, 'EcusolDuplicado',   'http://dummy-ecusol-dup',     'SUSPENDIDO',  1000000),
  (4, 'ArckBankduplicado', 'http://dummy-arckbank-dup',   'ACTIVO',      1000000);
```

---

## ⚙️ CONFIGURACIÓN PRINCIPAL (`application.properties`)

### PostgreSQL

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/switch_db
spring.datasource.username=postgres
spring.datasource.password=Admin123
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
```

### Redis

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
redis.idempotencia.ttl=86400  # 24 horas
```

### Otros

```properties
server.port=8083

# Estados válidos para el switch
switch.estados.validos=RECIBIDO,ENRUTADO,ESPERANDO_RESPUESTA,COMPLETADO,FALLIDO,TIMEOUT
```

---

## 🚀 CÓMO EJECUTAR

1. **Levantar PostgreSQL**

```bash
createdb -U postgres -p 5432 switch_db
psql -U postgres -d switch_db -f init-db.sql
```

2. **Levantar Redis**

```bash
docker run -d -p 6379:6379 --name redis-switch redis:latest
```

3. **Compilar y ejecutar**

```bash
# Windows
mvnw.cmd clean install
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run
```

4. **Health check**

```bash
curl http://localhost:8083/actuator/health
```

---

## 🧪 PRUEBAS MANUALES CON POSTMAN

### 1. Crear transferencia válida

```http
POST http://localhost:8083/api/v2/switch/transfers
Content-Type: application/json
```

Body:

```json
{
  "Transaccion": {
    "IdInstruccion": null,
    "EndToEnd": "E2E-OK-001",
    "IdBancoOrigen": 2,
    "IdBancoDestino": 1,
    "CuentaOrigen": "0102030405",
    "CuentaDestino": "9988776655",
    "Monto": 170.75,
    "Mensaje": "Pago prueba",
    "EstadoActual": "Enviado",
    "FechaCreacion": "2025-12-06T15:20:00Z",
    "CodigoRespuestaFinal": null
  }
}
```

### 2. Consultar estado

```http
GET http://localhost:8083/api/v2/switch/transfers/{idInstruccion}
```

### 3. Actualizar estado (simular respuesta del banco)

```http
PATCH http://localhost:8083/api/v2/switch/transfers/{idInstruccion}/estado
Content-Type: application/json
```

```json
{
  "nuevoEstado": "COMPLETADO",
  "codigoRespuestaFinal": "AC00"
}
```

### 4. Normalizar error

```http
POST http://localhost:8083/api/v2/switch/errors/normalize
Content-Type: application/json
```

```json
{
  "statusCode": 504,
  "errorMessage": "timeout al invocar banco destino"
}
```

Respuesta esperada:

```json
{
  "codigo": "MS03",
  "descripcion": "Error técnico"
}
```

---

## ✅ RESUMEN DE LO IMPLEMENTADO POR PERSONA 1

- ✅ Recepción y validación de transacciones (RF-01).
- ✅ Persistencia en PostgreSQL con estado inicial `RECIBIDO`.
- ✅ Gestión del ciclo de vida de estados (RECIBIDO → ENRUTADO → ESPERANDO_RESPUESTA → COMPLETADO/FALLIDO/TIMEOUT).
- ✅ Endpoint de consulta de estado por `IdInstruccion` (RF-04).
- ✅ Servicio y endpoint de normalización de errores (RF-06).
- ✅ Manejo global de errores y validaciones.
- ✅ Pruebas unitarias para:
  - Recepción OK.
  - Validación monto negativo.
  - Banco suspendido.
  - Actualización de estado.
  - Normalización de errores.

---

## 🔜 PENDIENTE PARA PERSONA 2

- Implementar **idempotencia con Redis** (`IdempotenciaServiceImpl` + `IdempotenciaFilter`).
- Implementar **enrutamiento HTTP** hacia bancos reales (`EnrutamientoServiceImpl`).
- Integrar con los estados y códigos normalizados de Persona 1.
- Añadir pruebas unitarias específicas para Persona 2.

---

🙌 Con este README tu profesor puede ver claramente:
- Qué se implementó como **Persona 1**.
- Qué endpoints están listos y probados.
- Qué queda como responsabilidad de **Persona 2**.
