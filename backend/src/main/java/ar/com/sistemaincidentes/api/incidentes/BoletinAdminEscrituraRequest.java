package ar.com.sistemaincidentes.api.incidentes;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BoletinAdminEscrituraRequest(
        @Positive int administradorId,
        @NotBlank @Size(max = 255) String titulo,
        String descripcion,
        LocalDate fechaRegistro,
        LocalDate fechaEmision,
        @Size(max = 255) String lugar,
        @Size(max = 255) String nombreApellido,
        @Size(max = 150) String cargo,
        @Size(max = 100) String matricula,
        @Size(max = 50) String dni,
        @Size(max = 150) String area,
        @Size(max = 255) String superiorInmediato,
        String historial,
        @NotNull PrioridadIncidente prioridad
) {
}
