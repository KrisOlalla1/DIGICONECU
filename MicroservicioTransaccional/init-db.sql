-- ========================================
-- SCRIPT SQL PARA CREAR LAS TABLAS
-- Base de datos: switch_db
-- ========================================

-- Tabla: EntidadBancaria
CREATE TABLE "EntidadBancaria" (
    "IdBanco" SERIAL PRIMARY KEY,
    "Nombre" VARCHAR(100) NOT NULL,
    "EndpointProduccion" VARCHAR(255),
    "Estado" VARCHAR(20),
    "Saldo" DECIMAL(15,2)
);

-- Tabla: Transaccion
CREATE TABLE "Transaccion" (
    "IdInstruccion" SERIAL PRIMARY KEY,
    "EndToEnd" VARCHAR(50) UNIQUE NOT NULL,
    "TraceId" VARCHAR(50),
    "IdBancoOrigen" INTEGER NOT NULL,
    "IdBancoDestino" INTEGER NOT NULL,
    "CuentaOrigen" VARCHAR(20) NOT NULL,
    "CuentaDestino" VARCHAR(20) NOT NULL,
    "Monto" DECIMAL(15,2) NOT NULL,
    "Mensaje" VARCHAR(200),
    "EstadoActual" VARCHAR(20) NOT NULL,
    "FechaCreacion" TIMESTAMP NOT NULL,
    "CodigoRespuestaFinal" VARCHAR(10),
    CONSTRAINT fk_banco_origen FOREIGN KEY ("IdBancoOrigen") REFERENCES "EntidadBancaria"("IdBanco"),
    CONSTRAINT fk_banco_destino FOREIGN KEY ("IdBancoDestino") REFERENCES "EntidadBancaria"("IdBanco")
);

-- Índices para mejorar el rendimiento
CREATE INDEX idx_transaccion_endtoend ON "Transaccion"("EndToEnd");
CREATE INDEX idx_transaccion_estado ON "Transaccion"("EstadoActual");
CREATE INDEX idx_transaccion_fecha ON "Transaccion"("FechaCreacion");

-- Datos de prueba: Bancos
INSERT INTO "EntidadBancaria" ("Nombre", "EndpointProduccion", "Estado", "Saldo") VALUES
('Banco ARCBANK', 'http://localhost:8080/api/transacciones', 'ACTIVO', 1000000.00),
('Banco Pichincha', 'http://banco-pichincha.com/api/transacciones', 'ACTIVO', 500000.00),
('Banco Guayaquil', 'http://banco-guayaquil.com/api/transacciones', 'ACTIVO', 750000.00),
('Banco Pacífico', 'http://banco-pacifico.com/api/transacciones', 'ACTIVO', 650000.00);

-- Datos de prueba: Transacciones
INSERT INTO "Transaccion" ("EndToEnd", "TraceId", "IdBancoOrigen", "IdBancoDestino", 
    "CuentaOrigen", "CuentaDestino", "Monto", "Mensaje", "EstadoActual", "FechaCreacion", "CodigoRespuestaFinal") 
VALUES
('E2E-TEST-001', 'TRACE-001', 1, 2, '1234567890', '0987654321', 100.50, 'Transferencia de prueba', 'Exitoso', NOW(), '00'),
('E2E-TEST-002', 'TRACE-002', 2, 1, '1111222233', '4444555566', 250.75, 'Pago de factura', 'Exitoso', NOW(), '00'),
('E2E-TEST-003', 'TRACE-003', 1, 3, '7777888899', '0000111122', 500.00, 'Transferencia urgente', 'Procesando', NOW(), NULL);

-- Comentarios en las tablas
COMMENT ON TABLE "EntidadBancaria" IS 'Catálogo de bancos participantes en el switch';
COMMENT ON TABLE "Transaccion" IS 'Registro de todas las transacciones interbancarias';
COMMENT ON COLUMN "Transaccion"."EndToEnd" IS 'Identificador único de la transacción de extremo a extremo';
COMMENT ON COLUMN "Transaccion"."TraceId" IS 'ID de trazabilidad para seguimiento';
COMMENT ON COLUMN "Transaccion"."EstadoActual" IS 'Estados: Enviado, Procesando, Exitoso, Fallido, Timeout';
