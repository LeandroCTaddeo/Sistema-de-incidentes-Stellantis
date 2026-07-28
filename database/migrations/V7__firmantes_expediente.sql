CREATE TABLE IF NOT EXISTS firmantes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    area_linea_1 VARCHAR(150) NOT NULL,
    area_linea_2 VARCHAR(150) NOT NULL,
    planta VARCHAR(100) NOT NULL DEFAULT 'Palomar',
    obligatorio BOOLEAN NOT NULL DEFAULT FALSE,
    grupo_seleccion VARCHAR(80),
    orden SMALLINT NOT NULL CHECK (orden BETWEEN 1 AND 10),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_firmante_regla CHECK (
        (obligatorio = TRUE AND grupo_seleccion IS NULL)
        OR
        (obligatorio = FALSE AND grupo_seleccion IS NOT NULL)
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_firmantes_nombre_planta
    ON firmantes (LOWER(nombre), LOWER(planta));

CREATE INDEX IF NOT EXISTS idx_firmantes_planta_activo
    ON firmantes (planta, activo, orden);

INSERT INTO firmantes (
    nombre, area_linea_1, area_linea_2, planta,
    obligatorio, grupo_seleccion, orden, activo
)
SELECT
    'Guillermo Taddeo', 'Security and Facilities', 'Palomar Plant', 'Palomar',
    FALSE, 'RESPONSABLE_PALOMAR', 1, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM firmantes
    WHERE LOWER(nombre) = LOWER('Guillermo Taddeo')
      AND LOWER(planta) = LOWER('Palomar')
);

INSERT INTO firmantes (
    nombre, area_linea_1, area_linea_2, planta,
    obligatorio, grupo_seleccion, orden, activo
)
SELECT
    'Latorre Julián', 'Security and Facilities', 'Palomar Plant', 'Palomar',
    FALSE, 'RESPONSABLE_PALOMAR', 1, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM firmantes
    WHERE LOWER(nombre) = LOWER('Latorre Julián')
      AND LOWER(planta) = LOWER('Palomar')
);

INSERT INTO firmantes (
    nombre, area_linea_1, area_linea_2, planta,
    obligatorio, grupo_seleccion, orden, activo
)
SELECT
    'Lopez Carlos Argentino', 'Security and Facilities', 'Argentina', 'Palomar',
    TRUE, NULL, 2, TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM firmantes
    WHERE LOWER(nombre) = LOWER('Lopez Carlos Argentino')
      AND LOWER(planta) = LOWER('Palomar')
);

CREATE TABLE IF NOT EXISTS expediente_firmantes (
    incidente_id INTEGER NOT NULL REFERENCES incidentes(id),
    orden SMALLINT NOT NULL,
    firmante_id INTEGER NOT NULL REFERENCES firmantes(id),
    nombre VARCHAR(150) NOT NULL,
    area_linea_1 VARCHAR(150) NOT NULL,
    area_linea_2 VARCHAR(150) NOT NULL,
    planta VARCHAR(100) NOT NULL,
    seleccionado_por INTEGER NOT NULL REFERENCES usuarios(id),
    fecha_seleccion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (incidente_id, orden)
);

CREATE INDEX IF NOT EXISTS idx_expediente_firmantes_firmante
    ON expediente_firmantes (firmante_id);
