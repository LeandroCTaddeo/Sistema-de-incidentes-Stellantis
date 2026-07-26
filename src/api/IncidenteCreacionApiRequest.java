package api;

import java.time.LocalDate;

import models.Incidente;

public record IncidenteCreacionApiRequest(
        String titulo,
        String descripcion,
        String prioridad,
        int usuarioId,
        LocalDate fechaRegistro,
        LocalDate fechaEmision,
        String lugar,
        String nombreApellido,
        String cargo,
        String matricula,
        String dni,
        String area,
        String superiorInmediato,
        String historial
) {

    public static IncidenteCreacionApiRequest desde(Incidente incidente) {
        return new IncidenteCreacionApiRequest(
                incidente.getTitulo(),
                incidente.getDescripcion(),
                incidente.getPrioridad().name(),
                incidente.getUsuarioId(),
                incidente.getFechaRegistro(),
                incidente.getFechaEmision(),
                incidente.getLugar(),
                incidente.getNombreApellido(),
                incidente.getCargo(),
                incidente.getMatricula(),
                incidente.getDni(),
                incidente.getArea(),
                incidente.getSuperiorInmediato(),
                incidente.getHistorial()
        );
    }
}
