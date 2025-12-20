# Account Balance Service

Microservicio de gestión de saldos de cuentas de prefondeo para el sistema transaccional Switch.

## 📋 Descripción

Este microservicio proporciona una API REST para:
- **Verificar saldo** disponible de cuentas bancarias
- **Congelar fondos** antes de ejecutar una transferencia
- **Completar transferencias** entre bancos participantes

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────────┐
│                    Account Balance Service                       │
├─────────────────────────────────────────────────────────────────┤
│  interfaces/rest                                                 │
│    └── BalanceController.java         (REST Endpoints)           │
├─────────────────────────────────────────────────────────────────┤
│  application/service                                             │
│    └── BalanceService.java            (Business Logic)           │
├─────────────────────────────────────────────────────────────────┤
│  application/dto                                                 │
│    ├── VerificarSaldoRequest/Response                            │
│    ├── CongelarFondosRequest/Response                            │
│    └── CompletarTransferenciaRequest/Response                    │
├─────────────────────────────────────────────────────────────────┤
│  domain/entity                                                   │
│    ├── CuentaPrefondeo.java          (Cuentas bancarias)         │
│    └── OperacionPrefondeo.java       (Historial operaciones)     │
├─────────────────────────────────────────────────────────────────┤
│  domain/repository                                               │
│    ├── CuentaPrefondeoRepository.java                            │
│    └── OperacionPrefondeoRepository.java                         │
└─────────────────────────────────────────────────────────────────┘
```

## 🛠️ Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| Framework | Spring Boot 3.4.1 |
| JDK | Eclipse Temurin 17 |
| Base de Datos | PostgreSQL 15 |
| Build | Maven |
| Contenedores | Docker & Docker Compose |

## 🚀 Instalación y Ejecución

### Requisitos Previos
- Docker Desktop instalado y ejecutándose
- Puertos 8083 y 5433 disponibles

### Ejecutar con Docker Compose

```bash
# Iniciar los servicios
docker-compose up -d

# Ver logs
docker logs account-balance-service -f

# Detener los servicios
docker-compose down

# Detener y eliminar volúmenes (borra datos)
docker-compose down -v
```

### Servicios Desplegados

| Servicio | Contenedor | Puerto |
|----------|------------|--------|
| API REST | account-balance-service | 8083 |
| PostgreSQL | account-balance-db | 5433 |

## 📡 API Endpoints

Base URL: `http://localhost:8083/api/v2/balance`

---

### 1. Verificar Saldo

Verifica si una cuenta tiene saldo suficiente para un monto específico.

**Endpoint:** `POST /verificar`

**Request Body:**
```json
{
  "bancoCodigo": "PICHINCHA",
  "monto": 1000.00
}
```

**Response (200 OK):**
```json
{
  "aprobado": true,
  "saldoDisponible": 500000.00,
  "mensaje": "Saldo suficiente"
}
```

**Campos de Respuesta:**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| aprobado | boolean | `true` si hay saldo suficiente |
| saldoDisponible | decimal | Saldo actual disponible |
| mensaje | string | Descripción del resultado |

---

### 2. Congelar Fondos

Congela un monto específico para una futura transferencia. El monto congelado se resta del saldo disponible.

**Endpoint:** `POST /congelar`

**Request Body:**
```json
{
  "bancoCodigo": "PICHINCHA",
  "monto": 10000.00,
  "instructionId": "11111111-1111-1111-1111-111111111111"
}
```

**Response (200 OK):**
```json
{
  "exito": true,
  "saldoCongelado": 10000.00,
  "mensaje": "Fondos congelados exitosamente"
}
```

**Campos de Respuesta:**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| exito | boolean | `true` si la operación fue exitosa |
| saldoCongelado | decimal | Total de fondos congelados |
| mensaje | string | Descripción del resultado |

---

### 3. Completar Transferencia

Ejecuta una transferencia del saldo congelado del banco origen hacia el saldo disponible del banco destino.

**Endpoint:** `POST /completar`

**Request Body:**
```json
{
  "bancoOrigen": "PICHINCHA",
  "bancoDestino": "GUAYAQUIL",
  "monto": 5000.00,
  "instructionId": "11111111-1111-1111-1111-111111111111"
}
```

**Response (200 OK):**
```json
{
  "exito": true,
  "mensaje": "Transferencia completada"
}
```

**Campos de Respuesta:**
| Campo | Tipo | Descripción |
|-------|------|-------------|
| exito | boolean | `true` si la transferencia fue exitosa |
| mensaje | string | Descripción del resultado |

---

## 🏦 Bancos Disponibles (Datos Iniciales)

| Código | Saldo Inicial | Moneda |
|--------|---------------|--------|
| PICHINCHA | $500,000.00 | USD |
| GUAYAQUIL | $500,000.00 | USD |
| PACIFICO | $500,000.00 | USD |
| PRODUBANCO | $500,000.00 | USD |
| BOLIVARIANO | $500,000.00 | USD |

## 📊 Modelo de Datos

