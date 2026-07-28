package ar.com.sistemaincidentes.api.firmantes;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/firmantes")
public class FirmanteController {

    private final FirmanteService service;

    public FirmanteController(FirmanteService service) {
        this.service = service;
    }

    @GetMapping
    public List<FirmanteResponse> listar(
            @RequestParam(defaultValue = "false") boolean incluirInactivos
    ) {
        return service.listar(incluirInactivos);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FirmanteResponse crear(@RequestBody FirmanteGuardarRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public FirmanteResponse actualizar(
            @PathVariable int id,
            @RequestBody FirmanteGuardarRequest request
    ) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public FirmanteResponse cambiarEstado(
            @PathVariable int id,
            @RequestBody FirmanteEstadoRequest request
    ) {
        return service.cambiarEstado(id, request);
    }
}
