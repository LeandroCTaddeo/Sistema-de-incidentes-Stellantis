package ar.com.sistemaincidentes.api.expedientes;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ar.com.sistemaincidentes.api.security.ApiKeyAuthenticationFilter;
import ar.com.sistemaincidentes.api.security.SecurityConfig;
import ar.com.sistemaincidentes.api.web.ApiExceptionHandler;
import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

@WebMvcTest(ExpedienteController.class)
@Import({ SecurityConfig.class, ApiExceptionHandler.class })
@TestPropertySource(properties = {
        "api.security.token=token-expediente-prueba",
        "api.security.user=admin-test",
        "api.security.role=ADMIN"
})
class ExpedienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpedienteConsultaService service;

    @Test
    void exigeCredencialParaConsultarElExpediente() throws Exception {
        mockMvc.perform(get("/api/incidentes/5/boletines"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void devuelveLosBoletinesInternos() throws Exception {
        when(service.listarBoletines(5)).thenReturn(List.of(
                new BoletinInternoResponse(
                        11, 5, 2, "Investigación", "Detalle",
                        null, null, null, null, null, null, null,
                        null, null, null, "MEDIA", null
                )
        ));

        mockMvc.perform(get("/api/incidentes/5/boletines")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-expediente-prueba"
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(11))
                .andExpect(jsonPath("$[0].incidenteId").value(5))
                .andExpect(jsonPath("$[0].titulo").value("Investigación"));
    }

    @Test
    void devuelve404CuandoNoExisteElIncidente() throws Exception {
        when(service.obtenerIncidente(999)).thenThrow(
                new RecursoNoEncontradoException("No se encontró el incidente solicitado.")
        );

        mockMvc.perform(get("/api/incidentes/999")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-expediente-prueba"
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/incidentes/999"));
    }

    @Test
    void entregaElContenidoDeLaImagenSinExponerSuRuta() throws Exception {
        byte[] bytes = new byte[] { 1, 2, 3, 4 };
        when(service.obtenerImagen(5, 12)).thenReturn(
                new ImagenContenido(
                        new ByteArrayResource(bytes),
                        "evidencia.jpg",
                        MediaType.IMAGE_JPEG,
                        bytes.length
                )
        );

        mockMvc.perform(get("/api/incidentes/5/imagenes/12/contenido")
                        .header(
                                ApiKeyAuthenticationFilter.HEADER_API_KEY,
                                "token-expediente-prueba"
                        ))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes(bytes))
                .andExpect(header().string(
                        HttpHeaders.CONTENT_DISPOSITION,
                        org.hamcrest.Matchers.containsString("evidencia.jpg")
                ));
    }
}
