package api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import models.BoletinAdmin;

public class AdministracionApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final String HEADER_API_KEY = "X-API-Key";

    private final String urlBase;
    private final String tokenApi;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AdministracionApiClient() {
        this(
                System.getenv().getOrDefault("INCIDENTES_API_URL", "http://127.0.0.1:8080"),
                System.getenv("INCIDENTES_API_TOKEN"),
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
        );
    }

    AdministracionApiClient(String urlBase, String tokenApi, HttpClient httpClient) {
        this.urlBase = quitarBarraFinal(urlBase);
        this.tokenApi = validarToken(tokenApi);
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public int crearBoletin(BoletinAdmin boletin) {
        validarBoletin(boletin);
        BoletinAdminGuardadoApiResponse response = enviarJson(
                "/api/incidentes/" + boletin.getIncidenteId() + "/boletines",
                "POST",
                BoletinAdminEscrituraApiRequest.desde(boletin),
                201,
                BoletinAdminGuardadoApiResponse.class
        );
        validarIdRespuesta(response.id(), "boletín");
        return response.id();
    }

    public void actualizarBoletin(BoletinAdmin boletin) {
        validarBoletin(boletin);
        validarId(boletin.getId(), "boletín");
        BoletinAdminGuardadoApiResponse response = enviarJson(
                "/api/incidentes/" + boletin.getIncidenteId()
                        + "/boletines/" + boletin.getId(),
                "PUT",
                BoletinAdminEscrituraApiRequest.desde(boletin),
                200,
                BoletinAdminGuardadoApiResponse.class
        );
        if (response.id() != boletin.getId()) {
            throw new IncidenteApiException(
                    "La API devolvió un identificador inesperado para el boletín."
            );
        }
    }

    public boolean resolverIncidente(int incidenteId, int administradorId) {
        validarId(incidenteId, "incidente");
        validarId(administradorId, "administrador");
        IncidenteResueltoApiResponse response = enviarJson(
                "/api/incidentes/" + incidenteId + "/resolucion",
                "PATCH",
                new ResolucionIncidenteApiRequest(administradorId),
                200,
                IncidenteResueltoApiResponse.class
        );
        return response.id() == incidenteId && "RESUELTO".equalsIgnoreCase(response.estado());
    }

    public AsignacionIncidenteApiResponse tomarIncidente(int incidenteId, int administradorId) {
        validarId(incidenteId, "incidente");
        validarId(administradorId, "administrador");
        return enviarJson(
                "/api/incidentes/" + incidenteId + "/asignacion",
                "POST",
                new AsignacionIncidenteApiRequest(administradorId),
                200,
                AsignacionIncidenteApiResponse.class
        );
    }

    public AsignacionIncidenteApiResponse liberarIncidente(int incidenteId, int administradorId) {
        validarId(incidenteId, "incidente");
        validarId(administradorId, "administrador");
        return enviarJson(
                "/api/incidentes/" + incidenteId + "/asignacion",
                "DELETE",
                new AsignacionIncidenteApiRequest(administradorId),
                200,
                AsignacionIncidenteApiResponse.class
        );
    }

    private <T> T enviarJson(
            String ruta,
            String metodo,
            Object cuerpo,
            int estadoEsperado,
            Class<T> tipoRespuesta
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
            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() != estadoEsperado) {
                throw new IncidenteApiException(mensajeError(response));
            }
            return objectMapper.readValue(response.body(), tipoRespuesta);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IncidenteApiException("La operación administrativa fue interrumpida.", e);
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo completar la operación mediante la API. "
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
            return "La credencial no tiene permisos de administrador.";
        }
        try {
            ApiErrorResponse error = objectMapper.readValue(response.body(), ApiErrorResponse.class);
            if (error.message() != null && !error.message().isBlank()) return error.message();
        } catch (Exception ignored) {
            // Si el cuerpo no tiene el formato esperado, se informa el estado HTTP.
        }
        return "La API respondió con el código HTTP " + response.statusCode() + ".";
    }

    private void validarBoletin(BoletinAdmin boletin) {
        if (boletin == null) throw new IncidenteApiException("El boletín no puede estar vacío.");
        validarId(boletin.getIncidenteId(), "incidente");
        validarId(boletin.getAdministradorId(), "administrador");
    }

    private void validarIdRespuesta(int id, String nombre) {
        if (id <= 0) {
            throw new IncidenteApiException(
                    "La API no devolvió un identificador válido para el " + nombre + "."
            );
        }
    }

    private void validarId(int id, String nombre) {
        if (id <= 0) {
            throw new IncidenteApiException("El identificador del " + nombre + " no es válido.");
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
