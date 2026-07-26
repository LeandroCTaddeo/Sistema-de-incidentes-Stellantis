package ar.com.sistemaincidentes.api.security;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import ar.com.sistemaincidentes.api.health.HealthController;
import ar.com.sistemaincidentes.api.incidentes.IncidenteCreacionService;
import ar.com.sistemaincidentes.api.incidentes.IncidenteConsultaService;
import ar.com.sistemaincidentes.api.incidentes.IncidenteController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = { IncidenteController.class, HealthController.class })
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "api.security.token=token-prueba-seguro",
        "api.security.user=admin-test",
        "api.security.role=ADMIN"
})
class ApiSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidenteConsultaService service;

    @MockitoBean
    private IncidenteCreacionService creacionService;

    @Test
    void permiteConsultarLaSaludSinToken() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void rechazaIncidentesSinToken() throws Exception {
        mockMvc.perform(get("/api/incidentes"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/incidentes"));
    }

    @Test
    void rechazaIncidentesConTokenIncorrecto() throws Exception {
        mockMvc.perform(get("/api/incidentes")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-incorrecto"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void permiteIncidentesConTokenYRolAdministrador() throws Exception {
        when(service.listar(isNull())).thenReturn(List.of());

        mockMvc.perform(get("/api/incidentes")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-prueba-seguro"
                        ))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
