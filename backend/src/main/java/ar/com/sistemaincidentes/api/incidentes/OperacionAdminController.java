package ar.com.sistemaincidentes.api.incidentes;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ar.com.sistemaincidentes.api.security.AuthenticatedUserGuard;

@RestController
@RequestMapping("/api/incidentes/{incidenteId}")
public class OperacionAdminController {

    private final OperacionAdminService service;
    private final AuthenticatedUserGuard authenticatedUserGuard;

    public OperacionAdminController(
            OperacionAdminService service,
            AuthenticatedUserGuard authenticatedUserGuard
    ) {
        this.service = service;
        this.authenticatedUserGuard = authenticatedUserGuard;
    }

    @PostMapping("/boletines")
    @ResponseStatus(HttpStatus.CREATED)
    public BoletinAdminGuardadoResponse crearBoletin(
            @PathVariable int incidenteId,
            @Valid @RequestBody BoletinAdminEscrituraRequest boletin
    ) {
        authenticatedUserGuard.validarUsuarioSolicitado(boletin.administradorId());
        return service.crearBoletin(incidenteId, boletin);
    }

    @PutMapping("/boletines/{boletinId}")
    public BoletinAdminGuardadoResponse actualizarBoletin(
            @PathVariable int incidenteId,
            @PathVariable int boletinId,
            @Valid @RequestBody BoletinAdminEscrituraRequest boletin
    ) {
        authenticatedUserGuard.validarUsuarioSolicitado(boletin.administradorId());
        return service.actualizarBoletin(incidenteId, boletinId, boletin);
    }

    @PatchMapping("/resolucion")
    public IncidenteResueltoResponse resolver(
            @PathVariable int incidenteId,
            @Valid @RequestBody ResolucionIncidenteRequest solicitud
    ) {
        authenticatedUserGuard.validarUsuarioSolicitado(solicitud.administradorId());
        return service.resolver(incidenteId, solicitud);
    }

    @PostMapping("/asignacion")
    public AsignacionIncidenteResponse tomar(
            @PathVariable int incidenteId,
            @Valid @RequestBody AsignacionIncidenteRequest solicitud
    ) {
        authenticatedUserGuard.validarUsuarioSolicitado(solicitud.administradorId());
        return service.tomar(incidenteId, solicitud);
    }

    @DeleteMapping("/asignacion")
    public AsignacionIncidenteResponse liberar(
            @PathVariable int incidenteId,
            @Valid @RequestBody AsignacionIncidenteRequest solicitud
    ) {
        authenticatedUserGuard.validarUsuarioSolicitado(solicitud.administradorId());
        return service.liberar(incidenteId, solicitud);
    }
}
