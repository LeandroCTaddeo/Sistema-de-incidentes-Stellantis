package api;

import java.time.LocalDateTime;

public record FirmaExpedienteApiResponse(
        int firmanteId,
        int orden,
        String nombre,
        String areaLinea1,
        String areaLinea2,
        String planta,
        LocalDateTime fechaSeleccion
) {
}
