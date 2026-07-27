package ar.com.sistemaincidentes.api.reportes;

public record ResumenReporteResponse(
        long total,
        long pendientes,
        long resueltos,
        double horasPromedio
) {
}
