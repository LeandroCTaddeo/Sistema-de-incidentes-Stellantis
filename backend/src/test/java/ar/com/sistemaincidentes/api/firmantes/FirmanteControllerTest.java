package ar.com.sistemaincidentes.api.firmantes;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

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
import ar.com.sistemaincidentes.api.web.ApiExceptionHandler;

@WebMvcTest(controllers = { FirmanteController.class, FirmaExpedienteController.class })
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-firmantes-admin",
        "api.security.user=admin-test",
        "api.security.role=ADMIN"
})
class FirmanteControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private FirmanteService service;

    @Test
    void listaFirmantesParaAdministrador() throws Exception {
        when(service.listar(true)).thenReturn(List.of(
                new FirmanteResponse(
                        3, "Lopez Carlos Argentino", "Security and Facilities",
                        "Argentina", "Palomar", true, null, 2, true
                )
        ));

        mockMvc.perform(get("/api/firmantes")
                        .param("incluirInactivos", "true")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-firmantes-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Lopez Carlos Argentino"))
                .andExpect(jsonPath("$[0].obligatorio").value(true));
    }

    @Test
    void guardaLaSeleccionDelExpediente() throws Exception {
        SeleccionFirmantesRequest request = new SeleccionFirmantesRequest(
                4, List.of(1, 3)
        );
        when(service.seleccionar(9, request)).thenReturn(List.of(
                new FirmaExpedienteResponse(
                        1, 1, "Guillermo Taddeo", "Security and Facilities",
                        "Palomar Plant", "Palomar", LocalDateTime.of(2026, 7, 27, 12, 0)
                ),
                new FirmaExpedienteResponse(
                        3, 2, "Lopez Carlos Argentino", "Security and Facilities",
                        "Argentina", "Palomar", LocalDateTime.of(2026, 7, 27, 12, 0)
                )
        ));

        mockMvc.perform(post("/api/incidentes/9/firmas")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-firmantes-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "administradorId": 4,
                                  "firmanteIds": [1, 3]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$[0].orden").value(1))
                .andExpect(jsonPath("$[1].orden").value(2));
    }
}
