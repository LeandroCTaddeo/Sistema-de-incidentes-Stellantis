package api;

import java.time.LocalDate;
import java.time.LocalDateTime;

import models.BoletinAdmin;

public record BoletinAdminApiResponse(
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

    public BoletinAdmin convertir() {
        BoletinAdmin boletin = new BoletinAdmin();
        boletin.setId(id);
        boletin.setIncidenteId(incidenteId);
        boletin.setAdministradorId(administradorId);
        boletin.setTitulo(titulo);
        boletin.setDescripcion(descripcion);
        boletin.setFechaRegistro(fechaRegistro);
        boletin.setFechaEmision(fechaEmision);
        boletin.setLugar(lugar);
        boletin.setNombreApellido(nombreApellido);
        boletin.setCargo(cargo);
        boletin.setMatricula(matricula);
        boletin.setDni(dni);
        boletin.setArea(area);
        boletin.setSuperiorInmediato(superiorInmediato);
        boletin.setHistorial(historial);
        boletin.setPrioridad(prioridad);
        boletin.setFechaCreacion(fechaCreacion);
        return boletin;
    }
}
