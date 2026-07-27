package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import database.Conexion;
import api.ReporteApiClient;
import api.ReporteApiResponse;

public class ReporteDAO {

    public record Resumen(long total, long pendientes, long resueltos, double horasPromedio) {}
    public record DatoConteo(String nombre, long cantidad) {}
    public record ReporteCompleto(
            Resumen resumen,
            List<DatoConteo> areas,
            List<DatoConteo> prioridades
    ) {}

    private ReporteApiClient apiClient;

    private boolean usarApi() {
        return "API".equalsIgnoreCase(
                System.getenv().getOrDefault("INCIDENTES_DATA_SOURCE", "JDBC")
        );
    }

    private ReporteApiClient apiClient() {
        if (apiClient == null) apiClient = new ReporteApiClient();
        return apiClient;
    }

    public ReporteCompleto obtenerReporte(LocalDate desde, LocalDate hasta) {
        if (!usarApi()) {
            return new ReporteCompleto(
                    obtenerResumen(desde, hasta),
                    obtenerPorArea(desde, hasta),
                    obtenerPorPrioridad(desde, hasta)
            );
        }

        ReporteApiResponse respuesta = apiClient().obtener(desde, hasta);
        Resumen resumen = new Resumen(
                respuesta.resumen().total(),
                respuesta.resumen().pendientes(),
                respuesta.resumen().resueltos(),
                respuesta.resumen().horasPromedio()
        );
        List<DatoConteo> areas = respuesta.areas().stream()
                .map(dato -> new DatoConteo(dato.nombre(), dato.cantidad()))
                .toList();
        List<DatoConteo> prioridades = respuesta.prioridades().stream()
                .map(dato -> new DatoConteo(dato.nombre(), dato.cantidad()))
                .toList();
        return new ReporteCompleto(resumen, areas, prioridades);
    }

    public Resumen obtenerResumen(LocalDate desde, LocalDate hasta) {
        String sql = """
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
                """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            configurarPeriodo(ps, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Resumen(
                            rs.getLong("total"),
                            rs.getLong("pendientes"),
                            rs.getLong("resueltos"),
                            rs.getDouble("horas_promedio")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new Resumen(0, 0, 0, 0);
    }

    public List<DatoConteo> obtenerPorArea(LocalDate desde, LocalDate hasta) {
        String sql = """
                SELECT COALESCE(NULLIF(TRIM(i.area), ''), NULLIF(TRIM(u.sector), ''), 'Sin área') AS nombre,
                       COUNT(*) AS cantidad
                FROM incidentes i
                JOIN usuarios u ON u.id = i.usuario_id
                WHERE i.fecha >= ? AND i.fecha < ?
                GROUP BY 1
                ORDER BY cantidad DESC, nombre ASC
                LIMIT 10
                """;
        return consultarConteos(sql, desde, hasta);
    }

    public List<DatoConteo> obtenerPorPrioridad(LocalDate desde, LocalDate hasta) {
        String sql = """
                SELECT COALESCE(i.prioridad, 'SIN PRIORIDAD') AS nombre,
                       COUNT(*) AS cantidad
                FROM incidentes i
                WHERE i.fecha >= ? AND i.fecha < ?
                GROUP BY i.prioridad
                ORDER BY cantidad DESC
                """;
        return consultarConteos(sql, desde, hasta);
    }

    private List<DatoConteo> consultarConteos(String sql, LocalDate desde, LocalDate hasta) {
        List<DatoConteo> datos = new ArrayList<>();
        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            configurarPeriodo(ps, desde, hasta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    datos.add(new DatoConteo(rs.getString("nombre"), rs.getLong("cantidad")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datos;
    }

    private void configurarPeriodo(PreparedStatement ps, LocalDate desde, LocalDate hasta) throws Exception {
        ps.setTimestamp(1, Timestamp.valueOf(desde.atStartOfDay()));
        ps.setTimestamp(2, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
    }
}
