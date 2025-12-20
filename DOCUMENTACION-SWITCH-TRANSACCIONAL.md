# 🏦 Switch Transaccional - Documentación Completa

## 📋 Índice

1. [Resumen del Proyecto](#resumen-del-proyecto)
2. [Arquitectura de Microservicios](#arquitectura-de-microservicios)
3. [Trabajo Realizado](#trabajo-realizado)
4. [Configuración de Bases de Datos](#configuración-de-bases-de-datos)
5. [Endpoints por Servicio](#endpoints-por-servicio)
6. [Pruebas Ejecutadas](#pruebas-ejecutadas)
7. [Pendientes y Mejoras Futuras](#pendientes-y-mejoras-futuras)
8. [Instrucciones de Despliegue](#instrucciones-de-despliegue)

---

## 📖 Resumen del Proyecto

El **Switch Transaccional** es una arquitectura de microservicios diseñada para procesar transferencias interbancarias en Ecuador. Implementa el patrón **Database per Service** donde cada bounded context tiene su propia base de datos, sin foreign keys entre ellas.

### Principios de Diseño

- ✅ **Database per Service**: Cada contexto tiene su BD independiente
- ✅ **Comunicación REST**: Los servicios se comunican exclusivamente por HTTP
- ✅ **Stateless Services**: Notification y Error Mapping no persisten datos
- ✅ **Idempotencia**: El sistema previene transacciones duplicadas
- ✅ **Dockerización**: Todos los servicios orquestados con Docker Compose

---

## 🏗️ Arquitectura de Microservicios

```
╔═══════════════════════════════════════════════════════════════════════════╗
║                    SWITCH TRANSACCIONAL - ARQUITECTURA                     ║
╠═══════════════════════════════════════════════════════════════════════════╣
║                                                                            ║
║  ┌────────────────────────────────────────────────────────────────────┐   ║
║  │                         CLIENTE / API GATEWAY                       │   ║
║  └──────────────────────────────┬─────────────────────────────────────┘   ║
║                                 │                                          ║
║                                 ▼                                          ║
║  ┌────────────────────────────────────────────────────────────────────┐   ║
║  │              PAYMENT PROCESSING SERVICE (8081)                      │   ║
║  │              ══════════════════════════════════                      │   ║
║  │  • Orquestador principal de transacciones                           │   ║
║  │  • Manejo de idempotencia con cache                                 │   ║
║  │  • Estados: RECIBIDA → ENRUTADA → ESPERANDO → COMPLETADA/FALLIDA   │   ║
║  │  📦 BD: PaymentProcessingDB (Transacciones, IdempotenciaCache)      │   ║
║  └─────┬──────────────┬────────────────┬──────────────┬───────────────┘   ║
║        │              │                │              │                    ║
║        ▼              ▼                ▼              ▼                    ║
║  ┌──────────┐  ┌──────────┐    ┌──────────┐   ┌─────────────────┐         ║
║  │ NETWORK  │  │ ACCOUNT  │    │NOTIFIC.  │   │  ERROR MAPPING  │         ║
║  │MANAGEMENT│  │ BALANCE  │    │ SERVICE  │   │    SERVICE      │         ║
║  │  (8082)  │  │  (8083)  │    │  (8086)  │   │     (8087)      │         ║
║  ├──────────┤  ├──────────┤    ├──────────┤   ├─────────────────┤         ║
║  │• Routing │  │• Prefond.│    │• Webhooks│   │• Traducción     │         ║
║  │• BIN Mgmt│  │• Congelar│    │• 4 retry │   │  códigos error  │         ║
║  │• Bancos  │  │• Liberar │    │• Delays  │   │• ISO 20022      │         ║
║  │          │  │          │    │          │   │                 │         ║
║  │📦NetworkDB│  │📦BalanceDB│   │ STATELESS│   │   STATELESS     │         ║
║  └──────────┘  └────┬─────┘    └──────────┘   └─────────────────┘         ║
║                     │                                                      ║
║                     │ (consulta saldos)                                    ║
║                     ▼                                                      ║
║  ┌──────────────────────────────────────────────────────────────────┐     ║
║  │                                                                    │     ║
║  │  ┌─────────────────────┐         ┌─────────────────────┐         │     ║
║  │  │  CLEARING SERVICE   │         │  RETURN MANAGEMENT  │         │     ║
║  │  │       (8084)        │         │       (8085)        │         │     ║
║  │  ├─────────────────────┤         ├─────────────────────┤         │     ║
║  │  │ • Ciclos compensac. │         │ • Devoluciones      │         │     ║
║  │  │ • Netting diario    │         │ • Reversas          │         │     ║
║  │  │ • Posiciones netas  │         │ • Motivos ISO       │         │     ║
║  │  │                     │         │                     │         │     ║
║  │  │ 📦 ClearingDB       │         │ 📦 ReturnMgmtDB     │         │     ║
║  │  └─────────────────────┘         └─────────────────────┘         │     ║
║  │                                                                    │     ║
║  └──────────────────────────────────────────────────────────────────┘     ║
║                                                                            ║
╚═══════════════════════════════════════════════════════════════════════════╝
```

---

## ✅ Trabajo Realizado

### 1. Corrección de Errores de Compilación

| Archivo                            | Problema                                                    | Solución                                  |
| ---------------------------------- | ----------------------------------------------------------- | ----------------------------------------- |
| `RestClientConfig.java` (Clearing) | `JdkClientHttpRequestFactory.setConnectTimeout()` no existe | Cambió a `SimpleClientHttpRequestFactory` |
| `RestClientConfig.java` (Return)   | Mismo error                                                 | Mismo fix                                 |
| `VerificarSaldoResponse.java`      | Faltaba campo `codigoError`                                 | Agregado el campo                         |
| `ClearingCycleResponse.java`       | Faltaba campo `estado`                                      | Agregado el campo                         |
| `application.properties` (Payment) | Caracteres con encoding incorrecto                          | Corregido encoding UTF-8                  |

### 2. Dockerización Completa

- ✅ Creado `Dockerfile` para **Clearing Service** (multi-stage build)
- ✅ Creado `Dockerfile` para **Return Management Service** (multi-stage build)
- ✅ Actualizado `Dockerfile` de **Payment Processing** a multi-stage
- ✅ Creado `docker-compose.yml` unificado con todos los servicios
- ✅ Configuradas variables de entorno para conexiones entre servicios
- ✅ Health checks con `wget` (compatible con Alpine)

### 3. Endpoints Agregados

#### Payment Processing (8081)

```java
@GetMapping("/health")  // NUEVO - Health check
public ResponseEntity<Map<String, String>> health()
```

#### Network Management (8082)

- Rutas estandarizadas a `/api/v2/routing/*`
- Endpoints de gestión de BINs

#### Account Balance (8083)

- `POST /api/v2/balance/liberar` - Liberar fondos congelados
- `POST /api/v2/balance/recargar` - Recargar cuenta prefondeo
- `GET /api/v2/balance/saldo/{banco}` - Consultar saldo
- `GET /api/v2/balance/operaciones` - Listar operaciones

#### Clearing Service (8084)

- `GET /api/v2/clearing/ciclos` - Listar últimos ciclos
- `GET /api/v2/clearing/ultimo` - Obtener último ciclo
- `GET /api/v2/clearing/health` - Health check

#### Return Management (8085)

- `GET /api/v2/returns/original/{id}` - Buscar por TX original
- `GET /api/v2/returns/health` - Health check

### 4. Mejoras de Código

- ✅ Validaciones con `@Valid`, `@NotNull`, `@DecimalMin`
- ✅ Manejo de excepciones global (`GlobalExceptionHandler`)
- ✅ Limpieza automática de cache (`@Scheduled`)
- ✅ Expanded `error-mappings.yml` con más bancos y categorías

---

## 🗄️ Configuración de Bases de Datos

### ¿Por qué 5 bases de datos y no 6?

Según el esquema arquitectónico, solo **5 bounded contexts** requieren persistencia:

| #   | Servicio           | Base de Datos       | Puerto Host | Justificación                               |
| --- | ------------------ | ------------------- | ----------- | ------------------------------------------- |
| 1   | Payment Processing | PaymentProcessingDB | 5441        | Persiste transacciones e idempotencia       |
| 2   | Network Management | NetworkManagementDB | 5442        | Persiste bancos y reglas de enrutamiento    |
| 3   | Account Balance    | AccountBalanceDB    | 5443        | Persiste cuentas y operaciones de prefondeo |
| 4   | Clearing Service   | ClearingDB          | 5444        | Persiste ciclos de compensación             |
| 5   | Return Management  | ReturnManagementDB  | 5445        | Persiste devoluciones                       |

### Servicios STATELESS (sin BD)

| Servicio              | Puerto | Justificación                              |
| --------------------- | ------ | ------------------------------------------ |
| Notification Service  | 8086   | Solo envía webhooks, no necesita persistir |
| Error Mapping Service | 8087   | Lee mapeos de archivo YAML en memoria      |

**Nota:** En la versión inicial del `docker-compose.yml` se había incluido erróneamente una base de datos `postgres-notification`. Esto fue **corregido** eliminando dicha BD y el volumen asociado.

### Tablas por Base de Datos

```sql
-- PaymentProcessingDB
├── Transacciones (instructionId, endToEndId, estado, monto, etc.)
└── IdempotenciaCache (instructionId, respuestaJson, fechaRegistro)

-- NetworkManagementDB
├── Bancos (id, codigo, nombre, endpoint, estado)
└── Enrutamiento (id, binInicio, binFin, bancoId, prioridad)

-- AccountBalanceDB
├── CuentasPrefondeo (id, bancoCodigo, saldoDisponible, saldoCongelado)
└── OperacionesPrefondeo (id, cuentaId, tipoOperacion, monto, etc.)

-- ClearingDB
└── CiclosCompensacion (id, fechaCiclo, estado, posicionesNetas, etc.)

-- ReturnManagementDB
└── Devoluciones (id, transaccionOriginalId, motivo, estado, etc.)
```

---

## 🔌 Endpoints por Servicio

### Payment Processing (8081)

| Método | Endpoint                   | Descripción           |
| ------ | -------------------------- | --------------------- |
| POST   | `/api/v2/transfers`        | Crear transferencia   |
| GET    | `/api/v2/transfers/{id}`   | Consultar estado      |
| GET    | `/api/v2/transfers`        | Listar transferencias |
| GET    | `/api/v2/transfers/health` | Health check          |

### Network Management (8082)

| Método | Endpoint                                 | Descripción          |
| ------ | ---------------------------------------- | -------------------- |
| POST   | `/api/v2/routing/resolve`                | Resolver BIN → Banco |
| GET    | `/api/v2/routing/bancos`                 | Listar bancos        |
| POST   | `/api/v2/routing/bancos`                 | Crear banco          |
| GET    | `/api/v2/routing/bancos/{codigo}`        | Obtener banco        |
| PUT    | `/api/v2/routing/bancos/{codigo}/estado` | Cambiar estado       |

### Account Balance (8083)

| Método | Endpoint                        | Descripción                |
| ------ | ------------------------------- | -------------------------- |
| POST   | `/api/v2/balance/verificar`     | Verificar saldo disponible |
| POST   | `/api/v2/balance/congelar`      | Congelar fondos            |
| POST   | `/api/v2/balance/completar`     | Completar operación        |
| POST   | `/api/v2/balance/liberar`       | Liberar fondos             |
| POST   | `/api/v2/balance/recargar`      | Recargar cuenta            |
| GET    | `/api/v2/balance/saldo/{banco}` | Consultar saldo            |
| GET    | `/api/v2/balance/operaciones`   | Listar operaciones         |

### Clearing Service (8084)

| Método | Endpoint                  | Descripción             |
| ------ | ------------------------- | ----------------------- |
| POST   | `/api/v2/clearing/ciclo`  | Ejecutar ciclo clearing |
| GET    | `/api/v2/clearing/ciclos` | Listar últimos ciclos   |
| GET    | `/api/v2/clearing/ultimo` | Último ciclo            |
| GET    | `/api/v2/clearing/health` | Health check            |

### Return Management (8085)

| Método | Endpoint                        | Descripción            |
| ------ | ------------------------------- | ---------------------- |
| POST   | `/api/v2/returns`               | Solicitar devolución   |
| GET    | `/api/v2/returns/{id}`          | Consultar devolución   |
| GET    | `/api/v2/returns/original/{id}` | Buscar por TX original |
| GET    | `/api/v2/returns/health`        | Health check           |

### Notification Service (8086)

| Método | Endpoint                      | Descripción         |
| ------ | ----------------------------- | ------------------- |
| POST   | `/api/v2/notification/enviar` | Enviar notificación |
| GET    | `/api/v2/notification/health` | Health check        |

### Error Mapping Service (8087)

| Método | Endpoint                            | Descripción            |
| ------ | ----------------------------------- | ---------------------- |
| POST   | `/api/v2/error-mapping/traducir`    | Traducir código error  |
| GET    | `/api/v2/error-mapping/codigos-iso` | Listar códigos ISO     |
| POST   | `/api/v2/error-mapping/reload`      | Recargar configuración |

---

## 🧪 Pruebas Ejecutadas

### Resultados de Pruebas (20 Dic 2025)

| #   | Prueba                            | Resultado | Observación                 |
| --- | --------------------------------- | --------- | --------------------------- |
| 1   | Crear transferencia TX-TEST-001   | ✅ PASS   | Estado: COMPLETADA          |
| 2   | Consultar estado TX-TEST-001      | ✅ PASS   | Datos correctos             |
| 3   | Idempotencia (TX duplicada)       | ✅ PASS   | No se duplica               |
| 4   | Segunda transferencia TX-TEST-002 | ✅ PASS   | Estado: COMPLETADA          |
| 5   | Listar transferencias             | ✅ PASS   | 2 TXs en lista              |
| 6   | Verificar saldo PICHINCHA         | ✅ PASS   | $500,000 disponibles        |
| 7   | Congelar $500                     | ✅ PASS   | Saldo congelado: $500       |
| 8   | Verificar saldo post-congelar     | ✅ PASS   | Disponible: $499,500        |
| 9   | Traducir error PICHINCHA/001      | ✅ PASS   | Retorna UNKNOWN             |
| 10  | Listar códigos ISO                | ✅ PASS   | MS03, DUPL, AM04, etc.      |
| 11  | Health check Clearing             | ✅ PASS   | Status: UP                  |
| 12  | Health check Return Mgmt          | ✅ PASS   | Status: UP                  |
| 13  | Health check Notification         | ✅ PASS   | OK                          |
| 14  | Ciclo Clearing                    | ⚠️ ERROR  | Error interno (integración) |

### Estado de Contenedores Docker

```
account-balance:    Up (healthy)     - 8083
clearing-service:   Up (healthy)     - 8084
error-mapping:      Up               - 8087
network-management: Up (healthy)     - 8082
notification:       Up               - 8086
payment-processing: Up               - 8081
return-management:  Up (healthy)     - 8085
postgres-payment:   Up (healthy)     - 5441
postgres-network:   Up (healthy)     - 5442
postgres-balance:   Up (healthy)     - 5443
postgres-clearing:  Up (healthy)     - 5444
postgres-return:    Up (healthy)     - 5445
```

---

## 📝 Pendientes y Mejoras Futuras

### 🔴 Alta Prioridad

| #   | Tarea                             | Servicio           | Descripción                                                  |
| --- | --------------------------------- | ------------------ | ------------------------------------------------------------ |
| 1   | Comunicación real entre servicios | Payment Processing | Actualmente simula llamadas a Network, Balance, Notification |
| 2   | Integración Clearing ↔ Payment    | Clearing Service   | RestClient falla al consultar transacciones                  |
| 3   | Generación archivos ISO 20022     | Clearing Service   | Pendiente generar XML de liquidación                         |
| 4   | Crear transacción inversa         | Return Management  | Pendiente crear TX reversa al aprobar devolución             |

### 🟡 Media Prioridad

| #   | Tarea                         | Descripción                             |
| --- | ----------------------------- | --------------------------------------- |
| 5   | Tests de integración          | Crear tests E2E con TestContainers      |
| 6   | Documentación OpenAPI/Swagger | Agregar springdoc-openapi               |
| 7   | Métricas Prometheus           | Agregar actuator + micrometer           |
| 8   | Trazabilidad distribuida      | Agregar Sleuth/Zipkin                   |
| 9   | Circuit Breaker               | Agregar Resilience4j para llamadas HTTP |

### 🟢 Baja Prioridad

| #   | Tarea                      | Descripción                                 |
| --- | -------------------------- | ------------------------------------------- |
| 10  | API Gateway                | Agregar Spring Cloud Gateway                |
| 11  | Service Discovery          | Agregar Eureka o Consul                     |
| 12  | Configuración centralizada | Agregar Spring Cloud Config                 |
| 13  | Logs centralizados         | ELK Stack (Elasticsearch, Logstash, Kibana) |

---

## 🚀 Instrucciones de Despliegue

### Prerrequisitos

- Docker Desktop instalado
- Al menos 8GB de RAM disponible
- Puertos libres: 8081-8087, 5441-5445

### Comandos

```bash
# Desde la carpeta raíz Switch-Transaccional

# 1. Levantar todos los servicios
docker-compose up --build -d

# 2. Ver logs en tiempo real
docker-compose logs -f

# 3. Ver estado de contenedores
docker-compose ps

# 4. Detener todos los servicios
docker-compose down

# 5. Detener y eliminar volúmenes (reset completo)
docker-compose down -v
```

### Health Checks

```bash
# Verificar todos los servicios
curl http://localhost:8081/api/v2/transfers/health
curl http://localhost:8082/api/v2/routing/bancos
curl http://localhost:8083/api/v2/balance/saldo/PICHINCHA
curl http://localhost:8084/api/v2/clearing/health
curl http://localhost:8085/api/v2/returns/health
curl http://localhost:8086/api/v2/notification/health
curl http://localhost:8087/api/v2/error-mapping/codigos-iso
```

### Ejemplo de Flujo Completo

```bash
# 1. Crear transferencia
curl -X POST http://localhost:8081/api/v2/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "instructionId": "TX-DEMO-001",
    "endToEndId": "E2E-DEMO-001",
    "bancoOrigen": "PICHINCHA",
    "cuentaOrigen": "2200123456",
    "cuentaDestino": "1700987654",
    "monto": 100.00,
    "moneda": "USD",
    "concepto": "Pago de servicios"
  }'

# 2. Consultar estado
curl http://localhost:8081/api/v2/transfers/TX-DEMO-001

# 3. Verificar saldo
curl http://localhost:8083/api/v2/balance/saldo/PICHINCHA
```

---

## 📊 Resumen Final

| Métrica                 | Valor                    |
| ----------------------- | ------------------------ |
| Total Microservicios    | 7                        |
| Servicios con BD        | 5                        |
| Servicios Stateless     | 2                        |
| Contenedores Docker     | 12 (7 apps + 5 postgres) |
| Endpoints implementados | ~35                      |
| Pruebas ejecutadas      | 14                       |
| Pruebas exitosas        | 13 (93%)                 |

---

**Fecha de documentación:** 20 de Diciembre de 2025  
**Versión:** 1.0.0
