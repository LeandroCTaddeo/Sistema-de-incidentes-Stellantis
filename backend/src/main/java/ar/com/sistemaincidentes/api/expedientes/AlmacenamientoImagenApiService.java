package ar.com.sistemaincidentes.api.expedientes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

@Service
public class AlmacenamientoImagenApiService {

    private static final Map<String, MediaType> TIPOS_PERMITIDOS = Map.of(
            ".png", MediaType.IMAGE_PNG,
            ".jpg", MediaType.IMAGE_JPEG,
            ".jpeg", MediaType.IMAGE_JPEG
    );

    private final Path raiz;
    private final boolean permitirAbsolutasLegadas;

    @Autowired
    public AlmacenamientoImagenApiService(
            @Value("${api.storage.root}") String raiz,
            @Value("${api.storage.allow-legacy-absolute-paths:false}")
            boolean permitirAbsolutasLegadas
    ) {
        this(Path.of(raiz), permitirAbsolutasLegadas);
    }

    AlmacenamientoImagenApiService(Path raiz, boolean permitirAbsolutasLegadas) {
        this.raiz = raiz.toAbsolutePath().normalize();
        this.permitirAbsolutasLegadas = permitirAbsolutasLegadas;
    }

    public ImagenAdjuntaResponse describir(ImagenAdjuntaArchivo imagen) {
        String nombre = nombreArchivo(imagen.ruta());
        return new ImagenAdjuntaResponse(
                imagen.id(),
                imagen.incidenteId(),
                nombre,
                mediaType(nombre).toString()
        );
    }

    public ImagenContenido abrir(ImagenAdjuntaArchivo imagen) {
        Path archivo = resolver(imagen.ruta());
        String nombre = archivo.getFileName().toString();

        try {
            return new ImagenContenido(
                    new FileSystemResource(archivo),
                    nombre,
                    mediaType(nombre),
                    Files.size(archivo)
            );
        } catch (IOException e) {
            throw new RecursoNoEncontradoException("No se pudo leer la imagen solicitada.");
        }
    }

    private Path resolver(String rutaGuardada) {
        if (rutaGuardada == null || rutaGuardada.isBlank()) {
            throw new RecursoNoEncontradoException("La imagen no tiene una referencia válida.");
        }

        try {
            Path guardada = Path.of(rutaGuardada);
            Path resuelta;

            if (guardada.isAbsolute()) {
                if (!permitirAbsolutasLegadas) {
                    throw new RecursoNoEncontradoException(
                            "La referencia absoluta de la imagen no está habilitada."
                    );
                }
                resuelta = guardada.normalize();
            } else {
                resuelta = raiz.resolve(rutaGuardada.replace('/', java.io.File.separatorChar))
                        .normalize();
                if (!resuelta.startsWith(raiz)) {
                    throw new RecursoNoEncontradoException(
                            "La referencia de la imagen está fuera del almacenamiento permitido."
                    );
                }
            }

            if (!Files.isRegularFile(resuelta)) {
                throw new RecursoNoEncontradoException("No se encontró la imagen solicitada.");
            }
            return resuelta;
        } catch (InvalidPathException e) {
            throw new RecursoNoEncontradoException("La referencia de la imagen no es válida.");
        }
    }

    private String nombreArchivo(String ruta) {
        try {
            Path path = Path.of(ruta == null ? "" : ruta);
            Path nombre = path.getFileName();
            return nombre == null ? "imagen" : nombre.toString();
        } catch (InvalidPathException e) {
            return "imagen";
        }
    }

    private MediaType mediaType(String nombre) {
        String minusculas = nombre.toLowerCase(Locale.ROOT);
        for (var tipo : TIPOS_PERMITIDOS.entrySet()) {
            if (minusculas.endsWith(tipo.getKey())) return tipo.getValue();
        }
        throw new RecursoNoEncontradoException("El formato de la imagen no está permitido.");
    }
}
