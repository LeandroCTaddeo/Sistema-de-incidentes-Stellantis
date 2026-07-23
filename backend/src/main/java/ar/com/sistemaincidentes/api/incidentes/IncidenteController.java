package ar.com.sistemaincidentes.api.incidentes;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/incidentes")
public class IncidenteController {

    private final IncidenteConsultaService service;

    public IncidenteController(IncidenteConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public List<IncidenteResponse> listar(@RequestParam(required = false) String estado) {
        return service.listar(estado);
    }
}
