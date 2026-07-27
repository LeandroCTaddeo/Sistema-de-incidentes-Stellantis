ALTER TABLE incidentes
    ADD COLUMN IF NOT EXISTS asignado_a INTEGER REFERENCES usuarios(id),
    ADD COLUMN IF NOT EXISTS fecha_asignacion TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_incidentes_asignado_estado
    ON incidentes (asignado_a, estado);

CREATE TABLE IF NOT EXISTS incidente_asignaciones (
    id BIGSERIAL PRIMARY KEY,
    incidente_id INTEGER NOT NULL REFERENCES incidentes(id),
    administrador_id INTEGER NOT NULL REFERENCES usuarios(id),
    accion VARCHAR(20) NOT NULL CHECK (accion IN ('TOMADO', 'LIBERADO')),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_incidente_asignaciones_incidente
    ON incidente_asignaciones (incidente_id, fecha DESC);
