package api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import models.Incidente;

public class IncidenteApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final String HEADER_API_KEY = "X-API-Key";

    private final String urlBase;
    private final String tokenApi;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public IncidenteApiClient() {
        this(
                System.getenv().getOrDefault("INCIDENTES_API_URL", "http://127.0.0.1:8080"),
                System.getenv("INCIDENTES_API_TOKEN"),
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
        );
    }

    IncidenteApiClient(String urlBase, String tokenApi, HttpClient httpClient) {
        this.urlBase = quitarBarraFinal(urlBase);
        this.tokenApi = validarToken(tokenApi);
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<Incidente> listar(String estado) {
        URI uri = crearUri(estado);
        HttpRequest request = HttpRequest.newBuilder(uri)
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

            if (response.statusCode() == 401) {
                throw new IncidenteApiException(
                        "La API rechazó la credencial. Verifique INCIDENTES_API_TOKEN."
                );
            }
            if (response.statusCode() == 403) {
                throw new IncidenteApiException("La credencial no tiene permisos de administrador.");
            }
            if (response.statusCode() != 200) {
                throw new IncidenteApiException(
                        "La API respondió con el código HTTP " + response.statusCode() + "."
                );
            }

            List<IncidenteApiResponse> incidentes = objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<IncidenteApiResponse>>() { }
            );
            return incidentes.stream().map(IncidenteApiResponse::convertir).toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IncidenteApiException("La consulta de incidentes fue interrumpida.", e);
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo consultar la API de incidentes. Verifique que el servidor esté iniciado.",
                    e
            );
        }
    }

    private URI crearUri(String estado) {
        String ruta = urlBase + "/api/incidentes";
        if (estado == null || estado.isBlank()) {
            return URI.create(ruta);
        }

        return URI.create(
                ruta + "?estado=" + URLEncoder.encode(estado, StandardCharsets.UTF_8)
        );
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
}
