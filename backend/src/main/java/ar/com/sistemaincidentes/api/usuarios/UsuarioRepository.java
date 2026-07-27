package ar.com.sistemaincidentes.api.usuarios;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository {

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
}
