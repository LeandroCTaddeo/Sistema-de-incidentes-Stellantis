package api;

import java.time.LocalDate;
import java.time.LocalDateTime;

import models.Incidente;
import models.Prioridad;

public record IncidenteApiResponse(
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

    public Incidente convertir() {
        return new Incidente(
                id,
                titulo,
                descripcion,
                Prioridad.valueOf(prioridad),
                estado,
                usuarioId,
                nombreEmpleado,
                sector,
                fecha == null ? null : fecha.toString(),
                fechaRegistro,
                fechaEmision,
                lugar,
                nombreApellido,
                cargo,
                matricula,
                dni,
                area,
                superiorInmediato,
                historial,
                fechaResolucion,
                resueltoPor,
                asignadoA,
                nombreResponsable,
                fechaAsignacion
        );
    }
}
