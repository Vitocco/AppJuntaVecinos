-- Perfiles de prueba: ids 1..4 iguales a las credenciales que crea el DataLoader de ms-auth
INSERT INTO usuario (id, nombre, email, telefono, numero_casa, fecha_nacimiento, recibir_avisos, fecha_creacion) VALUES
(1, 'Pedro Soto',   'directiva@zaror.cl', '+56911111111', '12',  '1970-03-15', TRUE,  NOW(6)),
(2, 'Rosa Muñoz',   'tesorero@zaror.cl',  '+56922222222', '27',  '1975-08-22', TRUE,  NOW(6)),
(3, 'Camila Rojas', 'camila@zaror.cl',    '+56933333333', '45',  '1995-11-02', TRUE,  NOW(6)),
(4, 'Luis Pérez',   'luis@zaror.cl',      '+56944444444', '8',   '1988-06-10', FALSE, NOW(6));
