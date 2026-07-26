package ar.com.sistemaincidentes.api.incidentes;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/incidentes")
public class IncidenteController {

    private final IncidenteConsultaService service;
    private final IncidenteCreacionService creacionService;

    public IncidenteController(
            IncidenteConsultaService service,
            IncidenteCreacionService creacionService
    ) {
        this.service = service;
        this.creacionService = creacionService;
    }

    @GetMapping
    public List<IncidenteResponse> listar(@RequestParam(required = false) String estado) {
        return service.listar(estado);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenteCreadoResponse crear(
            @Valid @RequestPart("incidente") IncidenteCreacionRequest incidente,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes
    ) {
        return creacionService.crear(incidente, imagenes);
    }
}
