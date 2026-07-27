package api;

public record ResumenReporteApiResponse(
        long total,
        long pendientes,
        long resueltos,
        double horasPromedio
) {
}
