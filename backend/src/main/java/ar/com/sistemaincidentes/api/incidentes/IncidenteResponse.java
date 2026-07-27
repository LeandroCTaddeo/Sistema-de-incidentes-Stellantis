package ar.com.sistemaincidentes.api.incidentes;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IncidenteResponse(
        int id,
        String titulo,
        String descripcion,
        String prioridad,
        String estado,
        int usuarioId,
        String nombreEmpleado,
        String sector,
        LocalDateTime fecha,
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
        LocalDateTime fechaResolucion,
        Integer resueltoPor,
        Integer asignadoA,
        String nombreResponsable,
        LocalDateTime fechaAsignacion
) {
    public IncidenteResponse(
            int id,
            String titulo,
            String descripcion,
            String prioridad,
            String estado,
            int usuarioId,
            String nombreEmpleado,
            String sector,
            LocalDateTime fecha,
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
            LocalDateTime fechaResolucion,
            Integer resueltoPor
    ) {
        this(
                id, titulo, descripcion, prioridad, estado, usuarioId,
                nombreEmpleado, sector, fecha, fechaRegistro, fechaEmision,
                lugar, nombreApellido, cargo, matricula, dni, area,
                superiorInmediato, historial, fechaResolucion, resueltoPor,
                null, null, null
        );
    }
}
