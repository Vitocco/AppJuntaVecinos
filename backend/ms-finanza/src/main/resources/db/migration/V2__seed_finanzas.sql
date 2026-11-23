-- Categorías
INSERT INTO categoria_movimiento (id, nombre, tipo, activa) VALUES
(1,  'Arriendo sede', 'INGRESO', TRUE),
(2,  'Cuotas',        'INGRESO', TRUE),
(3,  'Donación',      'INGRESO', TRUE),
(4,  'Otro ingreso',  'INGRESO', TRUE),
(5,  'Luz',           'GASTO',   TRUE),
(6,  'Agua',          'GASTO',   TRUE),
(7,  'Gas',           'GASTO',   TRUE),
(8,  'Mantención',    'GASTO',   TRUE),
(9,  'Materiales',    'GASTO',   TRUE),
(10, 'Otro gasto',    'GASTO',   TRUE);

-- Movimientos de octubre y noviembre 2026 (registrados por la tesorera, usuario 2)
INSERT INTO movimiento_financiero (id, tipo, categoria_id, reserva_id, descripcion, monto, fecha_movimiento, usuario_registro_id, estado, fecha_creacion) VALUES
-- Octubre: ingresos 500.300 - gastos 123.500 = 376.800
(1,  'INGRESO', 4,  NULL, 'Saldo inicial',                            331300, '2026-10-01', 2, 'VIGENTE', '2026-10-01 09:00:00'),
(2,  'INGRESO', 2,  NULL, 'Cuotas sociales octubre',                   84000, '2026-10-05', 2, 'VIGENTE', '2026-10-05 19:00:00'),
(3,  'GASTO',   5,  NULL, 'Boleta de luz septiembre',                  38450, '2026-10-08', 2, 'VIGENTE', '2026-10-08 12:00:00'),
(4,  'GASTO',   6,  NULL, 'Boleta de agua septiembre',                 21300, '2026-10-10', 2, 'VIGENTE', '2026-10-10 12:00:00'),
(5,  'INGRESO', 3,  NULL, 'Donación almacén El Vecino',                50000, '2026-10-15', 2, 'VIGENTE', '2026-10-15 18:30:00'),
(6,  'GASTO',   8,  NULL, 'Reparación chapa puerta principal',         45000, '2026-10-20', 2, 'VIGENTE', '2026-10-20 17:00:00'),
(7,  'GASTO',   9,  NULL, 'Artículos de aseo',                         18750, '2026-10-25', 2, 'VIGENTE', '2026-10-25 11:00:00'),
(8,  'INGRESO', 1,  NULL, 'Arriendo Salón principal - aniversario club', 35000, '2026-10-26', 2, 'VIGENTE', '2026-10-26 20:00:00'),
-- Noviembre
(9,  'INGRESO', 2,  NULL, 'Cuotas sociales noviembre',                 78000, '2026-11-05', 2, 'VIGENTE', '2026-11-05 19:00:00'),
(10, 'GASTO',   5,  NULL, 'Boleta de luz octubre',                     41200, '2026-11-07', 2, 'VIGENTE', '2026-11-07 12:00:00'),
(11, 'INGRESO', 1,  1,    'Arriendo Cocina comunitaria - Camila Rojas (2026-11-10)', 15000, '2026-11-10', 2, 'VIGENTE', '2026-11-10 15:00:00'),
(12, 'GASTO',   7,  NULL, 'Recarga gas cocina comunitaria',            32000, '2026-11-12', 2, 'VIGENTE', '2026-11-12 10:00:00'),
(13, 'GASTO',   6,  NULL, 'Boleta de agua octubre',                    19800, '2026-11-14', 2, 'VIGENTE', '2026-11-14 12:00:00'),
-- Ejemplo de movimiento anulado (no cuenta en saldos ni informes)
(14, 'GASTO',   9,  NULL, 'Compra de materiales (registrada dos veces)', 12000, '2026-11-14', 2, 'ANULADO', '2026-11-14 12:30:00');

-- Pago en efectivo de la reserva 1 de ms-reserva (Camila, cocina, 10-nov)
INSERT INTO pago (id, reserva_id, movimiento_id, monto, fecha_pago, medio, referencia, estado, webpay_token, fecha_creacion) VALUES
(1, 1, 11, 15000, '2026-11-10', 'EFECTIVO', 'Recibo N° 0001', 'PAGADO', NULL, '2026-11-10 15:00:00');

-- Informe publicado (los totales se calculan al consultarlo)
INSERT INTO informe_financiero (id, titulo, periodo_inicio, periodo_fin, observacion, publicado, fecha_generacion, usuario_generador_id) VALUES
(1, 'Rendición octubre', '2026-10-01', '2026-10-31', 'Rendición de cuentas presentada en la asamblea de noviembre.', TRUE, '2026-11-03 20:00:00', 2);
