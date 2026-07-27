package ar.com.sistemaincidentes.api.incidentes;

import jakarta.validation.constraints.Positive;

public record AsignacionIncidenteRequest(
        @Positive(message = "El administrador no es válido.") int administradorId
) {
}
