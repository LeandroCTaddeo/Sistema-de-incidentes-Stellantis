package ar.com.sistemaincidentes.api.incidentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EstadoIncidenteTest {

    @Test
    void aceptaEstadoSinImportarMayusculasOEspacios() {
        assertThat(EstadoIncidente.desdeParametro(" pendiente "))
                .isEqualTo(EstadoIncidente.PENDIENTE);
    }

    @Test
    void devuelveNullCuandoNoSeSolicitaUnEstado() {
        assertThat(EstadoIncidente.desdeParametro(" ")).isNull();
    }

    @Test
    void rechazaEstadosDesconocidos() {
        assertThatThrownBy(() -> EstadoIncidente.desdeParametro("CERRADO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PENDIENTE y RESUELTO");
    }
}
