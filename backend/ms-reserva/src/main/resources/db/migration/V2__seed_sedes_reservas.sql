-- Sedes de la Junta de Vecinos Población Zaror
INSERT INTO sede (id, nombre, descripcion, ubicacion, capacidad, precio, activa) VALUES
(1, 'Salón principal',    'Salón amplio con escenario, mesas y sillas. Ideal para cumpleaños, reuniones y eventos.', 'Sede social, primer piso', 80, 35000, TRUE),
(2, 'Cocina comunitaria', 'Cocina equipada con cocinilla industrial, horno y refrigerador. Para talleres y preparación de eventos.', 'Sede social, costado del salón', 15, 15000, TRUE),
(3, 'Patio y quincho',    'Patio techado con quincho, parrilla y mesones. Para asados y celebraciones al aire libre.', 'Sede social, patio trasero', 40, 25000, TRUE);

INSERT INTO sede_foto (sede_id, orden, ruta) VALUES
(1, 0, '/api/sedes/fotos/salon-1.jpg'),
(1, 1, '/api/sedes/fotos/salon-2.jpg'),
(2, 0, '/api/sedes/fotos/cocina-1.jpg'),
(3, 0, '/api/sedes/fotos/patio-1.jpg'),
(3, 1, '/api/sedes/fotos/patio-2.jpg');

-- Reservas de noviembre 2026 de Camila (id 3) y Luis (id 4)
INSERT INTO reserva (id, usuario_id, usuario_nombre, sede_id, fecha, hora_inicio, hora_fin, motivo, asistentes, estado, pagada, observacion, fecha_creacion, fecha_actualizacion) VALUES
-- 1) Ya realizada y pagada (ms-finanza tiene su pago y su ingreso "Arriendo sede")
(1, 3, 'Camila Rojas', 2, '2026-11-10', '10:00', '14:00', 'Taller de cocina navideña', 12, 'APROBADA',  TRUE,  'Aprobada',                                    '2026-11-02 10:00:00', '2026-11-05 18:00:00'),
-- 2) Aprobada y pendiente de pago (sirve para probar Webpay)
(2, 3, 'Camila Rojas', 1, '2026-11-28', '15:00', '20:00', 'Cumpleaños de mi hija',     50, 'APROBADA',  FALSE, 'Aprobada, recuerde dejar el salón limpio',    '2026-11-12 09:30:00', '2026-11-13 19:00:00'),
-- 3) Pendiente de revisión por la directiva
(3, 4, 'Luis Pérez',   3, '2026-11-22', '13:00', '18:00', 'Asado familiar',            25, 'PENDIENTE', FALSE, NULL,                                          '2026-11-15 20:10:00', '2026-11-15 20:10:00'),
-- 4) Rechazada porque se cruzaba con la reserva 2
(4, 4, 'Luis Pérez',   1, '2026-11-28', '18:00', '22:00', 'Reunión del club deportivo', 30, 'RECHAZADA', FALSE, 'Se superpone con otra reserva aprobada',      '2026-11-14 11:00:00', '2026-11-14 17:00:00');

-- Historial coherente con lo anterior (responsable 1 = directiva)
INSERT INTO historial_reserva (reserva_id, estado_anterior, estado_nuevo, usuario_responsable_id, observacion, fecha_cambio) VALUES
(1, NULL,        'PENDIENTE', 3, NULL,                                       '2026-11-02 10:00:00'),
(1, 'PENDIENTE', 'APROBADA',  1, 'Aprobada',                                 '2026-11-05 18:00:00'),
(2, NULL,        'PENDIENTE', 3, NULL,                                       '2026-11-12 09:30:00'),
(2, 'PENDIENTE', 'APROBADA',  1, 'Aprobada, recuerde dejar el salón limpio', '2026-11-13 19:00:00'),
(3, NULL,        'PENDIENTE', 4, NULL,                                       '2026-11-15 20:10:00'),
(4, NULL,        'PENDIENTE', 4, NULL,                                       '2026-11-14 11:00:00'),
(4, 'PENDIENTE', 'RECHAZADA', 1, 'Se superpone con otra reserva aprobada',   '2026-11-14 17:00:00');
