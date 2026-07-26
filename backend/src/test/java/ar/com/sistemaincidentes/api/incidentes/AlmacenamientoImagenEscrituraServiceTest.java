package ar.com.sistemaincidentes.api.incidentes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

class AlmacenamientoImagenEscrituraServiceTest {

    @TempDir
    Path temporal;

    @Test
    void guardaUnaImagenRealConRutaRelativaAdministrada() throws Exception {
        var service = new AlmacenamientoImagenEscrituraService(temporal, 1024 * 1024, 1000);
        var archivo = new MockMultipartFile(
                "imagenes",
                "evidencia.png",
                "image/png",
                imagenPng()
        );

        ImagenGuardada guardada = service.almacenar(archivo, 7);

        assertThat(guardada.rutaRelativa()).startsWith("incidentes/7/");
        assertThat(guardada.rutaRelativa()).endsWith(".png");
        assertThat(Files.isRegularFile(guardada.rutaFisica())).isTrue();
        assertThat(guardada.rutaFisica()).startsWith(temporal);
    }

    @Test
    void rechazaUnArchivoQueSoloFingeSerImagen() {
        var service = new AlmacenamientoImagenEscrituraService(temporal, 1024, 1000);
        var archivo = new MockMultipartFile(
                "imagenes",
                "falsa.jpg",
                "image/jpeg",
                "esto no es una imagen".getBytes()
        );

        assertThatThrownBy(() -> service.almacenar(archivo, 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("imagen válida");
    }

    @Test
    void rechazaUnaImagenQueSuperaElMaximoDeBytes() throws Exception {
        byte[] imagen = imagenPng();
        var service = new AlmacenamientoImagenEscrituraService(
                temporal,
                imagen.length - 1,
                1000
        );
        var archivo = new MockMultipartFile(
                "imagenes",
                "grande.png",
                "image/png",
                imagen
        );

        assertThatThrownBy(() -> service.almacenar(archivo, 7))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tamaño máximo");
    }

    private byte[] imagenPng() throws Exception {
        BufferedImage imagen = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        try (var salida = new ByteArrayOutputStream()) {
            ImageIO.write(imagen, "png", salida);
            return salida.toByteArray();
        }
    }
}
