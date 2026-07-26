package ar.com.sistemaincidentes.api.incidentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import ar.com.sistemaincidentes.api.web.ConflictoOperacionException;
import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

class OperacionAdminServiceTest {

    @Test
    void creaBoletinEnUnExpedientePendiente() {
        OperacionAdminRepository repository = mock(OperacionAdminRepository.class);
        OperacionAdminService service = new OperacionAdminService(repository);
        when(repository.existeAdministrador(2)).thenReturn(true);
        when(repository.obtenerEstadoIncidente(5)).thenReturn("PENDIENTE");
        when(repository.insertarBoletin(5, solicitud())).thenReturn(18);

        BoletinAdminGuardadoResponse response = service.crearBoletin(5, solicitud());

        assertThat(response.id()).isEqualTo(18);
    }

    @Test
    void actualizaSolamenteElBoletinDelExpedienteIndicado() {
        OperacionAdminRepository repository = mock(OperacionAdminRepository.class);
        OperacionAdminService service = new OperacionAdminService(repository);
        when(repository.existeAdministrador(2)).thenReturn(true);
        when(repository.obtenerEstadoIncidente(5)).thenReturn("PENDIENTE");
        when(repository.actualizarBoletin(5, 18, solicitud())).thenReturn(true);

        BoletinAdminGuardadoResponse response =
                service.actualizarBoletin(5, 18, solicitud());

        assertThat(response.id()).isEqualTo(18);
    }

    @Test
    void rechazaModificarUnExpedienteResuelto() {
        OperacionAdminRepository repository = mock(OperacionAdminRepository.class);
        OperacionAdminService service = new OperacionAdminService(repository);
        when(repository.existeAdministrador(2)).thenReturn(true);
        when(repository.obtenerEstadoIncidente(5)).thenReturn("RESUELTO");

        assertThatThrownBy(() -> service.crearBoletin(5, solicitud()))
                .isInstanceOf(ConflictoOperacionException.class)
                .hasMessageContaining("resuelto");
        verify(repository, never()).insertarBoletin(5, solicitud());
    }

    @Test
    void resuelveUnIncidentePendiente() {
        OperacionAdminRepository repository = mock(OperacionAdminRepository.class);
        OperacionAdminService service = new OperacionAdminService(repository);
        when(repository.existeAdministrador(2)).thenReturn(true);
        when(repository.obtenerEstadoIncidente(5)).thenReturn("PENDIENTE");
        when(repository.resolverIncidente(5, 2)).thenReturn(true);

        IncidenteResueltoResponse response =
                service.resolver(5, new ResolucionIncidenteRequest(2));

        assertThat(response.id()).isEqualTo(5);
        assertThat(response.estado()).isEqualTo("RESUELTO");
    }

    @Test
    void devuelveNoEncontradoSiNoExisteElIncidente() {
        OperacionAdminRepository repository = mock(OperacionAdminRepository.class);
        OperacionAdminService service = new OperacionAdminService(repository);
        when(repository.existeAdministrador(2)).thenReturn(true);
        when(repository.obtenerEstadoIncidente(99)).thenReturn(null);

        assertThatThrownBy(() -> service.resolver(99, new ResolucionIncidenteRequest(2)))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private BoletinAdminEscrituraRequest solicitud() {
        return new BoletinAdminEscrituraRequest(
                2,
                "Investigación",
                "Descripción",
                LocalDate.of(2026, 7, 25),
                LocalDate.of(2026, 7, 25),
                "Planta",
                "Nombre Apellido",
                "Seguridad",
                "123",
                "12345678",
                "Seguridad",
                "Superior",
                "Historial",
                PrioridadIncidente.MEDIA
        );
    }
}
