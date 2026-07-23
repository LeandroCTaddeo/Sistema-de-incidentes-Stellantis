package ar.com.sistemaincidentes.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ar.com.sistemaincidentes.api.health.HealthController;
import ar.com.sistemaincidentes.api.incidentes.IncidenteConsultaService;
import ar.com.sistemaincidentes.api.incidentes.IncidenteController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = { IncidenteController.class, HealthController.class })
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "api.security.token=token-prueba-empleado",
        "api.security.user=empleado-test",
        "api.security.role=EMPLOYEE"
})
class ApiSecurityForbiddenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidenteConsultaService service;

    @Test
    void impideConsultarIncidentesSinRolAdministrador() throws Exception {
        mockMvc.perform(get("/api/incidentes")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-prueba-empleado"
                        ))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.path").value("/api/incidentes"));
    }
}
