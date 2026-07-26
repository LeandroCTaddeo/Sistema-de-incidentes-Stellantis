package ar.com.sistemaincidentes.api.incidentes;

import java.sql.PreparedStatement;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class IncidenteEscrituraRepository {

    private final JdbcTemplate jdbcTemplate;

    public IncidenteEscrituraRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeUsuario(int usuarioId) {
        Boolean existe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM usuarios WHERE id = ?)",
                Boolean.class,
                usuarioId
        );
        return Boolean.TRUE.equals(existe);
    }

    public int insertarIncidente(IncidenteCreacionRequest incidente) {
        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conexion -> {
            PreparedStatement statement = conexion.prepareStatement(
                    """
                    INSERT INTO incidentes
                    (
                        titulo, descripcion, prioridad, usuario_id,
                        fecha_registro, fecha_emision, lugar, nombre_apellido,
                        cargo, matricula, dni, area, superior_inmediato, historial
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[] { "id" }
            );
            statement.setString(1, incidente.titulo().trim());
            statement.setString(2, incidente.descripcion().trim());
            statement.setString(3, incidente.prioridad().name());
            statement.setInt(4, incidente.usuarioId());
            statement.setObject(5, incidente.fechaRegistro());
            statement.setObject(6, incidente.fechaEmision());
            statement.setString(7, incidente.lugar().trim());
            statement.setString(8, incidente.nombreApellido().trim());
            statement.setString(9, incidente.cargo().trim());
            statement.setString(10, incidente.matricula().trim());
            statement.setString(11, incidente.dni().trim());
            statement.setString(12, incidente.area().trim());
            statement.setString(13, incidente.superiorInmediato().trim());
            statement.setString(14, incidente.historial().trim());
            return statement;
        }, keyHolder);

        Number clave = keyHolder.getKey();
        if (clave == null || clave.intValue() <= 0) {
            throw new IllegalStateException("La base de datos no devolvió el incidente creado.");
        }
        return clave.intValue();
    }

    public void insertarImagen(int incidenteId, String rutaRelativa) {
        jdbcTemplate.update(
                "INSERT INTO imagenes (incidente_id, ruta) VALUES (?, ?)",
                incidenteId,
                rutaRelativa
        );
    }
}
