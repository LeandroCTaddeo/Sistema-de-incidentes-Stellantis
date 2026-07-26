package ar.com.sistemaincidentes.api.incidentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

class IncidenteCreacionServiceTest {

    @Test
    void creaIncidenteYRegistraTodasLasImagenes() {
        var repository = mock(IncidenteEscrituraRepository.class);
        var almacenamiento = mock(AlmacenamientoImagenEscrituraService.class);
        var service = new IncidenteCreacionService(
                repository,
                almacenamiento,
                transacciones(),
                10
        );
        var archivo = new MockMultipartFile(
                "imagenes",
                "foto.png",
                "image/png",
                new byte[] { 1 }
        );
        var guardada = new ImagenGuardada(
                "incidentes/21/foto.png",
                Path.of("incidentes/21/foto.png")
        );
        when(repository.existeUsuario(3)).thenReturn(true);
        when(repository.insertarIncidente(any())).thenReturn(21);
        when(almacenamiento.almacenar(archivo, 21)).thenReturn(guardada);

        IncidenteCreadoResponse respuesta = service.crear(solicitud(), List.of(archivo));

        assertThat(respuesta.id()).isEqualTo(21);
        assertThat(respuesta.imagenesGuardadas()).isEqualTo(1);
        verify(repository).insertarImagen(21, "incidentes/21/foto.png");
    }

    @Test
    void eliminaArchivosSiFallaElRegistroEnBaseDeDatos() {
        var repository = mock(IncidenteEscrituraRepository.class);
        var almacenamiento = mock(AlmacenamientoImagenEscrituraService.class);
        var service = new IncidenteCreacionService(
                repository,
                almacenamiento,
                transacciones(),
                10
        );
        var archivo = new MockMultipartFile(
                "imagenes",
                "foto.png",
                "image/png",
                new byte[] { 1 }
        );
        var guardada = new ImagenGuardada(
                "incidentes/21/foto.png",
                Path.of("incidentes/21/foto.png")
        );
        when(repository.existeUsuario(3)).thenReturn(true);
        when(repository.insertarIncidente(any())).thenReturn(21);
        when(almacenamiento.almacenar(archivo, 21)).thenReturn(guardada);
        doThrow(new DataAccessResourceFailureException("fallo"))
                .when(repository).insertarImagen(21, guardada.rutaRelativa());

        assertThatThrownBy(() -> service.crear(solicitud(), List.of(archivo)))
                .isInstanceOf(DataAccessResourceFailureException.class);
        verify(almacenamiento).eliminarSilenciosamente(guardada);
    }

    @Test
    void rechazaMasImagenesQueElLimiteConfigurado() {
        var service = new IncidenteCreacionService(
                mock(IncidenteEscrituraRepository.class),
                mock(AlmacenamientoImagenEscrituraService.class),
                transacciones(),
                1
        );
        var una = new MockMultipartFile("imagenes", "1.png", "image/png", new byte[] { 1 });
        var dos = new MockMultipartFile("imagenes", "2.png", "image/png", new byte[] { 2 });

        assertThatThrownBy(() -> service.crear(solicitud(), List.of(una, dos)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("máximo 1");
    }

    private IncidenteCreacionRequest solicitud() {
        return new IncidenteCreacionRequest(
                "Título",
                "Descripción",
                PrioridadIncidente.MEDIA,
                3,
                LocalDate.of(2026, 7, 25),
                LocalDate.of(2026, 7, 25),
                "Planta",
                "Nombre Apellido",
                "Operario",
                "123",
                "12345678",
                "Producción",
                "Superior",
                "Historial"
        );
    }

    private TransactionTemplate transacciones() {
        return new TransactionTemplate(new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() {
                return new Object();
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) {
            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) {
            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) {
            }
        });
    }
}
