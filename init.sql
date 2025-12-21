-- 1. Esquemas para el CORE (Si quieres separarlos más)
CREATE SCHEMA IF NOT EXISTS nexus_cuentas;
CREATE SCHEMA IF NOT EXISTS nexus_clientes;
CREATE SCHEMA IF NOT EXISTS nexus_transacciones;

-- 2. Esquema para el SERVICIO WEB
CREATE SCHEMA IF NOT EXISTS nexus_web;

-- 3. Esquema para el SERVICIO VENTANILLA
CREATE SCHEMA IF NOT EXISTS nexus_ventanilla;