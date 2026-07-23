package ar.com.sistemaincidentes.api.health;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class HealthControllerTest {

    @Test
    void informaQueElServicioEstaDisponible() {
        HealthController.HealthResponse response = new HealthController().health();

        assertThat(response.status()).isEqualTo("UP");
        assertThat(response.service()).isEqualTo("sistema-incidentes-api");
        assertThat(response.timestamp()).isNotNull();
    }
}
