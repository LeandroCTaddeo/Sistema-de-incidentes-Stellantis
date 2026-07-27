package ar.com.sistemaincidentes.api.reportes;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

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
        "api.security.token=token-reportes-prueba",
        "api.security.user=admin-test",
        "api.security.role=ADMIN"
})
class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReporteService service;

    @Test
    void devuelveReporteCompletoAlAdministrador() throws Exception {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 7, 26);
        when(service.obtener(desde, hasta)).thenReturn(new ReporteResponse(
                new ResumenReporteResponse(8, 3, 5, 12.5),
                List.of(new DatoConteoResponse("Producción", 4)),
                List.of(new DatoConteoResponse("ALTA", 2))
        ));

        mockMvc.perform(get("/api/reportes")
                        .param("desde", "2026-01-01")
                        .param("hasta", "2026-07-26")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-reportes-prueba"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumen.total").value(8))
                .andExpect(jsonPath("$.areas[0].nombre").value("Producción"))
                .andExpect(jsonPath("$.prioridades[0].cantidad").value(2));
    }

    @Test
    void exigeAmbasFechas() throws Exception {
        mockMvc.perform(get("/api/reportes")
                        .param("desde", "2026-01-01")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-reportes-prueba"
                        ))
                .andExpect(status().isBadRequest());
    }
}
