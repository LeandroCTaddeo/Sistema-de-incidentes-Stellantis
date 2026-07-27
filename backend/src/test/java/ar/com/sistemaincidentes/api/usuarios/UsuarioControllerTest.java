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
import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

@WebMvcTest(UsuarioController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-usuario-prueba",
        "api.security.user=admin-test",
        "api.security.role=ADMIN"
})
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    @Test
    void devuelveUsuarioActualAlAdministrador() throws Exception {
        when(service.obtenerActual("leand"))
                .thenReturn(new UsuarioResponse(2, "Nombre Apellido", "ADMIN"));

        mockMvc.perform(get("/api/usuarios/actual")
                        .param("usuarioWindows", "leand")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-usuario-prueba"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void devuelve404CuandoNoEstaRegistrado() throws Exception {
        when(service.obtenerActual("desconocido")).thenThrow(
                new RecursoNoEncontradoException("Usuario no registrado.")
        );

        mockMvc.perform(get("/api/usuarios/actual")
                        .param("usuarioWindows", "desconocido")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-usuario-prueba"
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
