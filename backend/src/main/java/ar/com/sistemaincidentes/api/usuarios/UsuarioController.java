package ar.com.sistemaincidentes.api.usuarios;

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
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @GetMapping("/actual")
    public UsuarioResponse obtenerActual(@RequestParam String usuarioWindows) {
        return service.obtenerActual(usuarioWindows);
    }

    @GetMapping
    public List<UsuarioAdministracionResponse> listar(
            @RequestParam(required = false) String buscar
    ) {
        return service.listar(buscar);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioAdministracionResponse crear(@RequestBody UsuarioGuardarRequest request) {
        return service.crear(request);
    }

    @PutMapping("/{id}")
    public UsuarioAdministracionResponse actualizar(
            @PathVariable int id,
            @RequestBody UsuarioGuardarRequest request
    ) {
        return service.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    public UsuarioAdministracionResponse cambiarEstado(
            @PathVariable int id,
            @RequestBody UsuarioEstadoRequest request
    ) {
        return service.cambiarEstado(id, request);
    }
}
