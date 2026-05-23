
INSERT INTO recinto (nombre_recinto)
SELECT 'Polideportivo Municipal Estadio Central' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM recinto WHERE nombre_recinto = 'Polideportivo Municipal Estadio Central');

INSERT INTO recinto (nombre_recinto)
SELECT 'Complejo Deportivo Bicentenario' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM recinto WHERE nombre_recinto = 'Complejo Deportivo Bicentenario');

INSERT INTO recinto (nombre_recinto)
SELECT 'Complejo Deportivo Oriente' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM recinto WHERE nombre_recinto = 'Complejo Deportivo Oriente');



INSERT INTO cancha (nombre, tipo_de_cancha, fecha_registro, direccion, capacidad, recinto_id, activo)
SELECT 'Cancha Central de Fútbol', 'PASTO_NATURAL', '2026-01-10', 'Av. Olimpo 1230, Sector Centro', 44, 1, true FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cancha WHERE nombre = 'Cancha Central de Fútbol');

INSERT INTO cancha (nombre, tipo_de_cancha, fecha_registro, direccion, capacidad, recinto_id, activo)
SELECT 'Cancha de Tenis Fernando González', 'ARCILLA', '2026-02-15', 'Av. Olimpo 1230, Sector Centro', 4, 1, true FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cancha WHERE nombre = 'Cancha de Tenis Fernando González');

INSERT INTO cancha (nombre, tipo_de_cancha, fecha_registro, direccion, capacidad, recinto_id, activo)
SELECT 'Cancha Futsal Techada A', 'SINTETICA', '2026-03-01', 'Calle Deporte Real 450', 12, 2, true FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cancha WHERE nombre = 'Cancha Futsal Techada A');

INSERT INTO cancha (nombre, tipo_de_cancha, fecha_registro, direccion, capacidad, recinto_id, activo)
SELECT 'Cancha de Básquetbol Multiuso', 'PARQUET', '2026-03-12', 'Calle Deporte Real 450', 20, 2, true FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM cancha WHERE nombre = 'Cancha de Básquetbol Multiuso');