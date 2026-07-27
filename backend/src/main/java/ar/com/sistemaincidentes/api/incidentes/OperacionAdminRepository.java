package ar.com.sistemaincidentes.api.incidentes;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OperacionAdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public OperacionAdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existeAdministrador(int administradorId) {
        Boolean existe = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM usuarios WHERE id = ? AND UPPER(rol) = 'ADMIN' AND activo = TRUE)",
                Boolean.class,
                administradorId
        );
        return Boolean.TRUE.equals(existe);
    }

    public String obtenerEstadoIncidente(int incidenteId) {
        return jdbcTemplate.query(
                "SELECT estado FROM incidentes WHERE id = ?",
                (rs, fila) -> rs.getString("estado"),
                incidenteId
        ).stream().findFirst().orElse(null);
    }

    public Integer obtenerResponsable(int incidenteId) {
        return jdbcTemplate.query(
                "SELECT asignado_a FROM incidentes WHERE id = ?",
                (rs, fila) -> {
                    int valor = rs.getInt("asignado_a");
                    return rs.wasNull() ? null : valor;
                },
                incidenteId
        ).stream().findFirst().orElse(null);
    }

    public boolean tomarIncidente(int incidenteId, int administradorId) {
        return jdbcTemplate.update(
                """
                UPDATE incidentes
                SET asignado_a = ?, fecha_asignacion = CURRENT_TIMESTAMP
                WHERE id = ? AND estado <> 'RESUELTO' AND asignado_a IS NULL
                """,
                administradorId,
                incidenteId
        ) == 1;
    }

    public boolean liberarIncidente(int incidenteId, int administradorId) {
        return jdbcTemplate.update(
                """
                UPDATE incidentes
                SET asignado_a = NULL, fecha_asignacion = NULL
                WHERE id = ? AND estado <> 'RESUELTO' AND asignado_a = ?
                """,
                incidenteId,
                administradorId
        ) == 1;
    }

    public void registrarAsignacion(int incidenteId, int administradorId, String accion) {
        jdbcTemplate.update(
                """
                INSERT INTO incidente_asignaciones (incidente_id, administrador_id, accion)
                VALUES (?, ?, ?)
                """,
                incidenteId,
                administradorId,
                accion
        );
    }

    public Optional<AsignacionIncidenteResponse> obtenerAsignacion(int incidenteId) {
        return jdbcTemplate.query(
                """
                SELECT i.id, i.asignado_a, u.nombre, i.fecha_asignacion
                FROM incidentes i
                LEFT JOIN usuarios u ON u.id = i.asignado_a
                WHERE i.id = ?
                """,
                (rs, fila) -> {
                    int asignado = rs.getInt("asignado_a");
                    Integer administradorId = rs.wasNull() ? null : asignado;
                    Timestamp fecha = rs.getTimestamp("fecha_asignacion");
                    LocalDateTime fechaAsignacion = fecha == null ? null : fecha.toLocalDateTime();
                    return new AsignacionIncidenteResponse(
                            rs.getInt("id"), administradorId, rs.getString("nombre"), fechaAsignacion
                    );
                },
                incidenteId
        ).stream().findFirst();
    }

    public int insertarBoletin(int incidenteId, BoletinAdminEscrituraRequest boletin) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conexion -> {
            PreparedStatement statement = conexion.prepareStatement(
                    """
                    INSERT INTO boletines_admin
                    (
                        incidente_id, administrador_id, titulo, descripcion,
                        fecha_registro, fecha_emision, lugar, nombre_apellido,
                        cargo, matricula, dni, area, superior_inmediato,
                        historial, prioridad
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    new String[] { "id" }
            );
            cargarCampos(statement, incidenteId, boletin);
            return statement;
        }, keyHolder);

        Number clave = keyHolder.getKey();
        if (clave == null || clave.intValue() <= 0) {
            throw new IllegalStateException("La base de datos no devolvió el boletín creado.");
        }
        return clave.intValue();
    }

    public boolean actualizarBoletin(
            int incidenteId,
            int boletinId,
            BoletinAdminEscrituraRequest boletin
    ) {
        int filas = jdbcTemplate.update(
                """
                UPDATE boletines_admin
                SET administrador_id = ?, titulo = ?, descripcion = ?,
                    fecha_registro = ?, fecha_emision = ?, lugar = ?,
                    nombre_apellido = ?, cargo = ?, matricula = ?, dni = ?,
                    area = ?, superior_inmediato = ?, historial = ?, prioridad = ?
                WHERE id = ? AND incidente_id = ?
                """,
                boletin.administradorId(), limpiar(boletin.titulo()), limpiar(boletin.descripcion()),
                boletin.fechaRegistro(), boletin.fechaEmision(), limpiar(boletin.lugar()),
                limpiar(boletin.nombreApellido()), limpiar(boletin.cargo()),
                limpiar(boletin.matricula()), limpiar(boletin.dni()), limpiar(boletin.area()),
                limpiar(boletin.superiorInmediato()), limpiar(boletin.historial()),
                boletin.prioridad().name(), boletinId, incidenteId
        );
        return filas == 1;
    }

    public boolean resolverIncidente(int incidenteId, int administradorId) {
        int filas = jdbcTemplate.update(
                """
                UPDATE incidentes
                SET estado = 'RESUELTO', fecha_resolucion = CURRENT_TIMESTAMP, resuelto_por = ?
                WHERE id = ? AND estado <> 'RESUELTO' AND asignado_a = ?
                """,
                administradorId,
                incidenteId,
                administradorId
        );
        return filas == 1;
    }

    private void cargarCampos(
            PreparedStatement statement,
            int incidenteId,
            BoletinAdminEscrituraRequest boletin
    ) throws java.sql.SQLException {
        statement.setInt(1, incidenteId);
        statement.setInt(2, boletin.administradorId());
        statement.setString(3, limpiar(boletin.titulo()));
        statement.setString(4, limpiar(boletin.descripcion()));
        statement.setObject(5, boletin.fechaRegistro());
        statement.setObject(6, boletin.fechaEmision());
        statement.setString(7, limpiar(boletin.lugar()));
        statement.setString(8, limpiar(boletin.nombreApellido()));
        statement.setString(9, limpiar(boletin.cargo()));
        statement.setString(10, limpiar(boletin.matricula()));
        statement.setString(11, limpiar(boletin.dni()));
        statement.setString(12, limpiar(boletin.area()));
        statement.setString(13, limpiar(boletin.superiorInmediato()));
        statement.setString(14, limpiar(boletin.historial()));
        statement.setString(15, boletin.prioridad().name());
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
