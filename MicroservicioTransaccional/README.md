# 🏦 MICROSERVICIO SWITCH TRANSACCIONAL - ARCBANK

Sistema de enrutamiento de transacciones interbancarias desarrollado en Spring Boot con Redis para idempotencia.

---

## 📁 ESTRUCTURA DEL PROYECTO

```
MicroservicioTransaccional/
├── src/
│   ├── main/
│   │   ├── java/com/arcbank/switch/
│   │   │   ├── SwitchTransaccionalApplication.java      # Clase principal
│   │   │   ├── config/                                  # Configuraciones
│   │   │   │   ├── RedisConfig.java                    # Config Redis
│   │   │   │   ├── HttpClientConfig.java               # Config HTTP Client
│   │   │   │   └── CorsConfig.java                     # Config CORS
│   │   │   ├── controller/                              # REST Controllers
│   │   │   │   └── TransaccionController.java          # PERSONA 1: Endpoints REST
│   │   │   ├── service/                                 # Interfaces de servicios
│   │   │   │   ├── IEnrutamientoService.java           # PERSONA 2: Enrutamiento HTTP
│   │   │   │   ├── IGestorEstadosService.java          # PERSONA 1: Gestión estados
│   │   │   │   └── IIdempotenciaService.java           # PERSONA 2: Redis idempotencia
│   │   │   ├── service/impl/                            # Implementaciones
│   │   │   │   ├── EnrutamientoServiceImpl.java        # PERSONA 2: Implementar
│   │   │   │   ├── GestorEstadosServiceImpl.java       # PERSONA 1: Implementar
│   │   │   │   └── IdempotenciaServiceImpl.java        # PERSONA 2: Implementar
│   │   │   ├── model/                                   # Modelos de datos
│   │   │   │   ├── dto/                                # Data Transfer Objects
│   │   │   │   │   ├── TransaccionRequest.java         # JSON entrada
│   │   │   │   │   └── TransaccionResponse.java        # JSON salida
│   │   │   │   └── entity/                             # Entidades JPA
│   │   │   │       ├── TransaccionEntity.java          # Tabla Transaccion
│   │   │   │       └── EntidadBancariaEntity.java      # Tabla EntidadBancaria
│   │   │   ├── repository/                              # Repositorios JPA
│   │   │   │   ├── TransaccionRepository.java
│   │   │   │   └── EntidadBancariaRepository.java
│   │   │   └── middleware/                              # Filtros HTTP
│   │   │       └── IdempotenciaFilter.java             # PERSONA 2: Middleware
│   │   └── resources/
│   │       └── application.properties                   # Configuración app
│   └── test/
│       └── java/com/arcbank/switch/                    # Tests unitarios
├── pom.xml                                              # Dependencias Maven
├── mvnw                                                 # Maven Wrapper (Linux/Mac)
├── mvnw.cmd                                             # Maven Wrapper (Windows)
└── README.md                                            # Este archivo
```

---

## 👥 DIVISIÓN DE TRABAJO

### 🔹 PERSONA 1: Recepción, Validación y Persistencia

**Archivos a implementar:**

- `controller/TransaccionController.java`
- `service/impl/GestorEstadosServiceImpl.java`

**Tareas:**

1. ✅ **Endpoint POST /api/transacciones**

   - Validar request con `@Valid`
   - Verificar que `EndToEnd` no exista (evitar duplicados)
   - Persistir transacción en estado "Enviado"
   - Llamar a `enrutamientoService.procesarTransaccion(idInstruccion)`
   - Retornar respuesta

2. ✅ **Endpoint GET /api/transacciones/{id}**

   - Consultar transacción por ID
   - Retornar estado actual

3. ✅ **Endpoint GET /api/transacciones/endtoend/{endtoend}**

   - Consultar por EndToEnd único

4. ✅ **Implementar IGestorEstadosService**
   - `actualizarEstado()`: Actualizar estado en BD
   - `obtenerEstado()`: Consultar estado actual

**Dependencias que debes inyectar:**

```java
@Autowired
private TransaccionRepository transaccionRepository;

@Autowired
private IEnrutamientoService enrutamientoService; // Implementado por Persona 2

@Autowired
private IGestorEstadosService gestorEstadosService; // Tu implementación
```

---

### 🔹 PERSONA 2 (TÚ): Idempotencia, Enrutamiento y Comunicación HTTP

**Archivos a implementar:**

- `service/impl/IdempotenciaServiceImpl.java`
- `service/impl/EnrutamientoServiceImpl.java`
- `middleware/IdempotenciaFilter.java`

