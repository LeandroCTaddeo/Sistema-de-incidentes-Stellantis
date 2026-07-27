package ar.com.sistemaincidentes.api.security;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import ar.com.sistemaincidentes.api.incidentes.IncidenteCreacionService;
import ar.com.sistemaincidentes.api.incidentes.IncidenteConsultaService;
import ar.com.sistemaincidentes.api.incidentes.IncidenteController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IncidenteController.class)
@Import(CorporateSecurityConfig.class)
@ActiveProfiles("corporate")
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://id.example.test",
        "spring.security.oauth2.resourceserver.jwt.audiences=incidentes-api",
        "server.ssl.enabled=false",
        "TLS_KEYSTORE_PATH=certificado-prueba.p12",
        "TLS_KEYSTORE_PASSWORD=solo-prueba"
})
class CorporateSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private IncidenteConsultaService consultaService;

    @MockitoBean
    private IncidenteCreacionService creacionService;

    @MockitoBean
    private AuthenticatedUserGuard authenticatedUserGuard;

    @Test
    void rechazaUnaConsultaSinIdentidadCorporativa() throws Exception {
        mockMvc.perform(get("/api/incidentes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permiteUnaConsultaConJwtYPermisoAdministrador() throws Exception {
        when(consultaService.listar(isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/incidentes")
                        .with(jwt().authorities(
                                new SimpleGrantedAuthority("ROLE_ADMIN")
                        )))
                .andExpect(status().isOk());
    }
}
