-- ============================================
-- NETWORK MANAGEMENT SERVICE - DATABASE INIT
-- BOUNDED CONTEXT: Network Management (SUPPORTING)
-- ============================================

CREATE TABLE IF NOT EXISTS "Bancos" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "Codigo" VARCHAR(20) UNIQUE NOT NULL,
    "Nombre" VARCHAR(255) NOT NULL,
    "Endpoint" VARCHAR(500) NOT NULL,
    "Estado" VARCHAR(20) NOT NULL DEFAULT 'Activo',
    -- Columnas adicionales para Circuit Breaker y Health Check
    "FallosConsecutivos" INTEGER DEFAULT 0,
    "UltimoFallo" TIMESTAMPTZ,
    "UltimoHealthCheck" TIMESTAMPTZ,
    "LatenciaPromedioMs" BIGINT DEFAULT 0,
    "EstadoCircuito" VARCHAR(20) DEFAULT 'CLOSED',
    "CircuitoAbiertoDesde" TIMESTAMPTZ,
    
    CONSTRAINT "CHK_Bancos_Estado" CHECK ("Estado" IN ('Activo', 'Suspendido', 'Mantenimiento')),
    CONSTRAINT "CHK_Bancos_EstadoCircuito" CHECK ("EstadoCircuito" IN ('CLOSED', 'OPEN', 'HALF_OPEN'))
);

CREATE INDEX IF NOT EXISTS "IDX_Bancos_Codigo" ON "Bancos"("Codigo");
CREATE INDEX IF NOT EXISTS "IDX_Bancos_Estado" ON "Bancos"("Estado");

COMMENT ON TABLE "Bancos" IS 'BOUNDED CONTEXT: Network Management - Directorio de participantes';
COMMENT ON COLUMN "Bancos"."Codigo" IS 'Identificador inmutable del banco (NUNCA cambia)';

CREATE TABLE IF NOT EXISTS "Enrutamiento" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "BancoId" UUID NOT NULL,
    "BinInicio" VARCHAR(6) NOT NULL,
    "BinFin" VARCHAR(6) NOT NULL,
    "Activo" BOOLEAN DEFAULT TRUE,
    
    CONSTRAINT "FK_Enrutamiento_Banco" FOREIGN KEY ("BancoId") 
        REFERENCES "Bancos"("Id") ON DELETE CASCADE,
    CONSTRAINT "UQ_Enrutamiento_BinRango" UNIQUE("BinInicio", "BinFin")
);

CREATE INDEX IF NOT EXISTS "IDX_Enrutamiento_Bin" ON "Enrutamiento"("BinInicio", "BinFin");
CREATE INDEX IF NOT EXISTS "IDX_Enrutamiento_Banco" ON "Enrutamiento"("BancoId");

COMMENT ON TABLE "Enrutamiento" IS 'Rangos BIN para resolver a qué banco pertenece una cuenta';

INSERT INTO "Bancos" ("Codigo", "Nombre", "Endpoint", "Estado") VALUES
('PICHINCHA', 'Banco Pichincha', 'https://api.pichincha.com/incoming/credit', 'Activo'),
('GUAYAQUIL', 'Banco de Guayaquil', 'https://api.guayaquil.com/incoming/credit', 'Activo'),
('PACIFICO', 'Banco del Pacífico', 'https://api.pacifico.com/incoming/credit', 'Activo'),
('PRODUBANCO', 'Produbanco', 'https://api.produbanco.com/incoming/credit', 'Activo'),
('BOLIVARIANO', 'Banco Bolivariano', 'https://api.bolivariano.com/incoming/credit', 'Activo')
ON CONFLICT ("Codigo") DO NOTHING;

INSERT INTO "Enrutamiento" ("BancoId", "BinInicio", "BinFin")
SELECT "Id", '100000', '199999' FROM "Bancos" WHERE "Codigo" = 'PICHINCHA'
ON CONFLICT ON CONSTRAINT "UQ_Enrutamiento_BinRango" DO NOTHING;

INSERT INTO "Enrutamiento" ("BancoId", "BinInicio", "BinFin")
SELECT "Id", '200000', '299999' FROM "Bancos" WHERE "Codigo" = 'GUAYAQUIL'
ON CONFLICT ON CONSTRAINT "UQ_Enrutamiento_BinRango" DO NOTHING;

INSERT INTO "Enrutamiento" ("BancoId", "BinInicio", "BinFin")
SELECT "Id", '300000', '399999' FROM "Bancos" WHERE "Codigo" = 'PACIFICO'
ON CONFLICT ON CONSTRAINT "UQ_Enrutamiento_BinRango" DO NOTHING;

INSERT INTO "Enrutamiento" ("BancoId", "BinInicio", "BinFin")
SELECT "Id", '400000', '499999' FROM "Bancos" WHERE "Codigo" = 'PRODUBANCO'
ON CONFLICT ON CONSTRAINT "UQ_Enrutamiento_BinRango" DO NOTHING;

INSERT INTO "Enrutamiento" ("BancoId", "BinInicio", "BinFin")
SELECT "Id", '500000', '599999' FROM "Bancos" WHERE "Codigo" = 'BOLIVARIANO'
ON CONFLICT ON CONSTRAINT "UQ_Enrutamiento_BinRango" DO NOTHING;

CREATE OR REPLACE VIEW "BancosActivos" AS
SELECT 
    b."Codigo",
    b."Nombre",
    b."Endpoint",
    b."Estado",
    COUNT(e."Id") as "TotalRangos"
FROM "Bancos" b
LEFT JOIN "Enrutamiento" e ON b."Id" = e."BancoId"
WHERE b."Estado" = 'Activo'
GROUP BY b."Codigo", b."Nombre", b."Endpoint", b."Estado";
