package dao;

import java.time.LocalDate;
import java.util.List;

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

    private ReporteApiClient apiClient() {
        if (apiClient == null) {
            apiClient = new ReporteApiClient();
        }
        return apiClient;
    }

    public ReporteCompleto obtenerReporte(LocalDate desde, LocalDate hasta) {
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
}
