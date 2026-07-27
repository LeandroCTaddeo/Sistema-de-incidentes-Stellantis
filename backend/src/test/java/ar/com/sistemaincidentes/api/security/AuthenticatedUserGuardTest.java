package ar.com.sistemaincidentes.api.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import ar.com.sistemaincidentes.api.usuarios.UsuarioResponse;
import ar.com.sistemaincidentes.api.usuarios.UsuarioService;

class AuthenticatedUserGuardTest {

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rechazaOperarConElIdentificadorDeOtroUsuario() {
        UsuarioService usuarioService = mock(UsuarioService.class);
        AuthenticatedUserGuard guard = new AuthenticatedUserGuard(usuarioService, true);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                "usuario.corporativo", null, List.of()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(usuarioService.obtenerActual("usuario.corporativo"))
                .thenReturn(new UsuarioResponse(8, "Usuario", "ADMIN"));

        assertThatThrownBy(() -> guard.validarUsuarioSolicitado(99))
                .isInstanceOf(IdentidadNoCoincideException.class)
                .hasMessageContaining("no coincide");
    }
}
