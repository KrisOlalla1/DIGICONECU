# ☁️ Guía de Despliegue: Switch DIGICONECU en GCP

Esta guía detalla los pasos para desplegar el **Switch Transaccional (DIGICONECU)** en una Máquina Virtual (VM) de Google Cloud Platform.

## 1. Requisitos de Infraestructura (GCP)

### 🖥️ Máquina Virtual (Compute Engine)
*   **Nombre:** `vm-switch-digiconecu`
*   **Zona:** `us-central1-a` (o tu preferencia)
*   **Máquina:** `e2-medium` (2 vCPU, 4GB memoria) como mínimo.
*   **OS:** Ubuntu 22.04 LTS (x86/64).
*   **Disco:** 20GB HDD o Balanceado.

### 🛡️ Firewall (VPC Network)
Debes crear una regla de firewall para permitir tráfico entrante a la API Gateway.
*   **Nombre:** `allow-switch-gateway`
*   **Tráfico:** Ingress (Entrada)
*   **IPs de origen:** `0.0.0.0/0` (Público)
*   **Puertos:** `tcp:9080` (Puerto definido en docker-compose para el Gateway)

## 2. Preparación del Código

1.  Comprime la carpeta `DIGICONECU` completa en tu máquina local.
    *   Asegúrate de incluir `docker-compose.yml` y todas las subcarpetas.
    *   Ejemplo: `zip -r digiconecu.zip DIGICONECU/`

## 3. Instalación en el Servidor (SSH)

Conéctate por SSH a tu nueva VM y sigue estos pasos:

### 3.1. Instalar Docker y Docker Compose
```bash
# Actualizar repositorios
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg

# Instalar Docker
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=\"$(dpkg --print-architecture)\" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Verificar instalación
sudo docker run hello-world
```

### 3.2. Subir y Descomprimir Proyecto
Sube el archivo `digiconecu.zip` (puedes usar el botón "Upload file" en la consola SSH de GCP o `scp`).

```bash
sudo apt-get install unzip
unzip digiconecu.zip
cd DIGICONECU
```

### 3.3. Iniciar Servicios
```bash
# Construir e iniciar contenedores en segundo plano
sudo docker compose up -d --build
```

**Verificación:**
Ejecuta `sudo docker ps`. Deberías ver 8 microservicios + bases de datos + Redis corriendo.

## 4. Validación Inicial
Desde tu navegador o Postman, intenta acceder a la URL pública de tu VM:
`http://<IP_PUBLICA_VM>:9080/actuator/health`

Debería responder `{"status":"UP"}`.

## ⚠️ 5. Configuración Post-Despliegue (IMPORTANTE)
El Switch actualmente está configurado para buscar los bancos en nombres de host de Docker locales (`ms-transaccion-arcbank`). Cuando despliegues los Bancos en las VMs 2 y 3, **debes actualizar la base de datos del Switch** con las IPs reales.

**Comando para actualizar endpoints (Ejecutar SOLO cuando tengas las IPs de los Bancos):**
```bash
# Entrar a la base de datos de Network Management
sudo docker exec -it postgres-network psql -U postgres -d NetworkManagementDB

# En la consola SQL:
-- Actualizar BANTEC (Reemplaza X.X.X.X con la IP Pública de la VM de BANTEC)
UPDATE institucion_financiera 
SET endpoint_url = 'http://X.X.X.X:8080/api/v1/transacciones/webhook' 
WHERE codigo_bic = 'BANTEC';

-- Actualizar ARCBANK (Reemplaza Y.Y.Y.Y con la IP Pública de la VM de ARCBANK)
UPDATE institucion_financiera 
SET endpoint_url = 'http://Y.Y.Y.Y:8080/api/v1/transacciones/webhook' 
WHERE codigo_bic = 'ARCBANK';
```
