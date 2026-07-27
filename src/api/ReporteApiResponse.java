package api;

import java.util.List;

public record ReporteApiResponse(
        ResumenReporteApiResponse resumen,
        List<DatoConteoApiResponse> areas,
        List<DatoConteoApiResponse> prioridades
) {
}
