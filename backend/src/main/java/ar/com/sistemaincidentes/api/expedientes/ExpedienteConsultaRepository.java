package ar.com.sistemaincidentes.api.expedientes;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ExpedienteConsultaRepository {

    private static final String CONSULTA_BOLETINES = """
            SELECT
                id, incidente_id, administrador_id, titulo, descripcion,
                fecha_registro, fecha_emision, lugar, nombre_apellido,
                cargo, matricula, dni, area, superior_inmediato,
                historial, prioridad, fecha_creacion
            FROM boletines_admin
            WHERE incidente_id = ?
            ORDER BY fecha_creacion ASC, id ASC
            """;

    private final JdbcTemplate jdbcTemplate;

    public ExpedienteConsultaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BoletinInternoResponse> listarBoletines(int incidenteId) {
        return jdbcTemplate.query(
                CONSULTA_BOLETINES,
                this::mapearBoletin,
                incidenteId
        );
    }

    public List<ImagenAdjuntaArchivo> listarImagenes(int incidenteId) {
        return jdbcTemplate.query(
                """
                SELECT id, incidente_id, ruta
                FROM imagenes
                WHERE incidente_id = ?
                ORDER BY id ASC
                """,
                this::mapearImagen,
                incidenteId
        );
    }

    public Optional<ImagenAdjuntaArchivo> buscarImagen(int incidenteId, int imagenId) {
        return jdbcTemplate.query(
                """
                SELECT id, incidente_id, ruta
                FROM imagenes
                WHERE incidente_id = ? AND id = ?
                """,
                this::mapearImagen,
                incidenteId,
                imagenId
        ).stream().findFirst();
    }

    private BoletinInternoResponse mapearBoletin(ResultSet rs, int fila) throws SQLException {
        return new BoletinInternoResponse(
                rs.getInt("id"),
                rs.getInt("incidente_id"),
                rs.getInt("administrador_id"),
                rs.getString("titulo"),
                rs.getString("descripcion"),
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
                rs.getString("prioridad"),
                obtenerFechaHora(rs, "fecha_creacion")
        );
    }

    private ImagenAdjuntaArchivo mapearImagen(ResultSet rs, int fila) throws SQLException {
        return new ImagenAdjuntaArchivo(
                rs.getInt("id"),
                rs.getInt("incidente_id"),
                rs.getString("ruta")
        );
    }

    private LocalDate obtenerFecha(ResultSet rs, String columna) throws SQLException {
        Date fecha = rs.getDate(columna);
        return fecha == null ? null : fecha.toLocalDate();
    }

    private LocalDateTime obtenerFechaHora(ResultSet rs, String columna) throws SQLException {
        Timestamp fecha = rs.getTimestamp(columna);
        return fecha == null ? null : fecha.toLocalDateTime();
    }
}
