package ar.com.sistemaincidentes.api.usuarios;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

@WebMvcTest(UsuarioController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-empleado-usuario",
        "api.security.user=empleado-test",
        "api.security.role=EMPLOYEE"
})
class UsuarioEmpleadoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    @Test
    void permiteAEmpleadoObtenerSuPerfil() throws Exception {
        when(service.obtenerActual("operario"))
                .thenReturn(new UsuarioResponse(7, "Operario Prueba", "EMPLEADO"));

        mockMvc.perform(get("/api/usuarios/actual")
                        .param("usuarioWindows", "operario")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-empleado-usuario"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("EMPLEADO"));
    }
}
