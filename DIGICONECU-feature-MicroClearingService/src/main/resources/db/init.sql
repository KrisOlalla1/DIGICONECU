-- ============================================
-- CLEARING SERVICE - DATABASE INIT
-- BOUNDED CONTEXT: Clearing (CORE)
-- ============================================

CREATE TABLE IF NOT EXISTS "CiclosCompensacion" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "FechaCiclo" DATE UNIQUE NOT NULL,
    "HoraCorte" TIMESTAMP NOT NULL,
    "TotalTransacciones" INT NOT NULL DEFAULT 0,
    "PosicionesNetas" JSONB NOT NULL,
    "Estado" VARCHAR(20) NOT NULL DEFAULT 'Completado',
    "ArchivoLiquidacionUrl" VARCHAR(500),
    "ArchivoXmlContenido" TEXT,
    "ArchivoCsvContenido" TEXT,
    
    CONSTRAINT "CHK_CiclosCompensacion_Estado" CHECK ("Estado" IN ('Completado', 'Fallido'))
);

CREATE INDEX IF NOT EXISTS "IDX_CiclosCompensacion_Fecha" ON "CiclosCompensacion"("FechaCiclo" DESC);

COMMENT ON TABLE "CiclosCompensacion" IS 'BOUNDED CONTEXT: Clearing - Ciclos de compensación diarios';
COMMENT ON COLUMN "CiclosCompensacion"."PosicionesNetas" IS 'JSON con neteos: {"BANK-A": {"Enviado": 15000, "Recibido": 11000, "Neto": -4000}}';

CREATE OR REPLACE VIEW "UltimosCiclos" AS
SELECT 
    "FechaCiclo",
    "HoraCorte",
    "TotalTransacciones",
    "Estado",
    "ArchivoLiquidacionUrl"
FROM "CiclosCompensacion"
ORDER BY "FechaCiclo" DESC
LIMIT 30;
