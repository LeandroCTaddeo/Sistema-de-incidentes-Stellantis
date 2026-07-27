package ar.com.sistemaincidentes.api.incidentes;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import ar.com.sistemaincidentes.api.security.AuthenticatedUserGuard;

@RestController
@RequestMapping("/api/incidentes")
public class IncidenteController {

    private final IncidenteConsultaService service;
    private final IncidenteCreacionService creacionService;
    private final AuthenticatedUserGuard authenticatedUserGuard;

    public IncidenteController(
            IncidenteConsultaService service,
            IncidenteCreacionService creacionService,
            AuthenticatedUserGuard authenticatedUserGuard
    ) {
        this.service = service;
        this.creacionService = creacionService;
        this.authenticatedUserGuard = authenticatedUserGuard;
    }

    @GetMapping
    public List<IncidenteResponse> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String texto,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer asignadoA
    ) {
        if (asignadoA != null) {
            return service.listarAsignados(estado, texto, desde, hasta, asignadoA);
        }
        return service.listar(estado, texto, desde, hasta);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenteCreadoResponse crear(
            @Valid @RequestPart("incidente") IncidenteCreacionRequest incidente,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes
    ) {
        authenticatedUserGuard.validarUsuarioSolicitado(incidente.usuarioId());
        return creacionService.crear(incidente, imagenes);
    }
}
