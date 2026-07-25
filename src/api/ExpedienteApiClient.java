package api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import models.BoletinAdmin;
import models.Imagen;
import models.Incidente;

public class ExpedienteApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String HEADER_API_KEY = "X-API-Key";
    private static final Set<String> EXTENSIONES_PERMITIDAS =
            Set.of(".png", ".jpg", ".jpeg");

    private final String urlBase;
    private final String tokenApi;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Path cacheRaiz;

    public ExpedienteApiClient() {
        this(
                System.getenv().getOrDefault("INCIDENTES_API_URL", "http://127.0.0.1:8080"),
                System.getenv("INCIDENTES_API_TOKEN"),
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build(),
                crearCacheTemporal()
        );
    }

    ExpedienteApiClient(
            String urlBase,
            String tokenApi,
            HttpClient httpClient,
            Path cacheRaiz
    ) {
        this.urlBase = quitarBarraFinal(urlBase);
        this.tokenApi = validarToken(tokenApi);
        this.httpClient = httpClient;
        this.cacheRaiz = cacheRaiz.toAbsolutePath().normalize();
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public Incidente obtenerIncidente(int incidenteId) {
        validarId(incidenteId);
        IncidenteApiResponse response = obtenerJson(
                uri("/api/incidentes/" + incidenteId),
                new TypeReference<IncidenteApiResponse>() { }
        );
        return response.convertir();
    }

    public List<BoletinAdmin> listarBoletines(int incidenteId) {
        validarId(incidenteId);
        List<BoletinAdminApiResponse> respuestas = obtenerJson(
                uri("/api/incidentes/" + incidenteId + "/boletines"),
                new TypeReference<List<BoletinAdminApiResponse>>() { }
        );
        return respuestas.stream().map(BoletinAdminApiResponse::convertir).toList();
    }

    public List<Imagen> listarImagenes(int incidenteId) {
        validarId(incidenteId);
        List<ImagenAdjuntaApiResponse> respuestas = obtenerJson(
                uri("/api/incidentes/" + incidenteId + "/imagenes"),
                new TypeReference<List<ImagenAdjuntaApiResponse>>() { }
        );
        return respuestas.stream().map(this::descargarImagen).toList();
    }

    private Imagen descargarImagen(ImagenAdjuntaApiResponse imagen) {
        validarId(imagen.id());
        validarId(imagen.incidenteId());

        Path destino = prepararDestino(imagen);
        HttpRequest request = solicitud(
                uri("/api/incidentes/" + imagen.incidenteId()
                        + "/imagenes/" + imagen.id() + "/contenido"),
                "image/png, image/jpeg"
        );

        try {
            HttpResponse<Path> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofFile(destino)
            );
            validarRespuesta(response.statusCode());
            destino.toFile().deleteOnExit();
            return new Imagen(imagen.id(), imagen.incidenteId(), destino.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            eliminarTemporal(destino);
            throw new IncidenteApiException("La descarga de la imagen fue interrumpida.", e);
        } catch (IOException e) {
            eliminarTemporal(destino);
            throw new IncidenteApiException(
                    "No se pudo descargar una imagen del expediente.",
                    e
            );
        } catch (RuntimeException e) {
            eliminarTemporal(destino);
            throw e;
        }
    }

    private <T> T obtenerJson(URI uri, TypeReference<T> tipo) {
        try {
            HttpResponse<String> response = httpClient.send(
                    solicitud(uri, "application/json"),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            validarRespuesta(response.statusCode());
            return objectMapper.readValue(response.body(), tipo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IncidenteApiException("La consulta del expediente fue interrumpida.", e);
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo consultar el expediente mediante la API.",
                    e
            );
        }
    }

    private HttpRequest solicitud(URI uri, String accept) {
        return HttpRequest.newBuilder(uri)
                .timeout(TIMEOUT)
                .header("Accept", accept)
                .header(HEADER_API_KEY, tokenApi)
                .GET()
                .build();
    }

    private void validarRespuesta(int estado) {
        if (estado == 401) {
            throw new IncidenteApiException(
                    "La API rechazó la credencial. Verifique INCIDENTES_API_TOKEN."
            );
        }
        if (estado == 403) {
            throw new IncidenteApiException("La credencial no tiene permisos de administrador.");
        }
        if (estado == 404) {
            throw new IncidenteApiException("No se encontró el recurso solicitado en la API.");
        }
        if (estado != 200) {
            throw new IncidenteApiException(
                    "La API respondió con el código HTTP " + estado + "."
            );
        }
    }

    private Path prepararDestino(ImagenAdjuntaApiResponse imagen) {
        String extension = extensionSegura(imagen.nombreArchivo());
        String nombre = imagen.id() + "-" + UUID.randomUUID() + extension;
        Path carpeta = cacheRaiz.resolve(String.valueOf(imagen.incidenteId())).normalize();
        Path destino = carpeta.resolve(nombre).normalize();

        if (!destino.startsWith(cacheRaiz)) {
            throw new IncidenteApiException("La ruta temporal de la imagen no es válida.");
        }

        try {
            Files.createDirectories(carpeta);
            carpeta.toFile().deleteOnExit();
            return destino;
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo preparar la caché temporal de imágenes.",
                    e
            );
        }
    }

    private String extensionSegura(String nombreArchivo) {
        String nombre = nombreArchivo == null ? "" : nombreArchivo.toLowerCase(Locale.ROOT);
        for (String extension : EXTENSIONES_PERMITIDAS) {
            if (nombre.endsWith(extension)) return extension;
        }
        throw new IncidenteApiException("La API informó un formato de imagen no permitido.");
    }

    private URI uri(String ruta) {
        return URI.create(urlBase + ruta);
    }

    private static Path crearCacheTemporal() {
        try {
            return Files.createTempDirectory("sistema-incidentes-api-");
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo crear la caché temporal del expediente.",
                    e
            );
        }
    }

    private static void eliminarTemporal(Path archivo) {
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException ignored) {
            // La limpieza no debe ocultar el error principal.
        }
    }

    private static String quitarBarraFinal(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IncidenteApiException("INCIDENTES_API_URL no puede estar vacía.");
        }
        return valor.endsWith("/") ? valor.substring(0, valor.length() - 1) : valor;
    }

    private static String validarToken(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IncidenteApiException(
                    "Falta configurar la variable de entorno INCIDENTES_API_TOKEN."
            );
        }
        return valor.trim();
    }

    private static void validarId(int id) {
        if (id <= 0) {
            throw new IncidenteApiException("El identificador debe ser mayor que cero.");
        }
    }
}
