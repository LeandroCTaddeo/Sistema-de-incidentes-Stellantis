package ar.com.sistemaincidentes.api.reportes;

import java.util.List;

public record ReporteResponse(
        ResumenReporteResponse resumen,
        List<DatoConteoResponse> areas,
        List<DatoConteoResponse> prioridades
) {
}