**Tareas:**

1. ✅ **Implementar IIdempotenciaService**

   - `obtenerRespuestaPrevia()`: Buscar en Redis con clave `switch:idempotencia:{endToEnd}`
   - `guardarRespuesta()`: Guardar en Redis con TTL de 24 horas
   - `existeTransaccion()`: Verificar existencia en Redis

2. ✅ **Implementar IdempotenciaFilter (Middleware)**

   - Interceptar todas las peticiones POST a `/api/transacciones`
   - Extraer `EndToEnd` del body
   - Si existe en Redis, retornar respuesta cached directamente
   - Si no existe, continuar con el flujo normal

3. ✅ **Implementar IEnrutamientoService**
   - Obtener transacción desde BD
   - Actualizar estado a "Procesando"
   - Obtener información del banco destino desde `EntidadBancariaRepository`
   - Preparar payload HTTP según formato del banco
   - Enviar petición HTTP usando `RestTemplate`
   - Procesar respuesta del banco
   - Actualizar estado según resultado (Exitoso/Fallido/Timeout)
   - Guardar respuesta en Redis para idempotencia

**Dependencias que debes inyectar:**

```java
@Autowired
private TransaccionRepository transaccionRepository;

@Autowired
private EntidadBancariaRepository bancoRepository;

@Autowired
private IGestorEstadosService gestorEstadosService; // Implementado por Persona 1

@Autowired
private IIdempotenciaService idempotenciaService; // Tu implementación

@Autowired
private RestTemplate restTemplate; // Ya configurado en HttpClientConfig
```

---

## 🗄️ BASE DE DATOS POSTGRESQL

### Tabla: `Transaccion`

```sql
CREATE TABLE "Transaccion" (
    "IdInstruccion" SERIAL PRIMARY KEY,
    "EndToEnd" VARCHAR(50) UNIQUE NOT NULL,
    "TraceId" VARCHAR(50),
    "IdBancoOrigen" INTEGER NOT NULL,
    "IdBancoDestino" INTEGER NOT NULL,
    "CuentaOrigen" VARCHAR(20) NOT NULL,
    "CuentaDestino" VARCHAR(20) NOT NULL,
    "Monto" DECIMAL(15,2) NOT NULL,
    "Mensaje" VARCHAR(200),
    "EstadoActual" VARCHAR(20) NOT NULL,
    "FechaCreacion" TIMESTAMP NOT NULL,
    "CodigoRespuestaFinal" VARCHAR(10)
);
```

### Tabla: `EntidadBancaria`

```sql
CREATE TABLE "EntidadBancaria" (
    "IdBanco" SERIAL PRIMARY KEY,
    "Nombre" VARCHAR(100) NOT NULL,
    "EndpointProduccion" VARCHAR(255),
    "Estado" VARCHAR(20),
    "Saldo" DECIMAL(15,2)
);
```

---

## 🔴 REDIS

### Estructura de claves para idempotencia:

```
Clave: switch:idempotencia:{EndToEnd}
Valor: JSON de TransaccionResponse
TTL: 86400 segundos (24 horas)
```

**Ejemplo:**

```
Clave: switch:idempotencia:E2E-123456789
Valor: {"idInstruccion":1,"endToEnd":"E2E-123456789","estadoActual":"Exitoso",...}
TTL: 86400
```

---

## 📦 DEPENDENCIAS INCLUIDAS

- ✅ Spring Boot 3.5.7 (Java 21)
- ✅ Spring Data JPA (PostgreSQL)
- ✅ Spring Data Redis (Jedis)
- ✅ Spring Boot Validation
- ✅ Apache HttpClient 5 (Para HTTP)
- ✅ Lombok
- ✅ Jackson (JSON)
- ✅ PostgreSQL Driver
- ✅ Google Cloud SQL Socket Factory

---

## ⚙️ CONFIGURACIÓN

### application.properties

**Configuración local (PostgreSQL + Redis):**

```properties
# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5433/switch_db
spring.datasource.username=postgres
spring.datasource.password=123

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
redis.idempotencia.ttl=86400

# Puerto del microservicio
server.port=8083
```

---

## 🚀 CÓMO EJECUTAR

### 1. Iniciar PostgreSQL

```bash
# Asegúrate de tener PostgreSQL corriendo en puerto 5433
# Crea la base de datos:
createdb -U postgres -p 5433 switch_db

# Ejecuta los scripts SQL para crear las tablas
```

### 2. Iniciar Redis

```bash
# Docker:
docker run -d -p 6379:6379 redis:latest

# O instala Redis localmente
```

