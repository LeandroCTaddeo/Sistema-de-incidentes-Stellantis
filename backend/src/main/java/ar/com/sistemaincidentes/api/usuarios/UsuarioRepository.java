package ar.com.sistemaincidentes.api.usuarios;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository {

    private static final RowMapper<UsuarioAdministracionResponse> MAPEADOR_ADMIN =
            (rs, fila) -> new UsuarioAdministracionResponse(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("usuario_windows"),
                    rs.getString("sector"),
                    rs.getString("rol"),
                    rs.getBoolean("activo"),
                    rs.getLong("casos_abiertos"),
                    rs.getLong("casos_resueltos")
            );

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UsuarioResponse> buscarPorUsuarioWindows(String usuarioWindows) {
        return jdbcTemplate.query(
                """
                SELECT id, nombre, rol
                FROM usuarios
                WHERE LOWER(usuario_windows) = LOWER(?)
                  AND activo = TRUE
                ORDER BY id ASC
                LIMIT 1
                """,
                (rs, fila) -> new UsuarioResponse(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rol")
                ),
                usuarioWindows
        ).stream().findFirst();
    }

    public List<UsuarioAdministracionResponse> listar(String busqueda) {
        String termino = busqueda == null ? "" : busqueda.trim().toLowerCase();
        if (termino.isBlank()) {
            return jdbcTemplate.query(
                    """
                    SELECT u.id, u.nombre, u.usuario_windows, COALESCE(u.sector, '') AS sector,
                           u.rol, u.activo,
                           (SELECT COUNT(*) FROM incidentes i
                            WHERE i.asignado_a = u.id AND i.estado <> 'RESUELTO') AS casos_abiertos,
                           (SELECT COUNT(*) FROM incidentes i
                            WHERE i.resuelto_por = u.id AND i.estado = 'RESUELTO') AS casos_resueltos
                    FROM usuarios
                    u
                    WHERE UPPER(u.rol) = 'ADMIN'
                    ORDER BY activo DESC, nombre ASC, id ASC
                    """,
                    MAPEADOR_ADMIN
            );
        }

        String patron = "%" + termino + "%";
        return jdbcTemplate.query(
                """
                SELECT u.id, u.nombre, u.usuario_windows, COALESCE(u.sector, '') AS sector,
                       u.rol, u.activo,
                       (SELECT COUNT(*) FROM incidentes i
                        WHERE i.asignado_a = u.id AND i.estado <> 'RESUELTO') AS casos_abiertos,
                       (SELECT COUNT(*) FROM incidentes i
                        WHERE i.resuelto_por = u.id AND i.estado = 'RESUELTO') AS casos_resueltos
                FROM usuarios u
                WHERE UPPER(u.rol) = 'ADMIN'
                  AND (LOWER(u.nombre) LIKE ?
                   OR LOWER(u.usuario_windows) LIKE ?
                   OR LOWER(COALESCE(u.sector, '')) LIKE ?)
                ORDER BY activo DESC, nombre ASC, id ASC
                """,
                MAPEADOR_ADMIN,
                patron, patron, patron
        );
    }

    public Optional<UsuarioAdministracionResponse> buscarPorId(int id) {
        return jdbcTemplate.query(
                """
                SELECT u.id, u.nombre, u.usuario_windows, COALESCE(u.sector, '') AS sector,
                       u.rol, u.activo,
                       (SELECT COUNT(*) FROM incidentes i
                        WHERE i.asignado_a = u.id AND i.estado <> 'RESUELTO') AS casos_abiertos,
                       (SELECT COUNT(*) FROM incidentes i
                        WHERE i.resuelto_por = u.id AND i.estado = 'RESUELTO') AS casos_resueltos
                FROM usuarios u
                WHERE u.id = ?
                """,
                MAPEADOR_ADMIN,
                id
        ).stream().findFirst();
    }

    public boolean existeUsuarioWindows(String usuarioWindows, Integer excluirId) {
        Long cantidad = excluirId == null
                ? jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM usuarios WHERE LOWER(usuario_windows) = LOWER(?)",
                        Long.class,
                        usuarioWindows
                )
                : jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM usuarios
                        WHERE LOWER(usuario_windows) = LOWER(?) AND id <> ?
                        """,
                        Long.class,
                        usuarioWindows,
                        excluirId
                );
        return cantidad != null && cantidad > 0;
    }

    public UsuarioAdministracionResponse crear(
            String nombre,
            String usuarioWindows,
            String sector,
            String rol
    ) {
        Integer id = jdbcTemplate.queryForObject(
                """
                INSERT INTO usuarios (nombre, usuario_windows, sector, rol, activo)
                VALUES (?, ?, ?, ?, TRUE)
                RETURNING id
                """,
                Integer.class,
                nombre,
                usuarioWindows,
                sector,
                rol
        );
        return buscarPorId(id == null ? 0 : id)
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar el usuario creado."));
    }

    public boolean actualizar(
            int id,
            String nombre,
            String usuarioWindows,
            String sector,
            String rol
    ) {
        return jdbcTemplate.update(
                """
                UPDATE usuarios
                SET nombre = ?, usuario_windows = ?, sector = ?, rol = ?
                WHERE id = ?
                """,
                nombre,
                usuarioWindows,
                sector,
                rol,
                id
        ) == 1;
    }

    public boolean actualizarEstado(int id, boolean activo) {
        return jdbcTemplate.update(
                "UPDATE usuarios SET activo = ? WHERE id = ?",
                activo,
                id
        ) == 1;
    }

    public long contarAdministradoresActivosExcepto(int id) {
        Long cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM usuarios
                WHERE rol = 'ADMIN' AND activo = TRUE AND id <> ?
                """,
                Long.class,
                id
        );
        return cantidad == null ? 0 : cantidad;
    }
}
