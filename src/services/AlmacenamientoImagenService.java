package services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public final class AlmacenamientoImagenService {

    private static final String VARIABLE_RUTA = "INCIDENTES_FILES_PATH";
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(".png", ".jpg", ".jpeg");

    private final Path raiz;

    public AlmacenamientoImagenService() {
        this(obtenerRaizConfigurada());
    }

    AlmacenamientoImagenService(Path raiz) {
        this.raiz = raiz.toAbsolutePath().normalize();
    }

    public String almacenar(File origen, int incidenteId) {
        if (incidenteId <= 0) {
            throw new StorageException("No se puede guardar una imagen sin un incidente válido.");
        }

        Path archivoOrigen = origen.toPath().toAbsolutePath().normalize();
        if (!Files.isRegularFile(archivoOrigen)) {
            throw new StorageException("No se encontró la imagen seleccionada: " + origen.getName());
        }

        String extension = obtenerExtension(origen.getName());
        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new StorageException("El formato de la imagen no está permitido: " + origen.getName());
        }

        Path carpetaIncidente = raiz.resolve("incidentes").resolve(String.valueOf(incidenteId)).normalize();
        validarDentroDeRaiz(carpetaIncidente);

        String nombreInterno = UUID.randomUUID() + extension;
        Path destino = carpetaIncidente.resolve(nombreInterno);

        try {
            Files.createDirectories(carpetaIncidente);
            Files.copy(archivoOrigen, destino);
            return normalizarClave(raiz.relativize(destino));
        } catch (IOException e) {
            throw new StorageException(
                    "No se pudo copiar la imagen al almacenamiento del sistema.",
                    e
            );
        }
    }

    public File resolver(String rutaGuardada) {
        if (rutaGuardada == null || rutaGuardada.isBlank()) {
            throw new StorageException("La imagen no tiene una ruta de almacenamiento válida.");
        }

        try {
            Path ruta = Path.of(rutaGuardada);
            if (ruta.isAbsolute()) {
                return ruta.normalize().toFile();
            }

            Path resuelta = raiz.resolve(rutaGuardada.replace('/', File.separatorChar)).normalize();
            validarDentroDeRaiz(resuelta);
            return resuelta.toFile();
        } catch (InvalidPathException e) {
            throw new StorageException("La ruta guardada para la imagen no es válida.", e);
        }
    }

    public void eliminar(String rutaGuardada) {
        if (rutaGuardada == null || rutaGuardada.isBlank()) return;

        try {
            Path ruta = Path.of(rutaGuardada);
            if (ruta.isAbsolute()) return;

            Path resuelta = raiz.resolve(rutaGuardada.replace('/', File.separatorChar)).normalize();
            validarDentroDeRaiz(resuelta);
            Files.deleteIfExists(resuelta);
        } catch (IOException | InvalidPathException e) {
            throw new StorageException("No se pudo limpiar la imagen del almacenamiento.", e);
        }
    }

    private static Path obtenerRaizConfigurada() {
        String configurada = System.getenv(VARIABLE_RUTA);
        if (configurada != null && !configurada.isBlank()) {
            return Path.of(configurada);
        }

        return Path.of(System.getProperty("user.home"), "SistemaIncidentes", "imagenes");
    }

    private String obtenerExtension(String nombre) {
        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto).toLowerCase(Locale.ROOT);
    }

    private void validarDentroDeRaiz(Path ruta) {
        if (!ruta.startsWith(raiz)) {
            throw new StorageException("La ruta de imagen está fuera del almacenamiento permitido.");
        }
    }

    private String normalizarClave(Path rutaRelativa) {
        return rutaRelativa.toString().replace(File.separatorChar, '/');
    }
}
