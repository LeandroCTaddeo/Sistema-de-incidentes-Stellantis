package ar.com.sistemaincidentes.api.usuarios;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import ar.com.sistemaincidentes.api.web.ConflictoOperacionException;

class UsuarioAdministracionServiceTest {

    @Test
    void creaUsuarioNormalizandoLosDatos() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        UsuarioService service = new UsuarioService(repository);
        UsuarioAdministracionResponse esperado = new UsuarioAdministracionResponse(
                8, "Ana Pérez", "aperez", "Calidad", "EMPLEADO", true
        );
        when(repository.existeUsuarioWindows("aperez", (Integer) null)).thenReturn(false);
        when(repository.crear("Ana Pérez", "aperez", "Calidad", "EMPLEADO"))
                .thenReturn(esperado);

        UsuarioAdministracionResponse creado = service.crear(
                new UsuarioGuardarRequest("  Ana Pérez  ", " aperez ", " Calidad ", "empleado")
        );

        assertThat(creado).isEqualTo(esperado);
        verify(repository).crear("Ana Pérez", "aperez", "Calidad", "EMPLEADO");
    }

    @Test
    void rechazaCuentaWindowsDuplicada() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        UsuarioService service = new UsuarioService(repository);
        when(repository.existeUsuarioWindows("aperez", (Integer) null)).thenReturn(true);

        assertThatThrownBy(() -> service.crear(
                new UsuarioGuardarRequest("Ana Pérez", "aperez", "Calidad", "EMPLEADO")
        ))
                .isInstanceOf(ConflictoOperacionException.class)
                .hasMessageContaining("Ya existe");
    }

    @Test
    void noPermiteDeshabilitarAlUltimoAdministrador() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        UsuarioService service = new UsuarioService(repository);
        UsuarioAdministracionResponse administrador = new UsuarioAdministracionResponse(
                2, "Administrador", "admin", "Seguridad", "ADMIN", true
        );
        when(repository.buscarPorId(2)).thenReturn(Optional.of(administrador));
        when(repository.contarAdministradoresActivosExcepto(2)).thenReturn(0L);

        assertThatThrownBy(() -> service.cambiarEstado(2, new UsuarioEstadoRequest(false)))
                .isInstanceOf(ConflictoOperacionException.class)
                .hasMessageContaining("al menos un administrador");
    }

    @Test
    void permiteDeshabilitarAdministradorCuandoExisteOtroActivo() {
        UsuarioRepository repository = mock(UsuarioRepository.class);
        UsuarioService service = new UsuarioService(repository);
        UsuarioAdministracionResponse administrador = new UsuarioAdministracionResponse(
                2, "Administrador", "admin", "Seguridad", "ADMIN", true
        );
        UsuarioAdministracionResponse deshabilitado = new UsuarioAdministracionResponse(
                2, "Administrador", "admin", "Seguridad", "ADMIN", false
        );
        when(repository.buscarPorId(2))
                .thenReturn(Optional.of(administrador), Optional.of(deshabilitado));
        when(repository.contarAdministradoresActivosExcepto(2)).thenReturn(1L);
        when(repository.actualizarEstado(2, false)).thenReturn(true);

        UsuarioAdministracionResponse resultado = service.cambiarEstado(
                2, new UsuarioEstadoRequest(false)
        );

        assertThat(resultado.activo()).isFalse();
        verify(repository).actualizarEstado(2, false);
    }

    @Test
    void rechazaRolDesconocido() {
        UsuarioService service = new UsuarioService(mock(UsuarioRepository.class));

        assertThatThrownBy(() -> service.crear(
                new UsuarioGuardarRequest("Ana Pérez", "aperez", "Calidad", "SUPERVISOR")
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ADMIN o EMPLEADO");
    }
}
