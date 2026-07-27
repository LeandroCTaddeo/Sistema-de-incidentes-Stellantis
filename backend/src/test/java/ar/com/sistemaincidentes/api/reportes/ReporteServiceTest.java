package ar.com.sistemaincidentes.api.reportes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class ReporteServiceTest {

    @Test
    void reúneResumenAreasYPrioridades() {
        ReporteRepository repository = mock(ReporteRepository.class);
        ReporteService service = new ReporteService(repository);
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 7, 26);
        when(repository.obtenerResumen(desde, hasta))
                .thenReturn(new ResumenReporteResponse(8, 3, 5, 12.5));
        when(repository.obtenerPorArea(desde, hasta))
                .thenReturn(List.of(new DatoConteoResponse("Producción", 4)));
        when(repository.obtenerPorPrioridad(desde, hasta))
                .thenReturn(List.of(new DatoConteoResponse("ALTA", 2)));

        ReporteResponse response = service.obtener(desde, hasta);

        assertThat(response.resumen().total()).isEqualTo(8);
        assertThat(response.areas()).hasSize(1);
        assertThat(response.prioridades()).hasSize(1);
    }

    @Test
    void rechazaRangoInvertido() {
        ReporteService service = new ReporteService(mock(ReporteRepository.class));

        assertThatThrownBy(() -> service.obtener(
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 7, 26)
        )).isInstanceOf(IllegalArgumentException.class);
    }
}
