package ar.com.sistemaincidentes.api.usuarios;

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

@WebMvcTest(UsuarioController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-usuarios-empleado",
        "api.security.user=empleado-test",
        "api.security.role=EMPLOYEE"
})
class UsuarioAdministracionForbiddenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    @Test
    void empleadoNoPuedeListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-usuarios-empleado"
                        ))
                .andExpect(status().isForbidden());
    }
}
