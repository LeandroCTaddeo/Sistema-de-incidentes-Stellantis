package ar.com.sistemaincidentes.api.incidentes;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IncidenteConsultaRepository {

    private static final String CONSULTA_BASE = """
            SELECT
                i.id,
                i.titulo,
                i.descripcion,
                i.prioridad,
                i.estado,
                i.usuario_id,
                u.nombre AS nombre_empleado,
                u.sector,
                i.fecha,
                i.fecha_registro,
                i.fecha_emision,
                i.lugar,
                i.nombre_apellido,
                i.cargo,
                i.matricula,
                i.dni,
                i.area,
                i.superior_inmediato,
                i.historial,
                i.fecha_resolucion,
                i.resuelto_por
            FROM incidentes i
            JOIN usuarios u ON u.id = i.usuario_id
            """;

    private final JdbcTemplate jdbcTemplate;

    public IncidenteConsultaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<IncidenteResponse> listar(EstadoIncidente estado) {
        if (estado == null) {
            return jdbcTemplate.query(
                    CONSULTA_BASE + " ORDER BY i.id DESC",
                    this::mapear
            );
        }

        return jdbcTemplate.query(
                CONSULTA_BASE + " WHERE i.estado = ? ORDER BY i.id DESC",
                this::mapear,
                estado.name()
        );
    }

    private IncidenteResponse mapear(ResultSet rs, int numeroFila) throws SQLException {
        return new IncidenteResponse(
                rs.getInt("id"),
                rs.getString("titulo"),
                rs.getString("descripcion"),
                rs.getString("prioridad"),
                rs.getString("estado"),
                rs.getInt("usuario_id"),
                rs.getString("nombre_empleado"),
                rs.getString("sector"),
                obtenerFechaHora(rs, "fecha"),
                obtenerFecha(rs, "fecha_registro"),
                obtenerFecha(rs, "fecha_emision"),
                rs.getString("lugar"),
                rs.getString("nombre_apellido"),
                rs.getString("cargo"),
                rs.getString("matricula"),
                rs.getString("dni"),
                rs.getString("area"),
                rs.getString("superior_inmediato"),
                rs.getString("historial"),
                obtenerFechaHora(rs, "fecha_resolucion"),
                obtenerEntero(rs, "resuelto_por")
        );
    }

    private LocalDate obtenerFecha(ResultSet rs, String columna) throws SQLException {
        java.sql.Date fecha = rs.getDate(columna);
        return fecha == null ? null : fecha.toLocalDate();
    }

    private LocalDateTime obtenerFechaHora(ResultSet rs, String columna) throws SQLException {
        Timestamp fecha = rs.getTimestamp(columna);
        return fecha == null ? null : fecha.toLocalDateTime();
    }

    private Integer obtenerEntero(ResultSet rs, String columna) throws SQLException {
        int valor = rs.getInt(columna);
        return rs.wasNull() ? null : valor;
    }
}
