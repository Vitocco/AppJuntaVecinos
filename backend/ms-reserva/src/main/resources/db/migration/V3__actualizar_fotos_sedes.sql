-- Fotos definitivas de las sedes (archivos en src/main/resources/static/fotos/).
-- orden 0 = portada: es la que se usa como sedeFoto en las reservas.
DELETE FROM sede_foto;

INSERT INTO sede_foto (sede_id, orden, ruta) VALUES
(1, 0, '/api/sedes/fotos/salon-1.jpg'),   -- Salón principal: interior con escenario
(1, 1, '/api/sedes/fotos/salon-2.jpg'),   -- Salón principal: fachada
(2, 0, '/api/sedes/fotos/cocina-1.jpg'),  -- Cocina comunitaria
(3, 0, '/api/sedes/fotos/patio-1.jpg');   -- Patio y quincho
