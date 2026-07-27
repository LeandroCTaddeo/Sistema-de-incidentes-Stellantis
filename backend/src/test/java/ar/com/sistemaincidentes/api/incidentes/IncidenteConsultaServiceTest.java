package ar.com.sistemaincidentes.api.incidentes;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class IncidenteConsultaServiceTest {

    @Test
    void normalizaTextoYConservaRangoDeFechas() {
        IncidenteConsultaRepository repository = mock(IncidenteConsultaRepository.class);
        IncidenteConsultaService service = new IncidenteConsultaService(repository);
        LocalDate desde = LocalDate.of(2026, 7, 1);
        LocalDate hasta = LocalDate.of(2026, 7, 26);

        service.listar("RESUELTO", "  Producción  ", desde, hasta);

        verify(repository).listar(EstadoIncidente.RESUELTO, "produccion", desde, hasta);
    }

    @Test
    void rechazaUnRangoDeFechasInvertido() {
        IncidenteConsultaService service = new IncidenteConsultaService(
                mock(IncidenteConsultaRepository.class)
        );

        assertThatThrownBy(() -> service.listar(
                "RESUELTO",
                "",
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 7, 26)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha desde");
    }
}
