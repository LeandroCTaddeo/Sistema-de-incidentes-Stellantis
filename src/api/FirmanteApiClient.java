package api;

import java.io.IOException;
import java.net.URI;
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

public class FirmanteApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    private final String urlBase;
    private final String credencial;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public FirmanteApiClient() {
        this(
                System.getenv().getOrDefault(
                        "INCIDENTES_API_URL", "http://127.0.0.1:8080"
                ),
                ApiAutenticacion.credencialDesdeEntorno(),
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
        );
    }

    FirmanteApiClient(String urlBase, String credencial, HttpClient httpClient) {
        this.urlBase = quitarBarraFinal(urlBase);
        this.credencial = validarCredencial(credencial);
        ApiAutenticacion.validarTransporte(this.urlBase, this.credencial);
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<FirmanteApiResponse> listar(boolean incluirInactivos) {
        HttpResponse<String> response = enviar(
                solicitud("/api/firmantes?incluirInactivos=" + incluirInactivos)
                        .GET()
                        .build()
        );
        validarEstado(response, 200);
        return leerLista(response.body(), new TypeReference<List<FirmanteApiResponse>>() { });
    }

    public FirmanteApiResponse crear(FirmanteGuardarApiRequest request) {
        return enviarFirmante("/api/firmantes", "POST", request, 201);
    }

    public FirmanteApiResponse actualizar(int id, FirmanteGuardarApiRequest request) {
        validarId(id, "firmante");
        return enviarFirmante("/api/firmantes/" + id, "PUT", request, 200);
    }

    public FirmanteApiResponse cambiarEstado(int id, boolean activo) {
        validarId(id, "firmante");
        return enviarFirmante(
                "/api/firmantes/" + id + "/estado",
                "PATCH",
                new FirmanteEstadoApiRequest(activo),
                200
        );
    }

    public List<FirmaExpedienteApiResponse> obtenerSeleccion(int incidenteId) {
        validarId(incidenteId, "expediente");
        HttpResponse<String> response = enviar(
                solicitud("/api/incidentes/" + incidenteId + "/firmas")
                        .GET()
                        .build()
        );
        validarEstado(response, 200);
        return leerLista(
                response.body(),
                new TypeReference<List<FirmaExpedienteApiResponse>>() { }
        );
    }

    public List<FirmaExpedienteApiResponse> seleccionar(
            int incidenteId,
            int administradorId,
            List<Integer> firmanteIds
    ) {
        validarId(incidenteId, "expediente");
        validarId(administradorId, "administrador");
        try {
            HttpRequest request = solicitud("/api/incidentes/" + incidenteId + "/firmas")
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(
                            objectMapper.writeValueAsBytes(
                                    new SeleccionFirmantesApiRequest(
                                            administradorId, List.copyOf(firmanteIds)
                                    )
                            )
                    ))
                    .build();
            HttpResponse<String> response = enviar(request);
            validarEstado(response, 201);
            return leerLista(
                    response.body(),
                    new TypeReference<List<FirmaExpedienteApiResponse>>() { }
            );
        } catch (IOException e) {
            throw new IncidenteApiException("No se pudo guardar la selección de firmantes.", e);
        }
    }

    private FirmanteApiResponse enviarFirmante(
            String ruta,
            String metodo,
            Object cuerpo,
            int estadoEsperado
    ) {
        try {
            HttpRequest request = solicitud(ruta)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .method(
                            metodo,
                            HttpRequest.BodyPublishers.ofByteArray(
                                    objectMapper.writeValueAsBytes(cuerpo)
                            )
                    )
                    .build();
            HttpResponse<String> response = enviar(request);
            validarEstado(response, estadoEsperado);
            return objectMapper.readValue(response.body(), FirmanteApiResponse.class);
        } catch (IOException e) {
            throw new IncidenteApiException("No se pudieron procesar los datos del firmante.", e);
        }
    }

    private HttpRequest.Builder solicitud(String ruta) {
        return HttpRequest.newBuilder(URI.create(urlBase + ruta))
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .header(
                        ApiAutenticacion.nombreCabecera(credencial),
                        ApiAutenticacion.valorCabecera(credencial)
                );
    }

    private HttpResponse<String> enviar(HttpRequest request) {
        try {
            return httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IncidenteApiException("La operación de firmantes fue interrumpida.", e);
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo consultar la gestión de firmantes. Verifique el servidor.",
                    e
            );
        }
    }

    private <T> List<T> leerLista(String cuerpo, TypeReference<List<T>> tipo) {
        try {
            return objectMapper.readValue(cuerpo, tipo);
        } catch (IOException e) {
            throw new IncidenteApiException("La API devolvió una lista de firmantes inválida.", e);
        }
    }

    private void validarEstado(HttpResponse<String> response, int esperado) {
        if (response.statusCode() == esperado) return;
        if (response.statusCode() == 401) {
            throw new IncidenteApiException("La API rechazó la credencial configurada.");
        }
        if (response.statusCode() == 403) {
            throw new IncidenteApiException(
                    "La credencial no tiene permisos para gestionar firmantes."
            );
        }
        try {
            ApiErrorResponse error = objectMapper.readValue(
                    response.body(), ApiErrorResponse.class
            );
            if (error.message() != null && !error.message().isBlank()) {
                throw new IncidenteApiException(error.message());
            }
        } catch (IncidenteApiException e) {
            throw e;
        } catch (Exception ignored) {
            // Si la respuesta no contiene un error JSON válido, se informa el estado HTTP.
        }
        throw new IncidenteApiException(
                "La API respondió con el código HTTP " + response.statusCode() + "."
        );
    }

    private static void validarId(int id, String tipo) {
        if (id <= 0) {
            throw new IncidenteApiException("El identificador del " + tipo + " no es válido.");
        }
    }

    private static String quitarBarraFinal(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IncidenteApiException("INCIDENTES_API_URL no puede estar vacía.");
        }
        return valor.endsWith("/") ? valor.substring(0, valor.length() - 1) : valor;
    }

    private static String validarCredencial(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IncidenteApiException("Falta configurar la credencial de la API.");
        }
        return valor.trim();
    }

    private record ApiErrorResponse(String message) {
    }
}
