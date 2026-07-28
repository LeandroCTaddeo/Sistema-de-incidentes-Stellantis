package ar.com.sistemaincidentes.api.firmantes;

import java.time.LocalDateTime;

public record FirmaExpedienteResponse(
        int firmanteId,
        int orden,
        String nombre,
        String areaLinea1,
        String areaLinea2,
        String planta,
        LocalDateTime fechaSeleccion
) {
}
