package ar.com.sistemaincidentes.api.incidentes;

import static org.mockito.Mockito.verify;
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
import ar.com.sistemaincidentes.api.security.AuthenticatedUserGuard;
import ar.com.sistemaincidentes.api.web.ApiExceptionHandler;

@WebMvcTest(IncidenteController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-busqueda-prueba",
        "api.security.user=admin-test",
        "api.security.role=ADMIN"
})
class IncidenteBusquedaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidenteConsultaService service;

    @MockitoBean
    private IncidenteCreacionService creacionService;

    @MockitoBean
    private AuthenticatedUserGuard authenticatedUserGuard;

    @Test
    void recibeFiltrosHistoricosYDevuelveResultados() throws Exception {
        LocalDate desde = LocalDate.of(2026, 7, 1);
        LocalDate hasta = LocalDate.of(2026, 7, 26);
        when(service.listar("RESUELTO", "produccion", desde, hasta))
                .thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/incidentes")
                        .param("estado", "RESUELTO")
                        .param("texto", "produccion")
                        .param("desde", "2026-07-01")
                        .param("hasta", "2026-07-26")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-busqueda-prueba"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(9));

        verify(service).listar("RESUELTO", "produccion", desde, hasta);
    }

    private IncidenteResponse respuesta() {
        return new IncidenteResponse(
                9, "Incidente", "Detalle", "MEDIA", "RESUELTO",
                3, "Nombre Apellido", "Producción", null,
                null, null, "Planta", "Nombre Apellido", "Operario",
                "123", "12345678", "Producción", "Superior", "Historial",
                null, 2
        );
    }
}
