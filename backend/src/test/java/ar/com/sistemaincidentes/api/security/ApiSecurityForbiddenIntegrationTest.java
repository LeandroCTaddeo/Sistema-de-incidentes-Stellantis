package ar.com.sistemaincidentes.api.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ar.com.sistemaincidentes.api.health.HealthController;
import ar.com.sistemaincidentes.api.incidentes.IncidenteCreadoResponse;
import ar.com.sistemaincidentes.api.incidentes.IncidenteCreacionService;
import ar.com.sistemaincidentes.api.incidentes.IncidenteConsultaService;
import ar.com.sistemaincidentes.api.incidentes.IncidenteController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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

    @MockitoBean
    private IncidenteCreacionService creacionService;

    @MockitoBean
    private AuthenticatedUserGuard authenticatedUserGuard;

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

    @Test
    void permiteAUnEmpleadoEnviarUnBoletinValido() throws Exception {
        var boletin = new MockMultipartFile(
                "incidente",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "titulo":"Incidente de prueba",
                  "descripcion":"Descripción",
                  "prioridad":"MEDIA",
                  "usuarioId":3,
                  "fechaRegistro":"2026-07-25",
                  "fechaEmision":"2026-07-25",
                  "lugar":"Planta",
                  "nombreApellido":"Nombre Apellido",
                  "cargo":"Operario",
                  "matricula":"123",
                  "dni":"12345678",
                  "area":"Producción",
                  "superiorInmediato":"Superior",
                  "historial":"Historial"
                }
                """.getBytes()
        );
        when(creacionService.crear(any(), any()))
                .thenReturn(new IncidenteCreadoResponse(25, 0));

        mockMvc.perform(multipart("/api/incidentes")
                        .file(boletin)
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-prueba-empleado"
                        ))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(25));
    }
}
