package ar.com.sistemaincidentes.api.incidentes;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidentes/{incidenteId}")
public class OperacionAdminController {

    private final OperacionAdminService service;

    public OperacionAdminController(OperacionAdminService service) {
        this.service = service;
    }

    @PostMapping("/boletines")
    @ResponseStatus(HttpStatus.CREATED)
    public BoletinAdminGuardadoResponse crearBoletin(
            @PathVariable int incidenteId,
            @Valid @RequestBody BoletinAdminEscrituraRequest boletin
    ) {
        return service.crearBoletin(incidenteId, boletin);
    }

    @PutMapping("/boletines/{boletinId}")
    public BoletinAdminGuardadoResponse actualizarBoletin(
            @PathVariable int incidenteId,
            @PathVariable int boletinId,
            @Valid @RequestBody BoletinAdminEscrituraRequest boletin
    ) {
        return service.actualizarBoletin(incidenteId, boletinId, boletin);
    }

    @PatchMapping("/resolucion")
    public IncidenteResueltoResponse resolver(
            @PathVariable int incidenteId,
            @Valid @RequestBody ResolucionIncidenteRequest solicitud
    ) {
        return service.resolver(incidenteId, solicitud);
    }
}
