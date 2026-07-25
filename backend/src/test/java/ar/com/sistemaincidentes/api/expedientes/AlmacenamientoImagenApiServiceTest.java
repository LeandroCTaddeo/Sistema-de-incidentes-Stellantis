package ar.com.sistemaincidentes.api.expedientes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.support.TestPropertySourceUtils;

import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

class AlmacenamientoImagenApiServiceTest {

    @TempDir
    Path temporal;

    @Test
    void springPuedeConstruirElServicioConLaConfiguracionExterna() {
        try (var context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    context,
                    "api.storage.root=" + temporal.toString().replace('\\', '/'),
                    "api.storage.allow-legacy-absolute-paths=false"
            );
            context.register(AlmacenamientoImagenApiService.class);
            context.refresh();

            assertThat(context.getBean(AlmacenamientoImagenApiService.class)).isNotNull();
        }
    }

    @Test
    void abreUnaImagenRelativaDentroDelAlmacenamiento() throws Exception {
        Path archivo = temporal.resolve("incidentes/7/foto.jpg");
        Files.createDirectories(archivo.getParent());
        Files.write(archivo, new byte[] { 1, 2, 3 });

        var service = new AlmacenamientoImagenApiService(temporal, false);
        var imagen = new ImagenAdjuntaArchivo(10, 7, "incidentes/7/foto.jpg");

        ImagenAdjuntaResponse descripcion = service.describir(imagen);
        ImagenContenido contenido = service.abrir(imagen);

        assertThat(descripcion.nombreArchivo()).isEqualTo("foto.jpg");
        assertThat(descripcion.contentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
        assertThat(contenido.longitud()).isEqualTo(3);
        assertThat(contenido.recurso().exists()).isTrue();
    }

    @Test
    void rechazaUnaRutaQueIntentaSalirDelAlmacenamiento() {
        var service = new AlmacenamientoImagenApiService(temporal, false);
        var imagen = new ImagenAdjuntaArchivo(10, 7, "../fuera.jpg");

        assertThatThrownBy(() -> service.abrir(imagen))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("fuera del almacenamiento");
    }

    @Test
    void rechazaRutasAbsolutasSiLaCompatibilidadNoEstaHabilitada() {
        var service = new AlmacenamientoImagenApiService(temporal, false);
        var imagen = new ImagenAdjuntaArchivo(10, 7, temporal.resolve("foto.jpg").toString());

        assertThatThrownBy(() -> service.abrir(imagen))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("absoluta");
    }
}
