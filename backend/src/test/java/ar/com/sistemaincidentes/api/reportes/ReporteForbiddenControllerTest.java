package ar.com.sistemaincidentes.api.reportes;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ar.com.sistemaincidentes.api.security.ApiKeyAuthenticationFilter;
import ar.com.sistemaincidentes.api.security.SecurityConfig;
import ar.com.sistemaincidentes.api.web.ApiExceptionHandler;

@WebMvcTest(ReporteController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-empleado-reportes",
        "api.security.user=empleado-test",
        "api.security.role=EMPLOYEE"
})
class ReporteForbiddenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService service;

    @Test
    void impideQueUnEmpleadoConsulteReportes() throws Exception {
        mockMvc.perform(get("/api/reportes")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-07-26")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-empleado-reportes"
                        ))
                .andExpect(status().isForbidden());
    }
}
