package api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Usuario;

public class UsuarioApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final String HEADER_API_KEY = "X-API-Key";

    private final String urlBase;
    private final String tokenApi;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public UsuarioApiClient() {
        this(
                System.getenv().getOrDefault("INCIDENTES_API_URL", "http://127.0.0.1:8080"),
                System.getenv("INCIDENTES_API_TOKEN"),
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
        );
    }

    UsuarioApiClient(String urlBase, String tokenApi, HttpClient httpClient) {
        this.urlBase = quitarBarraFinal(urlBase);
        this.tokenApi = validarToken(tokenApi);
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public Usuario obtenerPorWindows(String usuarioWindows) {
        if (usuarioWindows == null || usuarioWindows.isBlank()) {
            throw new IncidenteApiException("No se pudo identificar el usuario de Windows.");
        }
        String consulta = URLEncoder.encode(usuarioWindows.trim(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder(
                        URI.create(urlBase + "/api/usuarios/actual?usuarioWindows=" + consulta)
                )
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .header(HEADER_API_KEY, tokenApi)
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() == 404) return null;
            if (response.statusCode() != 200) {
                throw new IncidenteApiException(mensajeError(response));
            }
            return objectMapper.readValue(response.body(), UsuarioApiResponse.class).convertir();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IncidenteApiException("La identificación del usuario fue interrumpida.", e);
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo identificar el usuario mediante la API. "
                    + "Verifique que el servidor esté iniciado.",
                    e
            );
        }
    }

    private String mensajeError(HttpResponse<String> response) {
        if (response.statusCode() == 401) {
            return "La API rechazó la credencial. Verifique INCIDENTES_API_TOKEN.";
        }
        if (response.statusCode() == 403) {
            return "La credencial no tiene permiso para identificar usuarios.";
        }
        try {
            ApiErrorResponse error = objectMapper.readValue(response.body(), ApiErrorResponse.class);
            if (error.message() != null && !error.message().isBlank()) return error.message();
        } catch (Exception ignored) {
            // Si el cuerpo no tiene el formato esperado, se informa el estado HTTP.
        }
        return "La API respondió con el código HTTP " + response.statusCode() + ".";
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

    private record ApiErrorResponse(String message) {
    }
}
