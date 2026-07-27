ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS activo BOOLEAN NOT NULL DEFAULT TRUE;

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_usuario_windows_lower
    ON usuarios (LOWER(usuario_windows));
