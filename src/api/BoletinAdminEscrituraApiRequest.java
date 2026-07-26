package api;

import java.time.LocalDate;

import models.BoletinAdmin;

public record BoletinAdminEscrituraApiRequest(
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
        String prioridad
) {
    public static BoletinAdminEscrituraApiRequest desde(BoletinAdmin boletin) {
        return new BoletinAdminEscrituraApiRequest(
                boletin.getAdministradorId(),
                boletin.getTitulo(),
                boletin.getDescripcion(),
                boletin.getFechaRegistro(),
                boletin.getFechaEmision(),
                boletin.getLugar(),
                boletin.getNombreApellido(),
                boletin.getCargo(),
                boletin.getMatricula(),
                boletin.getDni(),
                boletin.getArea(),
                boletin.getSuperiorInmediato(),
                boletin.getHistorial(),
                boletin.getPrioridad()
        );
    }
}
