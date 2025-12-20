# 🏦 Switch Transaccional - Arquitectura de Microservicios

## 📋 Estado de los Microservicios

| #   | Servicio           | Puerto | Estado       | Base de Datos       |
| --- | ------------------ | ------ | ------------ | ------------------- |
| 1   | Payment Processing | 8081   | ✅ Operativo | PaymentProcessingDB |
| 2   | Network Management | 8082   | ✅ Operativo | NetworkManagementDB |
| 3   | Account Balance    | 8083   | ✅ Operativo | AccountBalanceDB    |
| 4   | Clearing Service   | 8084   | ✅ Operativo | ClearingDB          |
| 5   | Return Management  | 8085   | ✅ Operativo | ReturnManagementDB  |
| 6   | Notification       | 8086   | ✅ Operativo | NotificationDB      |
| 7   | Error Mapping      | 8087   | ✅ Operativo | N/A (en memoria)    |

---

## 🔄 Flujo de Comunicación entre Servicios

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SWITCH TRANSACCIONAL                                │
│                     Arquitectura de Microservicios                           │
└─────────────────────────────────────────────────────────────────────────────┘

                              ┌──────────────────┐
                              │    Cliente/API   │
                              │    Gateway       │
                              └────────┬─────────┘
                                       │
                                       ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                      PAYMENT PROCESSING (8081)                                │
│  ══════════════════════════════════════════════                              │
│  • Orquestador principal de transacciones                                    │
│  • Manejo de idempotencia                                                    │
│  • Estados: RECIBIDA → ENRUTADA → ESPERANDO_RESPUESTA → COMPLETADA/FALLIDA  │
└─────┬──────────────┬───────────────────┬──────────────────┬─────────────────┘
      │              │                   │                  │
      ▼              ▼                   ▼                  ▼
┌─────────────┐ ┌─────────────┐   ┌─────────────┐   ┌─────────────────┐
│   NETWORK   │ │   ACCOUNT   │   │NOTIFICATION │   │  ERROR MAPPING  │
│ MANAGEMENT  │ │   BALANCE   │   │  SERVICE    │   │    SERVICE      │
│   (8082)    │ │   (8083)    │   │   (8086)    │   │     (8087)      │
├─────────────┤ ├─────────────┤   ├─────────────┤   ├─────────────────┤
│ • Routing   │ │ • Prefondeo │   │ • Webhooks  │   │ • Traducción    │
│ • BIN Mgmt  │ │ • Congelar  │   │ • 4 reinten │   │   códigos error │
│ • Directorio│ │ • Liberar   │   │ • Delays    │   │ • ISO 20022     │
│   bancos    │ │ • Recargar  │   │   exponenc. │   │ • Multi-banco   │
└─────────────┘ └──────┬──────┘   └─────────────┘   └─────────────────┘
                       │
                       │ (saldo disponible)
                       ▼
            ┌───────────────────────────────────────────────────┐
            │                                                   │
┌───────────┴────────────┐           ┌───────────────────────┐ │
│   CLEARING SERVICE     │           │   RETURN MANAGEMENT   │◄┘
│       (8084)           │           │       (8085)          │
├────────────────────────┤           ├───────────────────────┤
│ • Ciclos compensación  │           │ • Devoluciones        │
│ • Netting diario       │           │ • Reversas            │
│ • Posiciones netas     │           │ • Motivos ISO         │
│ • ISO 20022 (pendiente)│           │ • Transacción inversa │
└────────────────────────┘           └───────────────────────┘
```

---

## 🔗 Endpoints Principales por Servicio

### 1. Payment Processing (8081)

```
POST /api/v2/transfers         - Crear transferencia
GET  /api/v2/transfers/{id}    - Consultar estado
GET  /api/v2/transfers         - Listar transferencias (con filtros)
GET  /api/v2/transfers/health  - Health check
```

### 2. Network Management (8082)

```
POST /api/v2/routing/resolve           - Resolver BIN → Banco
GET  /api/v2/routing/banks             - Listar bancos
POST /api/v2/routing/banks             - Crear banco
GET  /api/v2/routing/banks/{codigo}    - Obtener banco por código
POST /api/v2/routing/bins              - Crear rango BIN
GET  /api/v2/routing/bins              - Listar rangos BIN
GET  /api/v2/routing/health            - Health check
```

### 3. Account Balance (8083)

```
POST /api/v2/balance/verificar    - Verificar saldo disponible
POST /api/v2/balance/congelar     - Congelar fondos
POST /api/v2/balance/completar    - Completar operación
POST /api/v2/balance/liberar      - Liberar fondos congelados
POST /api/v2/balance/recargar     - Recargar cuenta prefondeo
GET  /api/v2/balance/saldo/{banco}- Consultar saldo
GET  /api/v2/balance/operaciones  - Listar operaciones
GET  /api/v2/balance/health       - Health check
```

### 4. Clearing Service (8084)

```
POST /api/v2/clearing/ciclo       - Ejecutar ciclo de clearing
GET  /api/v2/clearing/ciclos      - Listar últimos ciclos
GET  /api/v2/clearing/ultimo      - Último ciclo
GET  /api/v2/clearing/health      - Health check
```

### 5. Return Management (8085)

```
POST /api/v2/returns              - Solicitar devolución
GET  /api/v2/returns/{id}         - Consultar devolución
GET  /api/v2/returns/original/{id}- Buscar por TX original
GET  /api/v2/returns/health       - Health check
```

### 6. Notification Service (8086)

```
POST /api/v2/notification/enviar  - Enviar notificación
GET  /api/v2/notification/{id}    - Consultar estado
GET  /api/v2/notification/health  - Health check
```

### 7. Error Mapping Service (8087)

```
GET  /api/v2/error-mapping/{banco}/{codigo}  - Traducir código error
GET  /api/v2/error-mapping/health            - Health check
```

---

## 🚀 Levantar todos los servicios

### Prerrequisitos

- Docker Desktop instalado y corriendo
- Al menos 8GB de RAM disponible
- Puertos 8081-8087 y 5441-5446 libres

### Comandos

```bash
# Desde la carpeta raíz (Switch-Transaccional)

