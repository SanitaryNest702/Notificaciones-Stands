--- Datos de ejemplo (los mismos stands y editoriales de la demo en memoria).
-- Se puede ejecutar varias veces: si el registro ya existe, se ignora.
-- Precio = metros cuadrados x $1,500 (precioPorMetroCuadrado de configuracion.properties).
-- Los pagos no estan en la base de datos en esta fase, solo las reservaciones.

INSERT INTO stands (id, metros_cuadrados, precio, estado) VALUES
    ('A-01',  9, 13500.00, 'OCUPADO'),
    ('A-02',  9, 13500.00, 'RESERVADO'),
    ('A-03', 12, 18000.00, 'RESERVADO'),
    ('A-04', 12, 18000.00, 'DISPONIBLE'),
    ('B-01', 18, 27000.00, 'OCUPADO'),
    ('B-02', 18, 27000.00, 'RESERVADO'),
    ('B-03', 24, 36000.00, 'BLOQUEADO'),
    ('B-04', 36, 54000.00, 'RESERVADO')
ON CONFLICT (id) DO NOTHING;

INSERT INTO editoriales (id, nombre, correo) VALUES
    ('E-1', 'Editorial Horizonte',    'contacto@horizonte.example'),
    ('E-2', 'Ediciones del Sur',      'ventas@delsur.example'),
    ('E-3', 'Libros Peninsulares',    'info@peninsulares.example'),
    ('E-4', 'Editoriales del Bombo',  'info@bombo.example')
ON CONFLICT (id) DO NOTHING;

INSERT INTO reservaciones (id, stand_id, editorial_id, fecha_reserva, fecha_vigencia, activa) VALUES
    ('R-1', 'A-01', 'E-1', '2026-10-31', '2026-11-15', TRUE),
    ('R-2', 'A-02', 'E-2', '2026-11-02', '2026-11-17', TRUE),
    ('R-3', 'B-01', 'E-3', '2026-11-05', '2026-11-20', TRUE),
    ('R-4', 'A-03', 'E-1', '2026-11-11', '2026-11-26', TRUE),
    ('R-5', 'B-02', 'E-2', '2026-11-14', '2026-11-29', TRUE),
    ('R-6', 'B-04', 'E-4', '2026-11-14', '2026-11-29', TRUE)
ON CONFLICT (id) DO NOTHING;
