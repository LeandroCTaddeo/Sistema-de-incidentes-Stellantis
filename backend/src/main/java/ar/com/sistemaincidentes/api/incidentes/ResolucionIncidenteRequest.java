package ar.com.sistemaincidentes.api.incidentes;

import jakarta.validation.constraints.Positive;

public record ResolucionIncidenteRequest(@Positive int administradorId) {
}
