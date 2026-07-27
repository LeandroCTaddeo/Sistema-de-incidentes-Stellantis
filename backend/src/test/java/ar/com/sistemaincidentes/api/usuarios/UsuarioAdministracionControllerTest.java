package ar.com.sistemaincidentes.api.usuarios;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(UsuarioController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-usuarios-admin",
        "api.security.user=admin-test",
        "api.security.role=ADMIN"
})
class UsuarioAdministracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService service;

    @Test
    void listaUsuariosParaAdministrador() throws Exception {
        when(service.listar("ana")).thenReturn(List.of(
                new UsuarioAdministracionResponse(
                        8, "Ana Pérez", "aperez", "Calidad", "EMPLEADO", true
                )
        ));

        mockMvc.perform(get("/api/usuarios")
                        .param("buscar", "ana")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-usuarios-admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioWindows").value("aperez"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void creaUsuario() throws Exception {
        UsuarioGuardarRequest request = new UsuarioGuardarRequest(
                "Ana Pérez", "aperez", "Calidad", "EMPLEADO"
        );
        when(service.crear(request)).thenReturn(
                new UsuarioAdministracionResponse(
                        8, "Ana Pérez", "aperez", "Calidad", "EMPLEADO", true
                )
        );

        mockMvc.perform(post("/api/usuarios")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-usuarios-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Ana Pérez",
                                  "usuarioWindows": "aperez",
                                  "sector": "Calidad",
                                  "rol": "EMPLEADO"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(8));
    }

    @Test
    void actualizaUsuario() throws Exception {
        UsuarioGuardarRequest request = new UsuarioGuardarRequest(
                "Ana Pérez", "aperez", "Producción", "ADMIN"
        );
        when(service.actualizar(8, request)).thenReturn(
                new UsuarioAdministracionResponse(
                        8, "Ana Pérez", "aperez", "Producción", "ADMIN", true
                )
        );

        mockMvc.perform(put("/api/usuarios/8")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-usuarios-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Ana Pérez",
                                  "usuarioWindows": "aperez",
                                  "sector": "Producción",
                                  "rol": "ADMIN"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMIN"));
    }

    @Test
    void cambiaEstadoUsuario() throws Exception {
        when(service.cambiarEstado(8, new UsuarioEstadoRequest(false))).thenReturn(
                new UsuarioAdministracionResponse(
                        8, "Ana Pérez", "aperez", "Calidad", "EMPLEADO", false
                )
        );

        mockMvc.perform(patch("/api/usuarios/8/estado")
                        .header(ApiKeyAuthenticationFilter.HEADER_API_KEY, "token-usuarios-admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }
}
