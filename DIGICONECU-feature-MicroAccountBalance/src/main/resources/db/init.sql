-- ============================================
-- ACCOUNT BALANCE SERVICE - DATABASE INIT
-- BOUNDED CONTEXT: Account Balance (SUPPORTING)
-- ============================================

CREATE TABLE IF NOT EXISTS "CuentasPrefondeo" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "BancoCodigo" VARCHAR(20) UNIQUE NOT NULL,
    "SaldoDisponible" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    "SaldoCongelado" DECIMAL(18,2) NOT NULL DEFAULT 0.00,
    "Moneda" VARCHAR(3) NOT NULL DEFAULT 'USD',
    "FechaActualizacion" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "CHK_CuentasPrefondeo_Saldos" CHECK ("SaldoDisponible" >= 0 AND "SaldoCongelado" >= 0)
);

CREATE INDEX IF NOT EXISTS "IDX_CuentasPrefondeo_Banco" ON "CuentasPrefondeo"("BancoCodigo");

COMMENT ON TABLE "CuentasPrefondeo" IS 'BOUNDED CONTEXT: Account Balance - Saldos técnicos de prefondeo';
COMMENT ON COLUMN "CuentasPrefondeo"."BancoCodigo" IS 'Código del banco (NO es FK, se valida por API)';
COMMENT ON COLUMN "CuentasPrefondeo"."SaldoDisponible" IS 'Dinero libre para nuevas transacciones';
COMMENT ON COLUMN "CuentasPrefondeo"."SaldoCongelado" IS 'Dinero reservado para transacciones en proceso';

CREATE TABLE IF NOT EXISTS "OperacionesPrefondeo" (
    "Id" UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    "CuentaId" UUID NOT NULL,
    "InstructionId" UUID,
    "TipoOperacion" VARCHAR(20) NOT NULL,
    "Monto" DECIMAL(18,2) NOT NULL,
    "SaldoAnterior" DECIMAL(18,2) NOT NULL,
    "SaldoPosterior" DECIMAL(18,2) NOT NULL,
    "FechaOperacion" TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT "FK_OperacionesPrefondeo_Cuenta" FOREIGN KEY ("CuentaId") 
        REFERENCES "CuentasPrefondeo"("Id"),
    CONSTRAINT "CHK_OperacionesPrefondeo_Tipo" CHECK (
        "TipoOperacion" IN ('Congelar', 'Descongelar', 'Debitar', 'Acreditar', 'Recargar', 'Liberar')
    )
);

CREATE INDEX IF NOT EXISTS "IDX_OperacionesPrefondeo_Cuenta" ON "OperacionesPrefondeo"("CuentaId");
CREATE INDEX IF NOT EXISTS "IDX_OperacionesPrefondeo_Instruction" ON "OperacionesPrefondeo"("InstructionId");
CREATE INDEX IF NOT EXISTS "IDX_OperacionesPrefondeo_Fecha" ON "OperacionesPrefondeo"("FechaOperacion" DESC);

COMMENT ON TABLE "OperacionesPrefondeo" IS 'Auditoría interna de operaciones sobre saldos';

-- Nota: La actualización de FechaActualizacion se maneja por JPA @PreUpdate

INSERT INTO "CuentasPrefondeo" ("BancoCodigo", "SaldoDisponible", "SaldoCongelado", "Moneda")
VALUES 
('PICHINCHA', 500000.00, 0.00, 'USD'),
('GUAYAQUIL', 500000.00, 0.00, 'USD'),
('PACIFICO', 500000.00, 0.00, 'USD'),
('PRODUBANCO', 500000.00, 0.00, 'USD'),
('BOLIVARIANO', 500000.00, 0.00, 'USD')
ON CONFLICT ("BancoCodigo") DO NOTHING;
