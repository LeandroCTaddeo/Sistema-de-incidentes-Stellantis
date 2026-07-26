package ar.com.sistemaincidentes.api.incidentes;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IncidenteCreacionRequest(
        @NotBlank @Size(max = 200) String titulo,
        @NotBlank @Size(max = 100000) String descripcion,
        @NotNull PrioridadIncidente prioridad,
        @Positive int usuarioId,
        @NotNull LocalDate fechaRegistro,
        @NotNull LocalDate fechaEmision,
        @NotBlank @Size(max = 150) String lugar,
        @NotBlank @Size(max = 150) String nombreApellido,
        @NotBlank @Size(max = 100) String cargo,
        @NotBlank @Pattern(regexp = "\\d{1,50}") String matricula,
        @NotBlank @Pattern(regexp = "\\d{1,50}") String dni,
        @NotBlank @Size(max = 100) String area,
        @NotBlank @Size(max = 150) String superiorInmediato,
        @NotBlank @Size(max = 100000) String historial
) {
}
