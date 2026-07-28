package ar.com.sistemaincidentes.api.firmantes;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidentes/{incidenteId}/firmas")
public class FirmaExpedienteController {

    private final FirmanteService service;

    public FirmaExpedienteController(FirmanteService service) {
        this.service = service;
    }

    @GetMapping
    public List<FirmaExpedienteResponse> obtener(@PathVariable int incidenteId) {
        return service.obtenerSeleccion(incidenteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<FirmaExpedienteResponse> seleccionar(
            @PathVariable int incidenteId,
            @RequestBody SeleccionFirmantesRequest request
    ) {
        return service.seleccionar(incidenteId, request);
    }
}