### 3. Compilar y ejecutar el microservicio

```bash
# Windows:
cd MicroservicioTransaccional
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run

# Linux/Mac:
cd MicroservicioTransaccional
./mvnw clean install
./mvnw spring-boot:run
```

### 4. Verificar que está corriendo

```bash
# Health check
curl http://localhost:8083/actuator/health
```

---

## 📝 FORMATO JSON DE ENTRADA

**POST /api/transacciones**

```json
{
  "Transaccion": {
    "EndToEnd": "E2E-123456789",
    "IdBancoOrigen": 1,
    "IdBancoDestino": 2,
    "CuentaOrigen": "0102030405",
    "CuentaDestino": "9988776655",
    "Monto": 150.75,
    "Mensaje": "Pago de servicio",
    "EstadoActual": "Enviado",
    "FechaCreacion": "2025-12-06T15:20:00Z"
  }
}
```

**Respuesta esperada:**

```json
{
  "idInstruccion": 1,
  "endToEnd": "E2E-123456789",
  "traceId": "TRACE-ABC123",
  "estadoActual": "Exitoso",
  "codigoRespuestaFinal": "00",
  "mensajeRespuesta": "Transacción procesada exitosamente",
  "exitoso": true,
  "fechaProcesamiento": "2025-12-06T15:20:05Z"
}
```

---

## 🔄 FLUJO DE TRABAJO

### Flujo normal de una transacción:

```
1. Cliente → POST /api/transacciones (JSON)
   ↓
2. IdempotenciaFilter verifica en Redis si ya fue procesada
   ↓
3. TransaccionController (PERSONA 1) recibe y valida
   ↓
4. Persiste en BD con estado "Enviado"
   ↓
5. Llama a EnrutamientoService (PERSONA 2)
   ↓
6. EnrutamientoService actualiza estado a "Procesando"
   ↓
7. Obtiene endpoint del banco destino desde BD
   ↓
8. Envía petición HTTP al banco con RestTemplate
   ↓
9. Procesa respuesta del banco
   ↓
10. Actualiza estado (Exitoso/Fallido) usando IGestorEstadosService
   ↓
11. Guarda respuesta en Redis (IIdempotenciaService)
   ↓
12. Retorna respuesta al cliente
```

---

## 🧪 TESTING

### Probar endpoint con curl:

```bash
curl -X POST http://localhost:8083/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{
    "Transaccion": {
      "EndToEnd": "E2E-TEST-001",
      "IdBancoOrigen": 1,
      "IdBancoDestino": 2,
      "CuentaOrigen": "1234567890",
      "CuentaDestino": "0987654321",
      "Monto": 100.50,
      "Mensaje": "Test",
      "EstadoActual": "Enviado",
      "FechaCreacion": "2025-12-06T15:00:00Z"
    }
  }'
```

### Verificar Redis:

```bash
# Conectarse a Redis CLI
redis-cli

# Ver todas las claves
KEYS switch:idempotencia:*

# Ver una clave específica
GET switch:idempotencia:E2E-TEST-001
```

---

## 🎯 PUNTOS DE INTEGRACIÓN

### Persona 1 llama a Persona 2:

```java
// En TransaccionController
TransaccionResponse respuesta = enrutamientoService.procesarTransaccion(idInstruccion);
```

### Persona 2 llama a Persona 1:

```java
// En EnrutamientoServiceImpl
gestorEstadosService.actualizarEstado(idInstruccion, "Procesando", null);
// ... procesar
gestorEstadosService.actualizarEstado(idInstruccion, "Exitoso", "00");
```

---

## ⚠️ NOTAS IMPORTANTES

1. **Estados válidos:** Enviado, Procesando, Exitoso, Fallido, Timeout
2. **EndToEnd debe ser único** en toda la BD
3. **TTL de Redis:** 24 horas por defecto (configurable)
4. **Timeouts HTTP:** Configurados en `application.properties`
5. **Puerto del microservicio:** 8083 (configurable)

---

## 📚 RECURSOS ÚTILES

- [Documentación Spring Boot](https://spring.io/projects/spring-boot)
- [Documentación Spring Data Redis](https://spring.io/projects/spring-data-redis)
- [Documentación Apache HttpClient](https://hc.apache.org/httpcomponents-client-5.0.x/)
- [Redis Commands](https://redis.io/commands/)

---

## 🤝 CONTACTO Y SOPORTE

Para dudas sobre la integración entre módulos, coordinar con tu compañero de desarrollo.

**¡Éxito con el desarrollo!** 🚀
