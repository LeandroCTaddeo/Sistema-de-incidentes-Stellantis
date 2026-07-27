package ar.com.sistemaincidentes.api.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

class UsuarioServiceTest {

    @Test
    void obtieneUsuarioRecortandoEspacios() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        UsuarioService service = new UsuarioService(repository);
        UsuarioResponse esperado = new UsuarioResponse(2, "Nombre Apellido", "ADMIN");
        when(repository.buscarPorUsuarioWindows("leand")).thenReturn(Optional.of(esperado));

        UsuarioResponse response = service.obtenerActual("  leand  ");

        assertThat(response).isEqualTo(esperado);
        verify(repository).buscarPorUsuarioWindows("leand");
    }

    @Test
    void informaCuandoElUsuarioNoEstaRegistrado() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        UsuarioService service = new UsuarioService(repository);
        when(repository.buscarPorUsuarioWindows("desconocido")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerActual("desconocido"))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("no está registrado");
    }

    @Test
    void rechazaUsuarioVacio() {
        UsuarioService service = new UsuarioService(mock(UsuarioRepository.class));

        assertThatThrownBy(() -> service.obtenerActual("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
