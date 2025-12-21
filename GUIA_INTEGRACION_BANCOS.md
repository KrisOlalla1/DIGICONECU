# 🏦 Guía de Integración de Bancos al Switch DIGICONECU

**Documento para Nuevos Integrantes del Equipo**
**Última Actualización:** 21 Diciembre 2025

Esta guía explica **cómo agregar un nuevo banco** al Switch DIGICONECU sin romper la integración existente con BANTEC y ARCBANK.

---

## 📊 Estado Actual del Switch (Producción - GCP)

| Banco | IP Pública | Código | BINs Registrados | Webhook |
|---|---|---|---|---|
| BANTEC | `34.41.171.65` | `BANTEC` | `220000`, `220100` | `:8082/api/transacciones/webhook` |
| ARCBANK | `107.178.215.37` | `ARCBANK` | `230000`, `230100` | `:4082/api/transacciones/webhook` |

**Switch DIGICONECU:** `34.172.224.240:9080`

---

## 🚀 Pasos para Agregar un NUEVO BANCO

### Paso 1: Requisitos Previos

Tu banco debe tener:
- ✅ Una VM en GCP con IP pública
- ✅ Un microservicio de transacciones (`ms-transaccion`) corriendo
- ✅ Un endpoint webhook en `/api/transacciones/webhook`
- ✅ Un rango BIN asignado (6 dígitos únicos)

### Paso 2: Configurar Firewall en GCP

Agrega una regla de firewall para permitir que el Switch llegue a tu webhook:

| Campo | Valor |
|---|---|
| Nombre | `allow-mibanco-ports` |
| Dirección | Entrada |
| Destinos | Todas las instancias |
| Rangos IP origen | `0.0.0.0/0` |
| Puertos TCP | `XXXX` (puerto de tu ms-transaccion) |

### Paso 3: Registrar tu Banco en el Switch

Conéctate a la VM del Switch y ejecuta:

```bash
ssh usuario@34.172.224.240

sudo docker exec -i postgres-network psql -U postgres -d NetworkManagementDB <<EOF
-- 1. Insertar tu Banco
INSERT INTO "Bancos" ("Id", "Codigo", "Nombre", "Endpoint", "Estado", "EstadoCircuito", "FallosConsecutivos", "LatenciaPromedioMs")
VALUES (
    gen_random_uuid(), 
    'MIBANCO',                                                    -- Código único
    'Nombre de Mi Banco',                                         -- Nombre display
    'http://TU_IP_PUBLICA:PUERTO/api/transacciones/webhook',      -- Webhook URL
    'Activo', 
    'CLOSED', 
    0, 
    0
) ON CONFLICT ("Codigo") DO UPDATE SET "Endpoint" = EXCLUDED."Endpoint";

-- 2. Registrar tus BINs (cambia XXXXXX por tu rango)
INSERT INTO "Enrutamiento" ("Id", "BancoId", "BinInicio", "BinFin", "Activo")
SELECT gen_random_uuid(), "Id", 'XXXXXX', 'XXXXXX', true 
FROM "Bancos" WHERE "Codigo" = 'MIBANCO';
EOF
```

**Reemplaza:**
- `MIBANCO` → Tu código de banco (máx 20 caracteres, único)
- `TU_IP_PUBLICA` → IP pública de tu VM en GCP
- `PUERTO` → Puerto donde corre tu `ms-transaccion` (ej: 8082)
- `XXXXXX` → Tu rango BIN (6 dígitos, debe ser único)

### Paso 4: Verificar Registro

```bash
# Verificar que tu banco aparece en la lista
curl http://34.172.224.240:9080/api/bancos

# Verificar el enrutamiento
sudo docker exec -i postgres-network psql -U postgres -d NetworkManagementDB -c 'SELECT b."Codigo", e."BinInicio", e."BinFin" FROM "Enrutamiento" e JOIN "Bancos" b ON e."BancoId" = b."Id";'
```

---

## 📌 Rangos BIN Asignados (NO USAR)

| Banco | Rango BIN | Estado |
|---|---|---|
| BANTEC | `220000` - `220100` | ⛔ Ocupado |
| ARCBANK | `230000` - `230100` | ⛔ Ocupado |

**Rangos Disponibles para nuevos bancos:**
- `240000` - `249999`
- `250000` - `259999`
- `260000` - `269999`
- etc.

---

## 🔌 Implementar el Webhook en tu Banco

Tu microservicio de transacciones debe tener un controlador como este:

```java
@RestController
@RequestMapping("/api/transacciones/webhook")
public class WebhookController {

    @PostMapping
    public ResponseEntity<?> recibirTransferenciaEntrante(@RequestBody Map<String, Object> payload) {
        // payload contiene:
        // - bancoOrigen: String (código del banco origen)
        // - cuentaOrigen: String
        // - cuentaDestino: String (cuenta de TU banco)
        // - monto: BigDecimal
        // - referencia: String (ID único de la transacción)
        // - concepto: String
        
        String cuentaDestino = (String) payload.get("cuentaDestino");
        BigDecimal monto = new BigDecimal(payload.get("monto").toString());
        
        // 1. Buscar la cuenta destino en tu banco
        // 2. Acreditar el monto
        // 3. Registrar la transacción entrante
        // 4. Devolver respuesta exitosa
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Transferencia recibida exitosamente"
        ));
    }
}
```

---

## 🔧 Configurar tu Banco para Enviar Transferencias

En tu `ms-transaccion`, configura la URL del Switch:

**application.yaml:**
```yaml
app:
  switch:
    url: http://34.172.224.240:9080
    banco-codigo: MIBANCO  # Tu código de banco
```

**docker-compose.yml (variables de entorno):**
```yaml
ms-transaccion:
  environment:
    APP_SWITCH_URL: http://34.172.224.240:9080
    BANCO_CODIGO: MIBANCO
```

---

## 🚫 Qué NO Tocar

Para evitar romper la integración existente:

| ❌ NO TOCAR | Razón |
|---|---|
| Código del Switch | Ya funciona para BANTEC y ARCBANK |
| Puertos del Switch (9080, 9081, 9082) | Configuración fija |
| BINs de otros bancos | Causaría conflictos de enrutamiento |
| Tabla `"Bancos"` (registros existentes) | Solo agregar nuevos, nunca modificar |

---

## ✅ Checklist de Integración

- [ ] VM creada en GCP con IP pública
- [ ] Microservicio `ms-transaccion` corriendo
- [ ] Endpoint `/api/transacciones/webhook` implementado
- [ ] Firewall permite tráfico al puerto del microservicio
- [ ] Banco registrado en `"Bancos"` del Switch
- [ ] BIN registrado en `"Enrutamiento"` del Switch
- [ ] Variable `APP_SWITCH_URL` configurada apuntando al Switch
- [ ] Prueba de transferencia desde otro banco exitosa
- [ ] Prueba de transferencia hacia otro banco exitosa

---

## 💡 Troubleshooting

| Error | Causa | Solución |
|---|---|---|
| **422 Unprocessable Entity** | BIN no registrado en Switch | Ejecutar SQL de registro de BIN |
| **504 Gateway Timeout** | Firewall bloquea puerto | Agregar puerto a regla de Firewall GCP |
| **404 Not Found** | Código de banco incorrecto | Verificar que `BANCO_CODIGO` coincide con el registrado |
| **Connection Refused** | Microservicio no corre | Verificar `docker ps` en la VM |
