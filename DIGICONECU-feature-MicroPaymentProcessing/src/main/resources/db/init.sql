-- ============================================
-- PAYMENT PROCESSING SERVICE - DATABASE INIT
-- BOUNDED CONTEXT: Payment Processing (CORE)
-- ============================================

CREATE TABLE IF NOT EXISTS "Transacciones" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "InstructionId" UUID UNIQUE NOT NULL,
    "EndToEndId" VARCHAR(100),
    "BancoOrigenCodigo" VARCHAR(20) NOT NULL,
    "BancoDestinoCodigo" VARCHAR(20) NOT NULL,
    "Monto" DECIMAL(18,2) NOT NULL,
    "Moneda" VARCHAR(3) NOT NULL DEFAULT 'USD',
    "CuentaOrigen" VARCHAR(50) NOT NULL,
    "CuentaDestino" VARCHAR(50) NOT NULL,
    "Estado" VARCHAR(30) NOT NULL DEFAULT 'Recibida',
    "CodigoError" VARCHAR(10),
    "MensajeError" VARCHAR(500),
    "FechaCreacion" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "CHK_Transacciones_Monto" CHECK ("Monto" > 0),
    CONSTRAINT "CHK_Transacciones_Estado" CHECK (
        "Estado" IN ('Recibida', 'Enrutada', 'EsperandoRespuesta', 
                     'Completada', 'Fallida', 'Rechazada', 'Timeout', 'Revertida')
    )
);

CREATE INDEX IF NOT EXISTS "IDX_Transacciones_InstructionId" ON "Transacciones"("InstructionId");
CREATE INDEX IF NOT EXISTS "IDX_Transacciones_Estado" ON "Transacciones"("Estado");
CREATE INDEX IF NOT EXISTS "IDX_Transacciones_Fecha" ON "Transacciones"("FechaCreacion" DESC);
CREATE INDEX IF NOT EXISTS "IDX_Transacciones_BancoOrigen" ON "Transacciones"("BancoOrigenCodigo");
CREATE INDEX IF NOT EXISTS "IDX_Transacciones_BancoDestino" ON "Transacciones"("BancoDestinoCodigo");

COMMENT ON TABLE "Transacciones" IS 'BOUNDED CONTEXT: Payment Processing - Registro de transferencias';
COMMENT ON COLUMN "Transacciones"."BancoOrigenCodigo" IS 'Código del banco origen (NO es FK, se valida por API)';
COMMENT ON COLUMN "Transacciones"."BancoDestinoCodigo" IS 'Código del banco destino (NO es FK, se valida por API)';

CREATE TABLE IF NOT EXISTS "IdempotenciaCache" (
    "InstructionId" UUID PRIMARY KEY,
    "RespuestaJson" TEXT NOT NULL,
    "FechaCreacion" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "FK_IdempotenciaCache_Transaccion" FOREIGN KEY ("InstructionId") 
        REFERENCES "Transacciones"("InstructionId") ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS "IDX_IdempotenciaCache_Fecha" ON "IdempotenciaCache"("FechaCreacion");

COMMENT ON TABLE "IdempotenciaCache" IS 'Cache temporal (24h) para prevenir duplicados';

-- Nota: La limpieza de idempotencia se maneja por scheduled task en Java
