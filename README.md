# 🏦 Switch Transaccional DIGICONECU

Sistema de switch transaccional bancario completo con arquitectura de microservicios, diseñado para procesar transferencias interbancarias en Ecuador siguiendo estándares ISO 20022.

---

## 📋 Tabla de Contenidos

1. [Arquitectura General](#arquitectura-general)
2. [Microservicios](#microservicios)
3. [Requisitos Previos](#requisitos-previos)
4. [Instalación y Ejecución](#instalación-y-ejecución)
5. [Configuración de Puertos](#configuración-de-puertos)
6. [Esquema de Base de Datos](#esquema-de-base-de-datos)
7. [API Endpoints](#api-endpoints)
8. [Flujo de Transferencia](#flujo-de-transferencia)
9. [Pruebas del Sistema](#pruebas-del-sistema)
10. [Monitoreo y Métricas](#monitoreo-y-métricas)

---

## 🏗️ Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API GATEWAY (8080)                              │
│                    Spring Cloud Gateway + Rate Limiting                      │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        │                         │                         │
        ▼                         ▼                         ▼
┌───────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   PAYMENT     │       │    NETWORK      │       │    ACCOUNT      │
│  PROCESSING   │◄─────►│   MANAGEMENT    │◄─────►│    BALANCE      │
│    (8081)     │       │     (8082)      │       │     (8083)      │
└───────┬───────┘       └────────┬────────┘       └────────┬────────┘
        │                        │                         │
        │                        │                         │
        ▼                        ▼                         ▼
┌───────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   CLEARING    │       │     RETURN      │       │  NOTIFICATION   │
│   SERVICE     │       │   MANAGEMENT    │       │    SERVICE      │
│    (8084)     │       │     (8085)      │       │     (8086)      │
└───────────────┘       └─────────────────┘       └─────────────────┘
        │
        └──────────────────────────┐
                                   ▼
                          ┌─────────────────┐
                          │  ERROR MAPPING  │
                          │    SERVICE      │
                          │     (8087)      │
                          └─────────────────┘
```

### Tecnologías Utilizadas

| Componente    | Tecnología                  |
| ------------- | --------------------------- |
| Backend       | Java 21 + Spring Boot 3.5.9 |
| API Gateway   | Spring Cloud Gateway 3.4.4  |
| Base de Datos | PostgreSQL 15               |
| Contenedores  | Docker + Docker Compose     |
| ORM           | Hibernate 7.2 / JPA         |
| Build         | Maven                       |

---

## 🔧 Microservicios

### 1. API Gateway (Puerto 8080)

**Ruta base:** `/`

Punto de entrada único al sistema. Proporciona:

- Enrutamiento inteligente a microservicios
- Rate limiting por IP
- Balanceo de carga
- Logging centralizado

### 2. Payment Processing (Puerto 8081)

**Ruta base:** `/api/v2/transfers`

Servicio principal de procesamiento de transferencias:

- Validación de transacciones entrantes
- Coordinación del flujo de transferencia
- Manejo de idempotencia
- Métricas de rendimiento

**Endpoints principales:**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v2/transfers` | Crear transferencia |
| GET | `/api/v2/transfers/{instructionId}` | Consultar estado |
| GET | `/api/v2/transfers/metrics` | Métricas del switch |
| GET | `/api/v2/transfers/health` | Health check |

### 3. Network Management (Puerto 8082)

**Ruta base:** `/api/v1/red`

Gestión de la red bancaria:

- Registro de bancos participantes
- Enrutamiento por rangos BIN
- Circuit breaker para bancos
- Monitoreo de disponibilidad

**Endpoints principales:**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/red/bancos` | Lista de bancos |
| GET | `/api/v1/red/bancos/{codigo}` | Detalle de banco |
| POST | `/api/v1/red/enrutamiento` | Resolver enrutamiento |
| GET | `/api/v1/red/bins` | Rangos BIN configurados |
| POST | `/api/v1/red/bancos/{codigo}/fallos` | Reportar fallo |
| GET | `/api/v1/red/circuit-breaker/stats` | Estado circuit breakers |

### 4. Account Balance (Puerto 8083)

**Ruta base:** `/api/v2/balance`

Gestión de cuentas de prefondeo:

- Control de saldos por banco
- Congelamiento de fondos
- Liberación de fondos
- Historial de operaciones

**Endpoints principales:**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v2/balance/saldo/{bancoCodigo}` | Consultar saldo |
| POST | `/api/v2/balance/verificar` | Verificar saldo suficiente |
| POST | `/api/v2/balance/congelar` | Congelar fondos |
| POST | `/api/v2/balance/liberar` | Liberar fondos |
| POST | `/api/v2/balance/completar` | Completar transferencia |
| POST | `/api/v2/balance/recargar` | Recargar cuenta |
| GET | `/api/v2/balance/operaciones/{bancoCodigo}` | Historial operaciones |

### 5. Clearing Service (Puerto 8084)

**Ruta base:** `/api/v2/clearing`

Servicio de compensación y liquidación:

- Ciclos de compensación
- Generación de archivos XML/CSV
- Cálculo de posiciones netas
- Liquidación interbancaria

**Endpoints principales:**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v2/clearing/ciclos/iniciar` | Iniciar ciclo |
| GET | `/api/v2/clearing/ciclos/activos` | Ciclos activos |
| POST | `/api/v2/clearing/ciclos/{id}/cerrar` | Cerrar ciclo |
| GET | `/api/v2/clearing/posiciones/{cicloId}` | Posiciones netas |

### 6. Return Management (Puerto 8085)

**Ruta base:** `/api/v2/returns`

Gestión de devoluciones:

- Procesamiento de devoluciones
- Validación de plazos (D+2)
- Reversión de operaciones
- Notificación a partes

**Endpoints principales:**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v2/returns` | Crear devolución |
| GET | `/api/v2/returns/{id}` | Consultar devolución |
| GET | `/api/v2/returns/transaccion/{txId}` | Devoluciones por TX |
| POST | `/api/v2/returns/{id}/aprobar` | Aprobar devolución |
| POST | `/api/v2/returns/{id}/rechazar` | Rechazar devolución |

### 7. Notification Service (Puerto 8086)

**Ruta base:** `/api/v1/notificaciones`

Sistema de notificaciones:

- Notificaciones por email
- Notificaciones por webhook
- Cola de mensajes pendientes
- Reintentos automáticos

### 8. Error Mapping Service (Puerto 8087)

**Ruta base:** `/api/v2/error-mapping`

Mapeo de códigos de error ISO 20022:

- Catálogo de errores estándar
- Traducción de códigos
- Mensajes localizados

**Endpoints principales:**
| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v2/error-mapping/codigos-iso` | Lista códigos ISO |
| GET | `/api/v2/error-mapping/health` | Health check |

---

## 📦 Requisitos Previos

- **Docker** v20.10+
- **Docker Compose** v2.0+
- **Java 21** (solo para desarrollo local)
- **Maven 3.9+** (solo para desarrollo local)
- **8GB RAM** mínimo recomendado

---

## 🚀 Instalación y Ejecución

### Opción 1: Docker Compose (Recomendado)

```bash
# Clonar el repositorio
git clone <repository-url>
cd Switch-Transaccional

# Iniciar todos los servicios
docker-compose up -d

# Verificar estado
docker-compose ps

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down

# Detener y eliminar volúmenes (reinicio limpio)
docker-compose down -v
```

### Opción 2: Desarrollo Local

```bash
# Compilar todos los servicios
./mvnw clean package -DskipTests

# Iniciar solo las bases de datos
docker-compose up -d postgres-payment postgres-network postgres-balance postgres-clearing postgres-return

# Ejecutar cada servicio individualmente
cd DIGICONECU-feature-MicroPaymentProcessing
./mvnw spring-boot:run
```

---

## 🔌 Configuración de Puertos

| Servicio           | Puerto Aplicación | Puerto PostgreSQL |
| ------------------ | ----------------- | ----------------- |
| API Gateway        | 8080              | -                 |
| Payment Processing | 8081              | 5441              |
| Network Management | 8082              | 5442              |
| Account Balance    | 8083              | 5443              |
| Clearing Service   | 8084              | 5444              |
| Return Management  | 8085              | 5445              |
| Notification       | 8086              | -                 |
| Error Mapping      | 8087              | -                 |

---

## 🗄️ Esquema de Base de Datos

### PaymentProcessingDB (Puerto 5441)

```sql
-- Transacciones
CREATE TABLE "Transacciones" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "InstructionId" UUID UNIQUE NOT NULL,
    "EndToEndId" VARCHAR(35),
    "BancoOrigenCodigo" VARCHAR(20) NOT NULL,
    "BancoDestinoCodigo" VARCHAR(20) NOT NULL,
    "Monto" DECIMAL(18,2) NOT NULL,
    "Moneda" VARCHAR(3) NOT NULL DEFAULT 'USD',
    "CuentaOrigen" VARCHAR(50) NOT NULL,
    "CuentaDestino" VARCHAR(50) NOT NULL,
    "Estado" VARCHAR(30) NOT NULL DEFAULT 'Recibida',
    "CodigoError" VARCHAR(10),
    "MensajeError" TEXT,
    "FechaCreacion" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Cache de Idempotencia
CREATE TABLE "IdempotenciaCache" (
    "InstructionId" UUID PRIMARY KEY,
    "RespuestaJson" TEXT NOT NULL,
    "FechaCreacion" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### NetworkManagementDB (Puerto 5442)

```sql
-- Bancos
CREATE TABLE "Bancos" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "Codigo" VARCHAR(20) UNIQUE NOT NULL,
    "Nombre" VARCHAR(100) NOT NULL,
    "Endpoint" VARCHAR(255) NOT NULL,
    "Estado" VARCHAR(20) NOT NULL DEFAULT 'Activo',
    "FallosConsecutivos" INTEGER DEFAULT 0,
    "EstadoCircuito" VARCHAR(20) DEFAULT 'CERRADO',
    "UltimoFallo" TIMESTAMP,
    "CircuitoAbiertoDesde" TIMESTAMP,
    "UltimoHealthCheck" TIMESTAMP,
    "LatenciaPromedioMs" INTEGER DEFAULT 0
);

-- Enrutamiento BIN
CREATE TABLE "Enrutamiento" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "BancoId" UUID NOT NULL REFERENCES "Bancos"("Id"),
    "BinInicio" VARCHAR(6) NOT NULL,
    "BinFin" VARCHAR(6) NOT NULL,
    "Activo" BOOLEAN DEFAULT true
);
```

### AccountBalanceDB (Puerto 5443)

```sql
-- Cuentas de Prefondeo
CREATE TABLE "CuentasPrefondeo" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "BancoCodigo" VARCHAR(20) UNIQUE NOT NULL,
    "SaldoTotal" DECIMAL(18,2) NOT NULL DEFAULT 0,
    "SaldoCongelado" DECIMAL(18,2) NOT NULL DEFAULT 0,
    "Moneda" VARCHAR(3) NOT NULL DEFAULT 'USD',
    "FechaActualizacion" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Operaciones de Prefondeo
CREATE TABLE "OperacionesPrefondeo" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "CuentaId" UUID NOT NULL REFERENCES "CuentasPrefondeo"("Id"),
    "InstructionId" UUID NOT NULL,
    "TipoOperacion" VARCHAR(20) NOT NULL,
    "Monto" DECIMAL(18,2) NOT NULL,
    "Estado" VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    "FechaCreacion" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### ClearingDB (Puerto 5444)

```sql
-- Ciclos de Compensación
CREATE TABLE "CiclosCompensacion" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "FechaInicio" TIMESTAMP NOT NULL,
    "FechaCierre" TIMESTAMP,
    "Estado" VARCHAR(20) NOT NULL DEFAULT 'Abierto',
    "TotalTransacciones" INTEGER DEFAULT 0,
    "MontoTotal" DECIMAL(18,2) DEFAULT 0
);
```

### ReturnManagementDB (Puerto 5445)

```sql
-- Devoluciones
CREATE TABLE "Devoluciones" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "TransaccionOriginalId" UUID NOT NULL,
    "TransaccionInversaId" UUID,
    "Motivo" VARCHAR(100) NOT NULL,
    "CodigoMotivo" VARCHAR(10),
    "Estado" VARCHAR(20) NOT NULL DEFAULT 'Pendiente',
    "FechaSolicitud" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "FechaResolucion" TIMESTAMP
);
```

---

## 🔄 Flujo de Transferencia

```
1. Cliente envía POST /api/v2/transfers
   │
   ▼
2. Payment Processing valida request
   │
   ├─► Verificar idempotencia (InstructionId único)
   │
   ▼
3. Resolver enrutamiento (Network Management)
   │
   ├─► Extraer BIN de cuenta destino (primeros 6 dígitos)
   ├─► Buscar banco destino por rango BIN
   │
   ▼
4. Verificar saldo (Account Balance)
   │
   ├─► Consultar saldo disponible banco origen
   │
   ▼
5. Congelar fondos (Account Balance)
   │
   ├─► SaldoDisponible -= Monto
   ├─► SaldoCongelado += Monto
   │
   ▼
6. Procesar transferencia
   │
   ├─► Actualizar estado: "EsperandoRespuesta"
   │
   ▼
7. Completar transferencia (Account Balance)
   │
   ├─► SaldoCongelado (origen) -= Monto
   ├─► SaldoTotal (destino) += Monto
   │
   ▼
8. Actualizar estado: "Completada"
   │
   ▼
9. Cachear respuesta (Idempotencia)
   │
   ▼
10. Retornar respuesta al cliente
```

---

## 🧪 Pruebas del Sistema

### Verificar Estado de Servicios

```bash
# Health checks
curl http://localhost:8081/api/v2/transfers/health
curl http://localhost:8082/api/v1/red/health
curl http://localhost:8083/api/v2/balance/health
curl http://localhost:8084/api/v2/clearing/health
curl http://localhost:8085/api/v2/returns/health
curl http://localhost:8087/api/v2/error-mapping/health
```

### Crear Transferencia

```bash
curl -X POST http://localhost:8081/api/v2/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "instructionId": "550e8400-e29b-41d4-a716-446655440000",
    "monto": 500.00,
    "moneda": "USD",
    "cuentaOrigen": "1234567890",
    "cuentaDestino": "2234567890",
    "bancoOrigen": "PICHINCHA",
    "concepto": "Pago de servicios"
  }'
```

### Consultar Estado de Transferencia

```bash
curl http://localhost:8081/api/v2/transfers/550e8400-e29b-41d4-a716-446655440000
```

### Consultar Saldo de Banco

```bash
curl http://localhost:8083/api/v2/balance/saldo/PICHINCHA
```

### Listar Bancos y BINs

```bash
curl http://localhost:8082/api/v1/red/bancos
curl http://localhost:8082/api/v1/red/bins
```

### Respuestas Esperadas

**Transferencia Exitosa:**

```json
{
  "data": {
    "instructionId": "550e8400-e29b-41d4-a716-446655440000",
    "estado": "Completada",
    "bancoOrigen": "PICHINCHA",
    "bancoDestino": "GUAYAQUIL",
    "monto": 500.0,
    "timestamp": "2025-12-21T01:21:31.182Z"
  },
  "error": null,
  "success": true
}
```

**Error de Validación:**

```json
{
  "data": null,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "bancoOrigen: bancoOrigen es requerido"
  },
  "success": false
}
```

---

## 📊 Monitoreo y Métricas

### Métricas del Switch

```bash
curl http://localhost:8081/api/v2/transfers/metrics
```

**Respuesta:**

```json
{
  "totalTransacciones": 8,
  "latenciaPromedioMs": 201,
  "latenciaMaximaTolerableMs": 200,
  "tpsObjetivo": 100,
  "timestamp": "2025-12-21T01:22:44.071Z"
}
```

### Circuit Breaker Stats

```bash
curl http://localhost:8082/api/v1/red/circuit-breaker/stats
```

### Logs de Contenedores

```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f payment-processing

# Ver últimas 100 líneas
docker-compose logs --tail=100 payment-processing
```

---

## 🔐 Códigos de Error ISO 20022

| Código | Descripción               | Mensaje                              |
| ------ | ------------------------- | ------------------------------------ |
| AM04   | Insufficient Funds        | El banco no tiene fondos suficientes |
| AC04   | Closed Account            | La cuenta destino está cerrada       |
| AC06   | Blocked Account           | La cuenta destino está bloqueada     |
| RC01   | Bank Identifier Incorrect | Código de banco inválido             |
| MS03   | Technical Error           | Error técnico en el sistema          |
| DUPL   | Duplicate Transaction     | Transacción duplicada                |
| NARR   | Narrative                 | Error descriptivo personalizado      |

---

## 🏦 Bancos Configurados (Datos Semilla)

| Código      | Nombre             | Rango BIN       |
| ----------- | ------------------ | --------------- |
| PICHINCHA   | Banco Pichincha    | 100000 - 199999 |
| GUAYAQUIL   | Banco de Guayaquil | 200000 - 299999 |
| PACIFICO    | Banco del Pacífico | 300000 - 399999 |
| PRODUBANCO  | Produbanco         | 400000 - 499999 |
| BOLIVARIANO | Banco Bolivariano  | 500000 - 599999 |

### Saldos Iniciales de Prefondeo

Cada banco inicia con **$500,000 USD** en su cuenta de prefondeo.

---

## 📁 Estructura del Proyecto

```
Switch-Transaccional/
├── docker-compose.yml              # Orquestación de contenedores
├── README.md                       # Esta documentación
│
├── DIGICONECU-api-gateway/         # API Gateway
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/main/java/...
│
├── DIGICONECU-feature-MicroPaymentProcessing/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/payment/payment_processing/
│       │   ├── controller/         # Controladores REST
│       │   ├── service/            # Lógica de negocio
│       │   ├── model/              # Entidades JPA
│       │   ├── repository/         # Repositorios
│       │   ├── dto/                # Data Transfer Objects
│       │   └── client/             # Clientes HTTP
│       └── main/resources/
│           ├── application.properties
│           └── db/init.sql         # Script inicialización BD
│
├── DIGICONECU-network-management-service/
├── DIGICONECU-feature-MicroAccountBalance/
├── DIGICONECU-feature-MicroClearingService/
├── DIGICONECU-feature-MicroReturnManagmentService/
├── DIGICONECU-notificacion-service/
└── DIGICONECU-error-mapping-service/
```

---

## 🐛 Troubleshooting

### Servicio no inicia

```bash
# Ver logs del servicio
docker logs <nombre-contenedor>

# Reiniciar servicio específico
docker-compose restart <servicio>

# Reconstruir imagen
docker-compose build --no-cache <servicio>
```

### Base de datos no inicializa

```bash
# Eliminar volumen y recrear
docker-compose down -v
docker-compose up -d
```

### Error de conexión entre servicios

```bash
# Verificar red
docker network inspect switch-transaccional-network

# Verificar DNS interno
docker exec payment-processing ping network-management
```

### Puerto ocupado

```bash
# En Windows
netstat -ano | findstr :<puerto>

# Detener proceso
taskkill /PID <pid> /F
```

---

## ✅ Resultados de Pruebas (Diciembre 2025)

### Estado de Servicios

| Servicio           | Estado     | Puerto |
| ------------------ | ---------- | ------ |
| API Gateway        | ✅ Healthy | 8080   |
| Payment Processing | ✅ Healthy | 8081   |
| Network Management | ✅ Healthy | 8082   |
| Account Balance    | ✅ Healthy | 8083   |
| Clearing Service   | ✅ Healthy | 8084   |
| Return Management  | ✅ Healthy | 8085   |
| Notification       | ✅ Healthy | 8086   |
| Error Mapping      | ✅ Healthy | 8087   |

### Transferencias Probadas

| TX  | Origen      | Destino     | Monto  | Estado        |
| --- | ----------- | ----------- | ------ | ------------- |
| 1   | PICHINCHA   | GUAYAQUIL   | $100   | ✅ Completada |
| 2   | GUAYAQUIL   | PACIFICO    | $250   | ✅ Completada |
| 3   | PACIFICO    | PRODUBANCO  | $750   | ✅ Completada |
| 4   | PRODUBANCO  | BOLIVARIANO | $1,500 | ✅ Completada |
| 5   | BOLIVARIANO | PICHINCHA   | $300   | ✅ Completada |

### Funcionalidades Verificadas

- ✅ Enrutamiento automático por BIN
- ✅ Gestión de saldos de prefondeo
- ✅ Idempotencia (transacciones duplicadas)
- ✅ Validaciones de entrada
- ✅ Métricas de rendimiento
- ✅ Health checks de todos los servicios

---

## 📄 Licencia

Este proyecto es parte del curso de Arquitectura de Software de la Universidad.

---

## 👥 Autores

- Equipo DIGICONECU - Universidad Técnica Particular de Loja

---

_Última actualización: Diciembre 2025_
