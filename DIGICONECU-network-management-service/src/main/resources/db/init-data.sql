-- Datos iniciales para Network Management Service
-- Script de inicialización de bancos y rangos BIN

-- Insertar bancos de ejemplo
INSERT INTO Bancos (Id, Codigo, Nombre, Endpoint, Estado) VALUES
    (gen_random_uuid(), 'PICHINCHA', 'Banco Pichincha', 'http://pichincha-api:8080/webhook', 'Activo'),
    (gen_random_uuid(), 'GUAYAQUIL', 'Banco de Guayaquil', 'http://guayaquil-api:8080/webhook', 'Activo'),
    (gen_random_uuid(), 'PACIFICO', 'Banco del Pacífico', 'http://pacifico-api:8080/webhook', 'Activo'),
    (gen_random_uuid(), 'BOLIVARIANO', 'Banco Bolivariano', 'http://bolivariano-api:8080/webhook', 'Activo'),
    (gen_random_uuid(), 'INTERNACIONAL', 'Banco Internacional', 'http://internacional-api:8080/webhook', 'Activo')
ON CONFLICT DO NOTHING;

-- Insertar rangos BIN de ejemplo
-- Cada banco tiene rangos de números de cuenta asignados
INSERT INTO Enrutamiento (Id, BancoId, BinInicio, BinFin, Activo)
SELECT 
    gen_random_uuid(),
    (SELECT Id FROM Bancos WHERE Codigo = 'PICHINCHA'),
    '100000',
    '199999',
    true
WHERE NOT EXISTS (SELECT 1 FROM Enrutamiento WHERE BinInicio = '100000');

INSERT INTO Enrutamiento (Id, BancoId, BinInicio, BinFin, Activo)
SELECT 
    gen_random_uuid(),
    (SELECT Id FROM Bancos WHERE Codigo = 'GUAYAQUIL'),
    '250000',
    '259999',
    true
WHERE NOT EXISTS (SELECT 1 FROM Enrutamiento WHERE BinInicio = '250000');

INSERT INTO Enrutamiento (Id, BancoId, BinInicio, BinFin, Activo)
SELECT 
    gen_random_uuid(),
    (SELECT Id FROM Bancos WHERE Codigo = 'PACIFICO'),
    '300000',
    '349999',
    true
WHERE NOT EXISTS (SELECT 1 FROM Enrutamiento WHERE BinInicio = '300000');

INSERT INTO Enrutamiento (Id, BancoId, BinInicio, BinFin, Activo)
SELECT 
    gen_random_uuid(),
    (SELECT Id FROM Bancos WHERE Codigo = 'BOLIVARIANO'),
    '400000',
    '449999',
    true
WHERE NOT EXISTS (SELECT 1 FROM Enrutamiento WHERE BinInicio = '400000');

INSERT INTO Enrutamiento (Id, BancoId, BinInicio, BinFin, Activo)
SELECT 
    gen_random_uuid(),
    (SELECT Id FROM Bancos WHERE Codigo = 'INTERNACIONAL'),
    '500000',
    '549999',
    true
WHERE NOT EXISTS (SELECT 1 FROM Enrutamiento WHERE BinInicio = '500000');
