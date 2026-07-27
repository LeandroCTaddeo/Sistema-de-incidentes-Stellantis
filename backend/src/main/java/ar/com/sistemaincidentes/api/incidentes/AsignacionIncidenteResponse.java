package ar.com.sistemaincidentes.api.incidentes;

import java.time.LocalDateTime;

public record AsignacionIncidenteResponse(
        int incidenteId,
        Integer administradorId,
        String nombreResponsable,
        LocalDateTime fechaAsignacion
) {
}
