-- Esquema de la base de datos de Notificaciones-Stands.
-- Solo 3 tablas: stands, editoriales y reservaciones.
-- Es SQL estandar: funciona igual en PostgreSQL (Neon) y en SQLite.
-- Se puede ejecutar varias veces sin problema (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS stands (
    id               VARCHAR(10)   PRIMARY KEY,
    metros_cuadrados NUMERIC(6,2)  NOT NULL CHECK (metros_cuadrados > 0),
    precio           NUMERIC(10,2) NOT NULL CHECK (precio > 0),
    estado           VARCHAR(12)   NOT NULL
                     CHECK (estado IN ('DISPONIBLE', 'RESERVADO', 'OCUPADO', 'BLOQUEADO'))
);

CREATE TABLE IF NOT EXISTS editoriales (
    id     VARCHAR(10)  PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(120) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservaciones (
    id             VARCHAR(10) PRIMARY KEY,
    stand_id       VARCHAR(10) NOT NULL REFERENCES stands (id),
    editorial_id   VARCHAR(10) NOT NULL REFERENCES editoriales (id),
    fecha_reserva  DATE        NOT NULL,
    fecha_vigencia DATE        NOT NULL,
    activa         BOOLEAN     NOT NULL DEFAULT TRUE,
    CHECK (fecha_vigencia >= fecha_reserva)
);
