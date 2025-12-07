# GUÍA RÁPIDA DE INICIO - SWITCH TRANSACCIONAL

## 🚀 SETUP RÁPIDO

### 1. Requisitos previos

- Java 21 instalado
- PostgreSQL instalado (puerto 5433)
- Redis instalado o Docker
- Maven (incluido en el proyecto con mvnw)

### 2. Configurar PostgreSQL

```bash
# Crear la base de datos
createdb -U postgres -p 5433 switch_db

# Ejecutar el script SQL
psql -U postgres -p 5433 -d switch_db -f init-db.sql
```

### 3. Iniciar Redis

```bash
# Opción 1: Docker
docker run -d -p 6379:6379 --name redis-switch redis:latest

# Opción 2: Servicio local
redis-server
```

### 4. Compilar y ejecutar

```bash
# Windows
.\mvnw.cmd clean install
.\mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw clean install
./mvnw spring-boot:run
```

### 5. Verificar que funciona

```bash
curl http://localhost:8083/actuator/health
```

## 📋 DIVISIÓN DE TAREAS

### PERSONA 1 - Implementar:

- [ ] `TransaccionController.java` - Método `recibirTransaccion()`
- [ ] `TransaccionController.java` - Método `consultarTransaccion()`
- [ ] `TransaccionController.java` - Método `consultarPorEndToEnd()`
- [ ] `GestorEstadosServiceImpl.java` - Método `actualizarEstado()`
- [ ] `GestorEstadosServiceImpl.java` - Método `obtenerEstado()`

### PERSONA 2 (TÚ) - Implementar:

- [ ] `IdempotenciaServiceImpl.java` - Método `obtenerRespuestaPrevia()`
- [ ] `IdempotenciaServiceImpl.java` - Método `guardarRespuesta()`
- [ ] `IdempotenciaServiceImpl.java` - Método `existeTransaccion()`
- [ ] `EnrutamientoServiceImpl.java` - Método `procesarTransaccion()`
- [ ] `IdempotenciaFilter.java` - Completar lógica del filtro

## 🧪 PROBAR EL MICROSERVICIO

```bash
# Test de transacción
curl -X POST http://localhost:8083/api/transacciones \
  -H "Content-Type: application/json" \
  -d '{
    "Transaccion": {
      "EndToEnd": "E2E-DEMO-001",
      "IdBancoOrigen": 1,
      "IdBancoDestino": 2,
      "CuentaOrigen": "1234567890",
      "CuentaDestino": "0987654321",
      "Monto": 100.00,
      "Mensaje": "Prueba de transacción",
      "EstadoActual": "Enviado",
      "FechaCreacion": "2025-12-06T15:00:00Z"
    }
  }'

# Consultar por ID
curl http://localhost:8083/api/transacciones/1

# Consultar por EndToEnd
curl http://localhost:8083/api/transacciones/endtoend/E2E-DEMO-001
```

## 📞 CONTACTO

Si tienes dudas, coordina con tu compañero de desarrollo.

¡Éxito! 🎯
