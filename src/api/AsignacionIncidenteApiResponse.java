package api;

import java.time.LocalDateTime;

public record AsignacionIncidenteApiResponse(
        int incidenteId,
        Integer administradorId,
        String nombreResponsable,
        LocalDateTime fechaAsignacion
) {
}
