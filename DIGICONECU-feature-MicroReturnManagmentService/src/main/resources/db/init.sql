-- ============================================
-- RETURN MANAGEMENT SERVICE - DATABASE INIT
-- BOUNDED CONTEXT: Return Management (CORE)
-- ============================================

CREATE TABLE IF NOT EXISTS "Devoluciones" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "ReturnInstructionId" UUID UNIQUE NOT NULL,
    "TransaccionOriginalInstructionId" UUID NOT NULL,
    "TransaccionInversaId" VARCHAR(50),
    "BancoIniciadorCodigo" VARCHAR(20) NOT NULL,
    "Motivo" VARCHAR(10) NOT NULL,
    "Monto" DECIMAL(18,2) NOT NULL,
    "Estado" VARCHAR(20) NOT NULL DEFAULT 'Completada',
    "FechaCreacion" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "CHK_Devoluciones_Estado" CHECK ("Estado" IN ('Completada', 'Fallida'))
);

CREATE INDEX IF NOT EXISTS "IDX_Devoluciones_Original" ON "Devoluciones"("TransaccionOriginalInstructionId");
CREATE INDEX IF NOT EXISTS "IDX_Devoluciones_ReturnId" ON "Devoluciones"("ReturnInstructionId");
CREATE INDEX IF NOT EXISTS "IDX_Devoluciones_Fecha" ON "Devoluciones"("FechaCreacion" DESC);
CREATE INDEX IF NOT EXISTS "IDX_Devoluciones_BancoIniciador" ON "Devoluciones"("BancoIniciadorCodigo");

COMMENT ON TABLE "Devoluciones" IS 'BOUNDED CONTEXT: Return Management - Devoluciones de transacciones';
COMMENT ON COLUMN "Devoluciones"."TransaccionOriginalInstructionId" IS 'Referencia a TX original (NO es FK, se valida por API)';
COMMENT ON COLUMN "Devoluciones"."BancoIniciadorCodigo" IS 'Banco que solicita devolución (NO es FK, se valida por API)';

CREATE OR REPLACE VIEW "DevolucionesRecientes" AS
SELECT 
    "ReturnInstructionId",
    "TransaccionOriginalInstructionId",
    "BancoIniciadorCodigo",
    "Motivo",
    "Monto",
    "Estado",
    "FechaCreacion"
FROM "Devoluciones"
ORDER BY "FechaCreacion" DESC
LIMIT 100;
