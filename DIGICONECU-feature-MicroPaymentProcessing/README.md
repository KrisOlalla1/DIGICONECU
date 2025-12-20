# 💳 Payment Processing Service (Core Bancario)

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-Ready-blue)

Este microservicio es el **corazón transaccional** del Switch de Pagos "Hermes". Es responsable de orquestar el flujo de transferencias entre bancos, garantizando la consistencia, la idempotencia y la persistencia de las transacciones.

## 🏛️ Contexto Arquitectónico

Dentro de la arquitectura de microservicios del Switch Transaccional, este servicio cumple el rol de **CORE (Criticidad 5/5)**.

| Característica | Detalle |
| :--- | :--- |
| **Bounded Context** | Procesamiento de Pagos |
| **Puerto** | `8081` |
| **Base de Datos** | `PaymentProcessingDB` (PostgreSQL) |
| **Responsabilidad** | Orquestación, validación y persistencia de pagos. |

---

## 🚀 Funcionalidades Principales

1.  **Recepción de Transferencias:** Punto de entrada para las instrucciones de pago (PACS.008).
2.  **Control de Idempotencia:** Evita que una misma transacción se procese dos veces utilizando una caché de respuestas en base de datos.
3.  **Máquina de Estados:** Gestiona el ciclo de vida de la transacción (`RECIBIDA` -> `ENRUTADA` -> `COMPLETADA`).
4.  **Simulación de Orquestación (MVP):** Actualmente simula la comunicación exitosa con los servicios de *Network Management* y *Account Balance* para permitir pruebas aisladas.

---

## 🛠️ Tecnologías

* **Lenguaje:** Java 21 (Eclipse Temurin)
* **Framework:** Spring Boot 3 (Web, Data JPA, Validation)
* **Persistencia:** PostgreSQL
* **Utilidades:** Lombok, Docker
* **Arquitectura:** Hexagonal / Capas

---

## ⚙️ Configuración y Ejecución

### Prerrequisitos
* Tener **PostgreSQL** corriendo en el puerto `5432`.
* Crear una base de datos vacía llamada `PaymentProcessingDB` (o dejar que Spring la cree si tienes permisos).

### 1. Ejecución Local (Maven)
```bash
# Descargar dependencias y ejecutar
mvn spring-boot:run
```
### 2. Ejecución con Docker
```bash
# Construir la imagen Docker
docker build -t payment-processing-service .   
# Ejecutar el contenedor
docker run -d -p 8081:8081 --name payment-processing-service payment-processing-service
```
## 🧪 Guía de Pruebas (Endpoints & JSON)

El servicio expone una API REST en el puerto **8081**. A continuación se detallan los casos de prueba para validar el funcionamiento.

### 1️⃣ Iniciar una Transferencia (Happy Path)
Envía una instrucción de pago desde un Banco A hacia un Banco B.

* **Método:** `POST`
* **Endpoint:** `/api/v2/transfers`

**Body (JSON):**
```json
{
  "instructionId": "TX-REQ-2025-001",
  "endToEndId": "FACTURA-001",
  "bancoOrigen": "PICHINCHA",
  "cuentaOrigen": "1000001234",
  "cuentaDestino": "2500005678",
  "monto": 1500.00,
  "moneda": "USD",
  "concepto": "Pago de servicios"
}
```
**Respuesta Esperada:**
```json
{
  "success": true,
  "data": {
    "instructionId": "TX-REQ-2025-001",
    "estado": "Completada",
    "bancoDestino": "GUAYAQUIL"
  }
}
```

### 2️⃣ Prueba de Idempotencia (Seguridad)
Intenta enviar **exactamente el mismo JSON** del paso anterior. El sistema no debe crear un nuevo registro, sino devolver la respuesta exitosa original para evitar duplicar el cobro.

* **Acción:** Volver a ejecutar el `POST` con `instructionId`: "TX-REQ-2025-001".
* **Respuesta Esperada:** `200 OK` (Mismo resultado, sin error 500 de llave duplicada).
* **Log en Consola:** `INFO: Intento duplicado para TX: TX-REQ-2025-001`.

### 3️⃣ Consultar Estado (Tracking)
Verifica que la transacción se haya persistido correctamente en la base de datos PostgreSQL.

* **Método:** `GET`
* **Endpoint:** `/api/v2/transfers/TX-REQ-2025-001`

**Respuesta Esperada:**
```json
{
  "success": true,
  "data": {
    "instructionId": "TX-REQ-2025-001",
    "estado": "COMPLETADA",
    "monto": 1500.00,
    "bancoOrigen": "PICHINCHA",
    "fechaCreacion": "2025-12-20T..."
  }
}
```
---

## 📂 Estructura del Proyecto

El código sigue una arquitectura por capas estándar en Spring Boot para asegurar la separación de responsabilidades:

```text
src/main/java/com/ecusol/payment
├── controller   # PaymentController (Exposición REST - API v2)
├── service      # PaymentService (Lógica de negocio, validaciones y mocks)
├── model        # Entidades JPA (Mapeo a tablas Transacciones e Idempotencia)
├── repository   # Interfaces JPA (Acceso a base de datos PostgreSQL)
└── dto          # Objetos de Transferencia (Request/Response y Validaciones)
```