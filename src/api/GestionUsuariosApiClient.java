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

public class GestionUsuariosApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);
    private static final String HEADER_API_KEY = "X-API-Key";

    private final String urlBase;
    private final String tokenApi;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GestionUsuariosApiClient() {
        this(
                System.getenv().getOrDefault("INCIDENTES_API_URL", "http://127.0.0.1:8080"),
                System.getenv("INCIDENTES_API_TOKEN"),
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
        );
    }

    GestionUsuariosApiClient(String urlBase, String tokenApi, HttpClient httpClient) {
        this.urlBase = quitarBarraFinal(urlBase);
        this.tokenApi = validarToken(tokenApi);
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<UsuarioGestionApiResponse> listar(String busqueda) {
        String ruta = "/api/usuarios";
        if (busqueda != null && !busqueda.isBlank()) {
            ruta += "?buscar=" + URLEncoder.encode(busqueda.trim(), StandardCharsets.UTF_8);
        }

        HttpResponse<String> response = enviar(
                HttpRequest.newBuilder(URI.create(urlBase + ruta))
                        .timeout(TIMEOUT)
                        .header("Accept", "application/json")
                        .header(HEADER_API_KEY, tokenApi)
                        .GET()
                        .build()
        );
        validarEstado(response, 200);
        try {
            return objectMapper.readValue(
                    response.body(),
                    new TypeReference<List<UsuarioGestionApiResponse>>() { }
            );
        } catch (IOException e) {
            throw new IncidenteApiException("La API devolvió una lista de usuarios inválida.", e);
        }
    }

    public UsuarioGestionApiResponse crear(UsuarioGuardarApiRequest request) {
        return enviarJson("/api/usuarios", "POST", request, 201);
    }

    public UsuarioGestionApiResponse actualizar(int id, UsuarioGuardarApiRequest request) {
        validarId(id);
        return enviarJson("/api/usuarios/" + id, "PUT", request, 200);
    }

    public UsuarioGestionApiResponse cambiarEstado(int id, boolean activo) {
        validarId(id);
        return enviarJson(
                "/api/usuarios/" + id + "/estado",
                "PATCH",
                new UsuarioEstadoApiRequest(activo),
                200
        );
    }

    private UsuarioGestionApiResponse enviarJson(
            String ruta,
            String metodo,
            Object cuerpo,
            int estadoEsperado
    ) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(urlBase + ruta))
                    .timeout(TIMEOUT)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .header(HEADER_API_KEY, tokenApi)
                    .method(
                            metodo,
                            HttpRequest.BodyPublishers.ofByteArray(
                                    objectMapper.writeValueAsBytes(cuerpo)
                            )
                    )
                    .build();
            HttpResponse<String> response = enviar(request);
            validarEstado(response, estadoEsperado);
            return objectMapper.readValue(response.body(), UsuarioGestionApiResponse.class);
        } catch (IOException e) {
            throw new IncidenteApiException("No se pudieron procesar los datos del usuario.", e);
        }
    }

    private HttpResponse<String> enviar(HttpRequest request) {
        try {
            return httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IncidenteApiException("La operación de usuarios fue interrumpida.", e);
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo consultar la gestión de usuarios. Verifique que el servidor esté iniciado.",
                    e
            );
        }
    }

    private void validarEstado(HttpResponse<String> response, int esperado) {
        if (response.statusCode() == esperado) return;
        if (response.statusCode() == 401) {
            throw new IncidenteApiException(
                    "La API rechazó la credencial. Verifique INCIDENTES_API_TOKEN."
            );
        }
        if (response.statusCode() == 403) {
            throw new IncidenteApiException("La credencial no tiene permisos de administrador.");
        }
        try {
            ApiErrorResponse error = objectMapper.readValue(response.body(), ApiErrorResponse.class);
            if (error.message() != null && !error.message().isBlank()) {
                throw new IncidenteApiException(error.message());
            }
        } catch (IncidenteApiException e) {
            throw e;
        } catch (Exception ignored) {
            // Si no hay un error JSON válido, se informa el estado HTTP.
        }
        throw new IncidenteApiException(
                "La API respondió con el código HTTP " + response.statusCode() + "."
        );
    }

    private static void validarId(int id) {
        if (id <= 0) {
            throw new IncidenteApiException("El identificador del usuario no es válido.");
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

    private record ApiErrorResponse(String message) {
    }
}
