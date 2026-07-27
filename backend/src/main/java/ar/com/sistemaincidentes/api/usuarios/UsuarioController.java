package ar.com.sistemaincidentes.api.usuarios;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
}
