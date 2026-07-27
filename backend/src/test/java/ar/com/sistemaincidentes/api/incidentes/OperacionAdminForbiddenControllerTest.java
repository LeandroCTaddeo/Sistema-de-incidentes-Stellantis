package ar.com.sistemaincidentes.api.incidentes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ar.com.sistemaincidentes.api.security.ApiKeyAuthenticationFilter;
import ar.com.sistemaincidentes.api.security.SecurityConfig;
import ar.com.sistemaincidentes.api.security.AuthenticatedUserGuard;
import ar.com.sistemaincidentes.api.web.ApiExceptionHandler;

@WebMvcTest(OperacionAdminController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-empleado-prueba",
        "api.security.user=empleado-test",
        "api.security.role=EMPLOYEE"
})
class OperacionAdminForbiddenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperacionAdminService service;

    @MockitoBean
    private AuthenticatedUserGuard authenticatedUserGuard;

    @Test
    void impideQueUnEmpleadoResuelvaIncidentes() throws Exception {
        mockMvc.perform(patch("/api/incidentes/5/resolucion")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-empleado-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"administradorId\":2}"))
                .andExpect(status().isForbidden());
    }
}
