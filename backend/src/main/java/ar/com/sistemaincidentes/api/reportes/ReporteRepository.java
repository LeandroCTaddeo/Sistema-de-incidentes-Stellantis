package ar.com.sistemaincidentes.api.reportes;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ReporteRepository {

    private final JdbcTemplate jdbcTemplate;

    public ReporteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ResumenReporteResponse obtenerResumen(LocalDate desde, LocalDate hasta) {
        return jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE i.estado = 'PENDIENTE') AS pendientes,
                       COUNT(*) FILTER (WHERE i.estado = 'RESUELTO') AS resueltos,
                       COALESCE(AVG(
                           CASE WHEN i.fecha_resolucion IS NOT NULL
                                THEN EXTRACT(EPOCH FROM (i.fecha_resolucion - i.fecha)) / 3600
                           END
                       ), 0) AS horas_promedio
                FROM incidentes i
                WHERE i.fecha >= ? AND i.fecha < ?
                """,
                (rs, fila) -> new ResumenReporteResponse(
                        rs.getLong("total"),
                        rs.getLong("pendientes"),
                        rs.getLong("resueltos"),
                        rs.getDouble("horas_promedio")
                ),
                inicio(desde), finExclusivo(hasta)
        );
    }

    public List<DatoConteoResponse> obtenerPorArea(LocalDate desde, LocalDate hasta) {
        return jdbcTemplate.query(
                """
                SELECT COALESCE(NULLIF(TRIM(i.area), ''),
                                NULLIF(TRIM(u.sector), ''), 'Sin área') AS nombre,
                       COUNT(*) AS cantidad
                FROM incidentes i
                JOIN usuarios u ON u.id = i.usuario_id
                WHERE i.fecha >= ? AND i.fecha < ?
                GROUP BY 1
                ORDER BY cantidad DESC, nombre ASC
                LIMIT 10
                """,
                (rs, fila) -> new DatoConteoResponse(
                        rs.getString("nombre"), rs.getLong("cantidad")
                ),
                inicio(desde), finExclusivo(hasta)
        );
    }

    public List<DatoConteoResponse> obtenerPorPrioridad(LocalDate desde, LocalDate hasta) {
        return jdbcTemplate.query(
                """
                SELECT COALESCE(i.prioridad, 'SIN PRIORIDAD') AS nombre,
                       COUNT(*) AS cantidad
                FROM incidentes i
                WHERE i.fecha >= ? AND i.fecha < ?
                GROUP BY i.prioridad
                ORDER BY cantidad DESC
                """,
                (rs, fila) -> new DatoConteoResponse(
                        rs.getString("nombre"), rs.getLong("cantidad")
                ),
                inicio(desde), finExclusivo(hasta)
        );
    }

    private Timestamp inicio(LocalDate fecha) {
        return Timestamp.valueOf(fecha.atStartOfDay());
    }

    private Timestamp finExclusivo(LocalDate fecha) {
        return Timestamp.valueOf(fecha.plusDays(1).atStartOfDay());
    }
}
