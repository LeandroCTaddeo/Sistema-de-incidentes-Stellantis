package ar.com.sistemaincidentes.api.incidentes;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.sistemaincidentes.api.web.ConflictoOperacionException;
import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

@Service
public class OperacionAdminService {

    private final OperacionAdminRepository repository;

    public OperacionAdminService(OperacionAdminRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public BoletinAdminGuardadoResponse crearBoletin(
            int incidenteId,
            BoletinAdminEscrituraRequest boletin
    ) {
        validarOperacion(incidenteId, boletin.administradorId());
        return new BoletinAdminGuardadoResponse(
                repository.insertarBoletin(incidenteId, boletin)
        );
    }

    @Transactional
    public BoletinAdminGuardadoResponse actualizarBoletin(
            int incidenteId,
            int boletinId,
            BoletinAdminEscrituraRequest boletin
    ) {
        validarId(boletinId, "boletín");
        validarOperacion(incidenteId, boletin.administradorId());
        if (!repository.actualizarBoletin(incidenteId, boletinId, boletin)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró el boletín interno dentro del expediente indicado."
            );
        }
        return new BoletinAdminGuardadoResponse(boletinId);
    }

    @Transactional
    public IncidenteResueltoResponse resolver(
            int incidenteId,
            ResolucionIncidenteRequest solicitud
    ) {
        validarId(incidenteId, "incidente");
        validarAdministrador(solicitud.administradorId());

        String estado = repository.obtenerEstadoIncidente(incidenteId);
        if (estado == null) {
            throw new RecursoNoEncontradoException("No se encontró el incidente solicitado.");
        }
        if ("RESUELTO".equalsIgnoreCase(estado)) {
            throw new ConflictoOperacionException("El expediente ya se encuentra resuelto.");
        }
        if (!repository.resolverIncidente(incidenteId, solicitud.administradorId())) {
            throw new ConflictoOperacionException(
                    "El expediente cambió de estado mientras se intentaba resolver."
            );
        }
        return new IncidenteResueltoResponse(incidenteId, "RESUELTO");
    }

    private void validarOperacion(int incidenteId, int administradorId) {
        validarId(incidenteId, "incidente");
        validarAdministrador(administradorId);
        String estado = repository.obtenerEstadoIncidente(incidenteId);
        if (estado == null) {
            throw new RecursoNoEncontradoException("No se encontró el incidente solicitado.");
        }
        if ("RESUELTO".equalsIgnoreCase(estado)) {
            throw new ConflictoOperacionException(
                    "No se pueden modificar boletines de un expediente resuelto."
            );
        }
    }

    private void validarAdministrador(int administradorId) {
        validarId(administradorId, "administrador");
        if (!repository.existeAdministrador(administradorId)) {
            throw new IllegalArgumentException(
                    "El administrador indicado no está registrado o no tiene ese rol."
            );
        }
    }

    private void validarId(int id, String nombre) {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador del " + nombre + " no es válido.");
        }
    }
}