# 1. Levantar todos los servicios
docker-compose up --build

# 2. Levantar en background
docker-compose up --build -d

# 3. Ver logs de todos los servicios
docker-compose logs -f

# 4. Ver logs de un servicio específico
docker-compose logs -f payment-processing

# 5. Detener todos los servicios
docker-compose down

# 6. Detener y eliminar volúmenes (reset completo)
docker-compose down -v

# 7. Verificar estado de contenedores
docker-compose ps
```

### Verificar que todo está corriendo

```bash
# Health checks de todos los servicios
curl http://localhost:8081/api/v2/transfers/health
curl http://localhost:8082/api/v2/routing/health
curl http://localhost:8083/api/v2/balance/health
curl http://localhost:8084/api/v2/clearing/health
curl http://localhost:8085/api/v2/returns/health
curl http://localhost:8086/api/v2/notification/health
curl http://localhost:8087/api/v2/error-mapping/health
```

---

## 📊 Puertos Expuestos

| Servicio           | App Port | DB Port (Host) |
| ------------------ | -------- | -------------- |
| Payment Processing | 8081     | 5441           |
| Network Management | 8082     | 5442           |
| Account Balance    | 8083     | 5443           |
| Clearing Service   | 8084     | 5444           |
| Return Management  | 8085     | 5445           |
| Notification       | 8086     | 5446           |
| Error Mapping      | 8087     | N/A            |

---

## 🧪 Ejemplo de Flujo Completo

### 1. Crear una transferencia

```bash
curl -X POST http://localhost:8081/api/v2/transfers \
  -H "Content-Type: application/json" \
  -d '{
    "instructionId": "TX-2024-001",
    "endToEndId": "E2E-001",
    "bancoOrigen": "BANCO_PICHINCHA",
    "cuentaOrigen": "2200123456",
    "cuentaDestino": "1700987654",
    "monto": 100.00,
    "moneda": "USD",
    "concepto": "Pago de servicios"
  }'
```

### 2. Consultar estado

```bash
curl http://localhost:8081/api/v2/transfers/TX-2024-001
```

### 3. Resolver routing

```bash
curl -X POST http://localhost:8082/api/v2/routing/resolve \
  -H "Content-Type: application/json" \
  -d '{
    "bin": "170098"
  }'
```

### 4. Verificar saldo

```bash
curl -X POST http://localhost:8083/api/v2/balance/verificar \
  -H "Content-Type: application/json" \
  -d '{
    "codigoBanco": "BANCO_PICHINCHA",
    "montoRequerido": 100.00
  }'
```

---

## 🛠️ Troubleshooting

### Error: Puerto en uso

```bash
# Verificar qué proceso usa el puerto
netstat -ano | findstr :8081

# Liberar puerto o cambiar en docker-compose.yml
```

### Error: Base de datos no conecta

```bash
# Verificar que postgres está corriendo
docker-compose ps postgres-payment

# Ver logs de postgres
docker-compose logs postgres-payment
```

### Error: Servicio no arranca

```bash
# Ver logs del servicio
docker-compose logs payment-processing

# Reconstruir imagen
docker-compose build --no-cache payment-processing
```

---

## 📁 Estructura del Proyecto

```
Switch-Transaccional/
├── docker-compose.yml                    # ← Orquestación unificada
├── README-SERVICIOS.md                   # ← Este archivo
│
├── DIGICONECU-feature-MicroPaymentProcessing/
├── DIGICONECU-network-management-service/
├── DIGICONECU-feature-MicroAccountBalance/
├── DIGICONECU-feature-MicroClearingService/
├── DIGICONECU-feature-MicroReturnManagmentService/
├── DIGICONECU-notificacion-service/
└── DIGICONECU-error-mapping-service/
```

---

## ✅ Checklist de Implementación

- [x] Health endpoints en todos los servicios
- [x] Validación de requests
- [x] Manejo de excepciones global
- [x] Idempotencia en Payment Processing
- [x] Retry mechanism en Notification (4 reintentos)
- [x] Mapeo de errores multi-banco
- [x] Docker Compose unificado
- [x] Configuración por variables de entorno
- [ ] Tests de integración
- [ ] Documentación OpenAPI/Swagger
- [ ] Métricas con Prometheus
- [ ] Trazabilidad con Sleuth/Zipkin
