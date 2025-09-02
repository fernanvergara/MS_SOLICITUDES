-- Creación de la tabla 'estados'
-- Esta tabla almacena los posibles estados que puede tener una solicitud
CREATE TABLE IF NOT EXISTS estado (
    id_estado SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);

-- Creación de la tabla 'tipo_prestamo'
-- Esta tabla almacena los diferentes tipos de préstamos disponibles
CREATE TABLE IF NOT EXISTS tipo_prestamo (
    id_tipo_prestamo SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    monto_minimo NUMERIC(15, 2) NOT NULL,
    monto_maximo NUMERIC(15, 2) NOT NULL,
    tasa_interes NUMERIC(5, 2) NOT NULL,
    validacion_automatica BOOLEAN NOT NULL DEFAULT FALSE,
    CHECK (monto_maximo > monto_minimo)
);

-- Creación de la tabla 'solicitud'
-- Esta tabla es la principal y contiene los datos de la solicitud de préstamo
-- Referencia a las tablas 'estados' y 'tipo_prestamo' a través de claves foráneas
CREATE TABLE IF NOT EXISTS solicitud (
    id_solicitud BIGSERIAL PRIMARY KEY,
    documento_identidad VARCHAR(15) NOT NULL,
    monto NUMERIC(15, 2) NOT NULL,
    plazo_maximo_dias INT NOT NULL,
    fecha_solicitud DATE DEFAULT CURRENT_DATE,
    id_estado INT NOT NULL,
    id_tipo_prestamo INT NOT NULL,
    CONSTRAINT fk_solicitud_estado FOREIGN KEY (id_estado)
        REFERENCES estados (id_estado),
    CONSTRAINT fk_solicitud_tipo_prestamo FOREIGN KEY (id_tipo_prestamo)
        REFERENCES tipo_prestamo (id_tipo_prestamo)
);

-- Opcional: Inserción de datos iniciales para las tablas de catálogo
INSERT INTO estado (nombre) VALUES
    ('Pendiente', 'La solicitud está en espera de revisión.'),
    ('Revision Manual', 'La solicitud esta siendo revisada por un asesor.'),
    ('Aprobada', 'La solicitud ha sido aprobada.'),
    ('Rechazada', 'La solicitud ha sido rechazada.');

INSERT INTO tipo_prestamo (nombre, monto_minimo, monto_maximo, tasa_interes, validacion_automatica) VALUES
    ('Préstamo Personal', 1000.00, 50000.00, 15.50, TRUE),
    ('Préstamo Hipotecario', 50000.00, 5000000.00, 8.75, TRUE),
    ('Préstamo Automotriz', 5000.00, 100000.00, 10.25, FALSE),
    ('Préstamo Educativo', 2000.00, 200000.00, 7.50, TRUE),
    ('Préstamo Empresarial', 15000.00, 1000000.00, 12.00, FALSE);
