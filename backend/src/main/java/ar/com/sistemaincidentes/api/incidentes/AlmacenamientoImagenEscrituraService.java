package ar.com.sistemaincidentes.api.incidentes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AlmacenamientoImagenEscrituraService {

    private final Path raiz;
    private final long maximoBytes;
    private final int maximaDimension;

    @Autowired
    public AlmacenamientoImagenEscrituraService(
            @Value("${api.storage.root}") String raiz,
            @Value("${api.storage.max-image-bytes:10485760}") long maximoBytes,
            @Value("${api.storage.max-image-dimension:12000}") int maximaDimension
    ) {
        this(Path.of(raiz), maximoBytes, maximaDimension);
    }

    AlmacenamientoImagenEscrituraService(
            Path raiz,
            long maximoBytes,
            int maximaDimension
    ) {
        this.raiz = raiz.toAbsolutePath().normalize();
        this.maximoBytes = maximoBytes;
        this.maximaDimension = maximaDimension;
    }

    public ImagenGuardada almacenar(MultipartFile imagen, int incidenteId) {
        validarArchivoBasico(imagen);
        String extension = detectarYValidarFormato(imagen);
        Path carpeta = raiz.resolve("incidentes").resolve(String.valueOf(incidenteId)).normalize();
        validarDentroDeRaiz(carpeta);
        Path destino = carpeta.resolve(UUID.randomUUID() + extension).normalize();
        validarDentroDeRaiz(destino);

        try {
            Files.createDirectories(carpeta);
            try (var entrada = imagen.getInputStream()) {
                Files.copy(entrada, destino);
            }
            String relativa = raiz.relativize(destino).toString()
                    .replace(java.io.File.separatorChar, '/');
            return new ImagenGuardada(relativa, destino);
        } catch (IOException e) {
            eliminarSilenciosamente(destino);
            throw new IllegalStateException("No se pudo almacenar una imagen adjunta.", e);
        }
    }

    public void eliminarSilenciosamente(ImagenGuardada imagen) {
        if (imagen != null) eliminarSilenciosamente(imagen.rutaFisica());
    }

    private void validarArchivoBasico(MultipartFile imagen) {
        if (imagen == null || imagen.isEmpty()) {
            throw new IllegalArgumentException("No se puede adjuntar una imagen vacía.");
        }
        if (imagen.getSize() > maximoBytes) {
            throw new IllegalArgumentException(
                    "Una imagen supera el tamaño máximo permitido de "
                    + (maximoBytes / 1024 / 1024) + " MB."
            );
        }
    }

    private String detectarYValidarFormato(MultipartFile imagen) {
        try (var entrada = imagen.getInputStream();
             ImageInputStream datos = ImageIO.createImageInputStream(entrada)) {
            if (datos == null) {
                throw new IllegalArgumentException("El archivo adjunto no es una imagen válida.");
            }

            Iterator<ImageReader> lectores = ImageIO.getImageReaders(datos);
            if (!lectores.hasNext()) {
                throw new IllegalArgumentException("El archivo adjunto no es una imagen válida.");
            }

            ImageReader lector = lectores.next();
            try {
                lector.setInput(datos, true, true);
                String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
                String extension = switch (formato) {
                    case "jpeg", "jpg" -> ".jpg";
                    case "png" -> ".png";
                    default -> throw new IllegalArgumentException(
                            "Sólo se permiten imágenes PNG o JPEG."
                    );
                };

                int ancho = lector.getWidth(0);
                int alto = lector.getHeight(0);
                if (ancho <= 0 || alto <= 0
                        || ancho > maximaDimension || alto > maximaDimension) {
                    throw new IllegalArgumentException(
                            "Las dimensiones de una imagen no están permitidas."
                    );
                }
                return extension;
            } finally {
                lector.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo validar una imagen adjunta.", e);
        }
    }

    private void validarDentroDeRaiz(Path ruta) {
        if (!ruta.startsWith(raiz)) {
            throw new IllegalArgumentException(
                    "La ruta de almacenamiento de la imagen no es válida."
            );
        }
    }

    private void eliminarSilenciosamente(Path archivo) {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
            // La limpieza no debe ocultar el error original.
        }
    }
}