### Tabla: cuentasprefondeo

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | Identificador único |
| bancocodigo | VARCHAR(20) | Código del banco (UNIQUE) |
| saldodisponible | DECIMAL(18,2) | Saldo disponible |
| saldocongelado | DECIMAL(18,2) | Saldo congelado |
| moneda | VARCHAR(3) | Código de moneda (USD) |
| fechaactualizacion | TIMESTAMP | Última actualización |

### Tabla: operacionesprefondeo

| Columna | Tipo | Descripción |
|---------|------|-------------|
| id | UUID | Identificador único |
| cuentaid | UUID | FK a cuentasprefondeo |
| instructionid | UUID | ID de instrucción externa |
| tipooperacion | VARCHAR(20) | Tipo de operación |
| monto | DECIMAL(18,2) | Monto de la operación |
| saldoanterior | DECIMAL(18,2) | Saldo antes de operación |
| saldoposterior | DECIMAL(18,2) | Saldo después de operación |
| fechaoperacion | TIMESTAMP | Fecha de la operación |

### Tipos de Operación

- `Congelar` - Congelar fondos
- `Descongelar` - Liberar fondos congelados
- `Debitar` - Débito de cuenta origen
- `Acreditar` - Crédito a cuenta destino
- `Recargar` - Recarga de saldo
- `Liberar` - Liberación de fondos

## 🔧 Configuración

### Variables de Entorno

| Variable | Valor por defecto | Descripción |
|----------|-------------------|-------------|
| DB_HOST | localhost | Host de PostgreSQL |
| DB_PORT | 5432 | Puerto de PostgreSQL |
| DB_NAME | AccountBalanceDB | Nombre de la base de datos |
| DB_USER | postgres | Usuario de BD |
| DB_PASSWORD | postgres | Contraseña de BD |

### Puertos

| Servicio | Puerto Interno | Puerto Externo |
|----------|----------------|----------------|
| API | 8083 | 8083 |
| PostgreSQL | 5432 | 5433 |

## 📁 Estructura del Proyecto

```
account-balance-service/
├── src/main/java/com/switchpay/transaccional/accountbalance/
│   ├── AccountBalanceServiceApplication.java
│   ├── application/
│   │   ├── dto/
│   │   │   ├── CompletarTransferenciaRequest.java
│   │   │   ├── CompletarTransferenciaResponse.java
│   │   │   ├── CongelarFondosRequest.java
│   │   │   ├── CongelarFondosResponse.java
│   │   │   ├── VerificarSaldoRequest.java
│   │   │   └── VerificarSaldoResponse.java
│   │   └── service/
│   │       └── BalanceService.java
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── CuentaPrefondeo.java
│   │   │   └── OperacionPrefondeo.java
│   │   ├── enums/
│   │   │   └── TipoOperacion.java
│   │   └── repository/
│   │       ├── CuentaPrefondeoRepository.java
│   │       └── OperacionPrefondeoRepository.java
│   └── interfaces/
│       └── rest/
│           └── BalanceController.java
├── src/main/resources/
│   ├── application.yml
│   └── db/
│       └── init.sql
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 🧪 Ejemplos de Prueba (PowerShell)

```powershell
# 1. Verificar saldo
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8083/api/v2/balance/verificar" `
  -ContentType "application/json" `
  -Body '{"bancoCodigo": "PICHINCHA", "monto": 1000.00}'

# 2. Congelar fondos
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8083/api/v2/balance/congelar" `
  -ContentType "application/json" `
  -Body '{"bancoCodigo": "PICHINCHA", "monto": 10000.00, "instructionId": "11111111-1111-1111-1111-111111111111"}'

# 3. Completar transferencia
Invoke-RestMethod -Method Post `
  -Uri "http://localhost:8083/api/v2/balance/completar" `
  -ContentType "application/json" `
  -Body '{"bancoOrigen": "PICHINCHA", "bancoDestino": "GUAYAQUIL", "monto": 5000.00, "instructionId": "11111111-1111-1111-1111-111111111111"}'
```

## 🧪 Ejemplos de Prueba (cURL / Bash)

```bash
# 1. Verificar saldo
curl -X POST http://localhost:8083/api/v2/balance/verificar \
  -H "Content-Type: application/json" \
  -d '{"bancoCodigo": "PICHINCHA", "monto": 1000.00}'

# 2. Congelar fondos
curl -X POST http://localhost:8083/api/v2/balance/congelar \
  -H "Content-Type: application/json" \
  -d '{"bancoCodigo": "PICHINCHA", "monto": 10000.00, "instructionId": "11111111-1111-1111-1111-111111111111"}'

# 3. Completar transferencia
curl -X POST http://localhost:8083/api/v2/balance/completar \
  -H "Content-Type: application/json" \
  -d '{"bancoOrigen": "PICHINCHA", "bancoDestino": "GUAYAQUIL", "monto": 5000.00, "instructionId": "11111111-1111-1111-1111-111111111111"}'
```

## 🔍 Health Check

```bash
curl http://localhost:8083/actuator/health
```

**Respuesta esperada:**
```json
{"status": "UP"}
```

---

**Desarrollado para:** Sistema Transaccional SwitchPay  
**Versión:** 1.0.0  
**Puerto:** 8083
