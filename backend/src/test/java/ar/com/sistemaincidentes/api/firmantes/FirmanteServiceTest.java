package ar.com.sistemaincidentes.api.firmantes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ar.com.sistemaincidentes.api.security.AuthenticatedUserGuard;
import ar.com.sistemaincidentes.api.web.ConflictoOperacionException;

@ExtendWith(MockitoExtension.class)
class FirmanteServiceTest {

    @Mock private FirmanteRepository repository;
    @Mock private AuthenticatedUserGuard guard;

    private FirmanteService service;
    private FirmanteResponse guillermo;
    private FirmanteResponse latorre;
    private FirmanteResponse lopez;

    @BeforeEach
    void preparar() {
        service = new FirmanteService(repository, guard);
        guillermo = new FirmanteResponse(
                1, "Guillermo Taddeo", "Security and Facilities", "Palomar Plant",
                "Palomar", false, "RESPONSABLE_PALOMAR", 1, true
        );
        latorre = new FirmanteResponse(
                2, "Latorre Julián", "Security and Facilities", "Palomar Plant",
                "Palomar", false, "RESPONSABLE_PALOMAR", 1, true
        );
        lopez = new FirmanteResponse(
                3, "Lopez Carlos Argentino", "Security and Facilities", "Argentina",
                "Palomar", true, null, 2, true
        );
    }

    @Test
    void guardaUnaAlternativaYElFirmanteObligatorio() {
        when(repository.incidenteExiste(9)).thenReturn(true);
        when(repository.obtenerSeleccion(9))
                .thenReturn(List.of())
                .thenReturn(List.of(
                        firma(guillermo),
                        firma(lopez)
                ));
        when(repository.listar("Palomar", false))
                .thenReturn(List.of(guillermo, latorre, lopez));

        var resultado = service.seleccionar(
                9,
                new SeleccionFirmantesRequest(4, List.of(3, 1))
        );

        assertThat(resultado).extracting(FirmaExpedienteResponse::firmanteId)
                .containsExactly(1, 3);
        verify(guard).validarUsuarioSolicitado(4);
        verify(repository).guardarSeleccion(9, 4, List.of(guillermo, lopez));
    }

    @Test
    void rechazaUnaSeleccionSinLopez() {
        when(repository.incidenteExiste(9)).thenReturn(true);
        when(repository.obtenerSeleccion(9)).thenReturn(List.of());
        when(repository.listar("Palomar", false))
                .thenReturn(List.of(guillermo, latorre, lopez));

        assertThatThrownBy(() -> service.seleccionar(
                9,
                new SeleccionFirmantesRequest(4, List.of(1))
        )).isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Lopez Carlos Argentino");

        verify(repository, never()).guardarSeleccion(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                anyList()
        );
    }

    @Test
    void conservaLaSeleccionHistoricaExistente() {
        var existentes = List.of(firma(guillermo), firma(lopez));
        when(repository.incidenteExiste(9)).thenReturn(true);
        when(repository.obtenerSeleccion(9)).thenReturn(existentes);

        var resultado = service.seleccionar(
                9,
                new SeleccionFirmantesRequest(4, List.of(2, 3))
        );

        assertThat(resultado).isSameAs(existentes);
        verify(repository, never()).guardarSeleccion(
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(),
                anyList()
        );
    }

    @Test
    void noPermiteDeshabilitarLaUltimaAlternativa() {
        when(repository.buscarPorId(1)).thenReturn(java.util.Optional.of(guillermo));
        when(repository.contarAlternativasActivas("Palomar", "RESPONSABLE_PALOMAR", 1))
                .thenReturn(0L);

        assertThatThrownBy(() -> service.cambiarEstado(
                1, new FirmanteEstadoRequest(false)
        )).isInstanceOf(ConflictoOperacionException.class)
          .hasMessageContaining("seleccionable activo");
    }

    @Test
    void noPermiteCrearDosFirmantesObligatorios() {
        when(repository.contarObligatoriosActivos("Palomar", 0)).thenReturn(1L);

        assertThatThrownBy(() -> service.crear(new FirmanteGuardarRequest(
                "Otro responsable", "Security and Facilities", "Argentina", "OBLIGATORIO"
        ))).isInstanceOf(ConflictoOperacionException.class)
          .hasMessageContaining("ya tiene un firmante obligatorio");
    }

    private FirmaExpedienteResponse firma(FirmanteResponse firmante) {
        return new FirmaExpedienteResponse(
                firmante.id(),
                firmante.orden(),
                firmante.nombre(),
                firmante.areaLinea1(),
                firmante.areaLinea2(),
                firmante.planta(),
                LocalDateTime.of(2026, 7, 27, 12, 0)
        );
    }
}
