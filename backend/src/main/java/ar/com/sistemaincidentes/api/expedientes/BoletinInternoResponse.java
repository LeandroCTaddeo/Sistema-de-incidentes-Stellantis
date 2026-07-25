package ar.com.sistemaincidentes.api.expedientes;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BoletinInternoResponse(
        int id,
        int incidenteId,
        int administradorId,
        String titulo,
        String descripcion,
        LocalDate fechaRegistro,
        LocalDate fechaEmision,
        String lugar,
        String nombreApellido,
        String cargo,
        String matricula,
        String dni,
        String area,
        String superiorInmediato,
        String historial,
        String prioridad,
        LocalDateTime fechaCreacion
) {
}
