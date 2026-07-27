package ar.com.sistemaincidentes.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ar.com.sistemaincidentes.api.usuarios.UsuarioResponse;
import ar.com.sistemaincidentes.api.usuarios.UsuarioService;

@Component
public class AuthenticatedUserGuard {

    private final UsuarioService usuarioService;
    private final boolean identidadDesdePrincipal;

    public AuthenticatedUserGuard(
            UsuarioService usuarioService,
            @Value("${api.security.identity-from-principal:false}") boolean identidadDesdePrincipal
    ) {
        this.usuarioService = usuarioService;
        this.identidadDesdePrincipal = identidadDesdePrincipal;
    }

    public void validarUsuarioSolicitado(int usuarioId) {
        if (!identidadDesdePrincipal) return;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IdentidadNoCoincideException(
                    "No se pudo determinar la identidad corporativa actual."
            );
        }

        UsuarioResponse actual = usuarioService.obtenerActual(authentication.getName());
        if (actual.id() != usuarioId) {
            throw new IdentidadNoCoincideException(
                    "La identidad corporativa no coincide con el usuario de la operación."
            );
        }
    }
}
