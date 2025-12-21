# Guía de Integración de Bancos al Switch DIGICONECU (Local)

Esta guía documenta los cambios realizados para que el Switch funcione localmente y explica **cómo agregar nuevos bancos** sin modificar la lógica interna del sistema.

## 📌 Estado Actual y Cambios Realizados

Para que el switch funcione en un entorno de desarrollo local, se realizaron las siguientes configuraciones críticas. **NO MODIFICAR** estas configuraciones a menos que sea estrictamente necesario.

1.  **Red Docker Compartida:** Se utiliza la red externa `switch-transaccional-network`. Todos los bancos deben conectarse a esta red para ser visibles por el switch.
2.  **Payment Service Real:** Se modificó `PaymentServiceV2.java` para realizar llamadas HTTP reales a los webhooks de los bancos, eliminando el simulador (mock) que tenía por defecto.
3.  **Timeouts:** Se aumentaron los timeouts a **10 segundos** para soportar la latencia de Docker en Windows/Mac.

---

## 🚀 Pasos para Agregar un NUEVO BANCO

Si eres un compañero integrando otro banco (ej. "Banco Pichincha"), sigue estos pasos EXACTOS para no romper nada.

### 1. Conectar tu Banco a la Red del Switch

En el `docker-compose.yml` de tu banco, asegúrate de que tu contenedor de transacciones (`ms-transaccion`) esté conectado a la red `switch-transaccional-network`.

```yaml
services:
  ms-transaccion:
    container_name: ms-transaccion-mibanco
    networks:
      - mi-banco-net
      - switch-transaccional-network  # <--- IMPORTANTE

networks:
  switch-transaccional-network:
    external: true
```

### 2. Registrar tu Banco en el Switch

Edita **ÚNICAMENTE** el archivo `DIGICONECU-network-management-service/src/main/resources/db/init.sql`.

Agrega tu banco al final de la sección de inserts. Necesitas:
- Código único (ej. `MIBANCO`)
- Nombre
- Endpoint del Webhook (accesible desde la red del switch)
- Rango BIN (6 dígitos)

**Ejemplo de SQL a agregar:**

```sql
-- 1. Insertar el Banco
INSERT INTO "Bancos" ("Codigo", "Nombre", "Endpoint", "Estado") VALUES
('MIBANCO', 'Banco MiBanco', 'http://ms-transaccion-mibanco:8080/api/transacciones/webhook', 'Activo')
ON CONFLICT ("Codigo") DO NOTHING;

-- 2. Asignar Rango BIN (Ej. 550000 - 559999)
INSERT INTO "Enrutamiento" ("BancoId", "BinInicio", "BinFin")
SELECT "Id", '550000', '559999' FROM "Bancos" WHERE "Codigo" = 'MIBANCO'
ON CONFLICT ON CONSTRAINT "UQ_Enrutamiento_BinRango" DO NOTHING;
```

⚠️ **Nota sobre el Endpoint:** El hostname (`http://ms-transaccion-mibanco...`) debe coincidir EXACTAMENTE con el `container_name` que definiste en tu docker-compose. El puerto debe ser el puerto INTERNO del contenedor (usualmente 8080), no el expuesto en localhost.

### 3. Aplicar los Cambios

Para que el switch reconozca el nuevo banco, debes reconstruir el servicio de gestión de red o reiniciar la base de datos si usas volúmenes persistentes.

Opción rápida (borrando datos antiguos):
```bash
cd DIGICONECU
docker-compose down -v
docker-compose up -d --build
```
*(Esto reiniciará todo el switch y cargará el nuevo `init.sql`)*.

---

## 🚫 Qué NO Tocar

Para evitar conflictos con la lógica de transferencias que ya funciona:

*   **NO toques** `DIGICONECU-feature-MicroPaymentProcessing/.../PaymentServiceV2.java`. Ya contiene la lógica para llamar a cualquier banco registrado.
*   **NO toques** la configuración de puertos del Switch (9080, 9081, etc).
*   **NO cambies** el nombre de la red `switch-transaccional-network`.

---

## ✅ Verificación

Para probar si tu banco quedó registrado correctamente, puedes consultar el API del switch (una vez levantado):

```bash
curl http://localhost:9082/api/v1/red/bancos
```

Deberías ver tu banco en la lista JSON.
