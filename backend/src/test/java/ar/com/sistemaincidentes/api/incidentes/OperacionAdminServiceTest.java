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
        when(repository.obtenerResponsable(5)).thenReturn(2);
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
        when(repository.obtenerResponsable(5)).thenReturn(2);
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
        when(repository.obtenerResponsable(5)).thenReturn(2);
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

    @Test
    void tomaUnCasoLibreYRegistraAuditoria() {
        OperacionAdminRepository repository = mock(OperacionAdminRepository.class);
        OperacionAdminService service = new OperacionAdminService(repository);
        when(repository.existeAdministrador(2)).thenReturn(true);
        when(repository.obtenerEstadoIncidente(5)).thenReturn("PENDIENTE");
        when(repository.tomarIncidente(5, 2)).thenReturn(true);
        when(repository.obtenerAsignacion(5)).thenReturn(java.util.Optional.of(
                new AsignacionIncidenteResponse(5, 2, "Ana Admin", null)
        ));

        AsignacionIncidenteResponse response = service.tomar(
                5, new AsignacionIncidenteRequest(2)
        );

        assertThat(response.administradorId()).isEqualTo(2);
        verify(repository).registrarAsignacion(5, 2, "TOMADO");
    }

    @Test
    void rechazaResolverUnCasoAsignadoAOtroAdministrador() {
        OperacionAdminRepository repository = mock(OperacionAdminRepository.class);
        OperacionAdminService service = new OperacionAdminService(repository);
        when(repository.existeAdministrador(2)).thenReturn(true);
        when(repository.obtenerEstadoIncidente(5)).thenReturn("PENDIENTE");
        when(repository.obtenerResponsable(5)).thenReturn(3);

        assertThatThrownBy(() -> service.resolver(5, new ResolucionIncidenteRequest(2)))
                .isInstanceOf(ConflictoOperacionException.class)
                .hasMessageContaining("responsable");
        verify(repository, never()).resolverIncidente(5, 2);
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
