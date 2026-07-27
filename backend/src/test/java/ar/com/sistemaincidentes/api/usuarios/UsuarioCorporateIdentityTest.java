package ar.com.sistemaincidentes.api.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.junit.jupiter.api.Test;

class UsuarioCorporateIdentityTest {

    @Test
    void ignoraElParametroYUsaLaIdentidadAutenticadaEnModoCorporativo() {
        UsuarioService service = mock(UsuarioService.class);
        UsuarioController controller = new UsuarioController(service, true);
        Principal principal = () -> "usuario.corporativo";
        when(service.obtenerActual("usuario.corporativo"))
                .thenReturn(new UsuarioResponse(7, "Usuario Corporativo", "ADMIN"));

        UsuarioResponse response = controller.obtenerActual("usuario.inventado", principal);

        assertThat(response.id()).isEqualTo(7);
        verify(service).obtenerActual("usuario.corporativo");
    }
}
