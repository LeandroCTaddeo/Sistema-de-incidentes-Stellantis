package ar.com.sistemaincidentes.api.incidentes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import ar.com.sistemaincidentes.api.web.ApiExceptionHandler;

@WebMvcTest(OperacionAdminController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-admin-prueba",
        "api.security.user=admin-test",
        "api.security.role=ADMIN"
})
class OperacionAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OperacionAdminService service;

    @Test
    void creaBoletinConCredencialAdministrativa() throws Exception {
        when(service.crearBoletin(any(Integer.class), any()))
                .thenReturn(new BoletinAdminGuardadoResponse(18));

        mockMvc.perform(post("/api/incidentes/5/boletines")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-admin-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(boletinJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(18));
    }

    @Test
    void actualizaBoletinConCredencialAdministrativa() throws Exception {
        when(service.actualizarBoletin(any(Integer.class), any(Integer.class), any()))
                .thenReturn(new BoletinAdminGuardadoResponse(18));

        mockMvc.perform(put("/api/incidentes/5/boletines/18")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-admin-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(boletinJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(18));
    }

    @Test
    void resuelveIncidenteConCredencialAdministrativa() throws Exception {
        when(service.resolver(any(Integer.class), any()))
                .thenReturn(new IncidenteResueltoResponse(5, "RESUELTO"));

        mockMvc.perform(patch("/api/incidentes/5/resolucion")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-admin-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"administradorId\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RESUELTO"));
    }

    @Test
    void rechazaTituloVacio() throws Exception {
        mockMvc.perform(post("/api/incidentes/5/boletines")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-admin-prueba")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(boletinJson().replace("Investigación", "")))
                .andExpect(status().isBadRequest());
    }

    private String boletinJson() {
        return """
                {
                  "administradorId": 2,
                  "titulo": "Investigación",
                  "descripcion": "Detalle",
                  "prioridad": "MEDIA"
                }
                """;
    }
}
