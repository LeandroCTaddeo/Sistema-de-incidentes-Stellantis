package api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import models.Incidente;

public class IncidenteApiClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private static final Duration TIMEOUT_ENVIO = Duration.ofMinutes(2);
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
        return consultar(crearUri(estado, null, null, null, null));
    }

    public List<Incidente> listarAsignados(String estado, int administradorId) {
        if (administradorId <= 0) {
            throw new IncidenteApiException("El administrador asignado no es válido.");
        }
        return consultar(crearUri(estado, null, null, null, administradorId));
    }

    public List<Incidente> buscarResueltos(
            String texto,
            LocalDate desde,
            LocalDate hasta
    ) {
        return consultar(crearUri("RESUELTO", texto, desde, hasta, null));
    }

    private List<Incidente> consultar(URI uri) {
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

    public IncidenteCreadoApiResponse crear(Incidente incidente, List<File> imagenes) {
        String limite = "----SistemaIncidentes-" + UUID.randomUUID();

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(urlBase + "/api/incidentes"))
                    .timeout(TIMEOUT_ENVIO)
                    .header("Accept", "application/json")
                    .header(HEADER_API_KEY, tokenApi)
                    .header("Content-Type", "multipart/form-data; boundary=" + limite)
                    .POST(crearCuerpoMultipart(incidente, imagenes, limite))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );
            if (response.statusCode() != 201) {
                throw new IncidenteApiException(mensajeError(response));
            }

            IncidenteCreadoApiResponse resultado = objectMapper.readValue(
                    response.body(),
                    IncidenteCreadoApiResponse.class
            );
            if (resultado.id() <= 0) {
                throw new IncidenteApiException(
                        "La API no devolvió un identificador válido para el incidente."
                );
            }
            return resultado;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IncidenteApiException("El envío del boletín fue interrumpido.", e);
        } catch (IOException e) {
            throw new IncidenteApiException(
                    "No se pudo enviar el boletín mediante la API. "
                    + "Verifique que el servidor esté iniciado.",
                    e
            );
        }
    }

    private HttpRequest.BodyPublisher crearCuerpoMultipart(
            Incidente incidente,
            List<File> imagenes,
            String limite
    ) throws IOException {
        List<HttpRequest.BodyPublisher> partes = new ArrayList<>();
        byte[] json = objectMapper.writeValueAsBytes(IncidenteCreacionApiRequest.desde(incidente));

        partes.add(texto(
                "--" + limite + "\r\n"
                + "Content-Disposition: form-data; name=\"incidente\"\r\n"
                + "Content-Type: application/json; charset=UTF-8\r\n\r\n"
        ));
        partes.add(HttpRequest.BodyPublishers.ofByteArray(json));
        partes.add(texto("\r\n"));

        for (File imagen : imagenes == null ? List.<File>of() : imagenes) {
            if (imagen == null || !Files.isRegularFile(imagen.toPath())) {
                throw new IncidenteApiException("No se encontró una de las imágenes seleccionadas.");
            }

            String nombre = nombreArchivoSeguro(imagen.getName());
            String tipo = tipoContenido(nombre);
            partes.add(texto(
                    "--" + limite + "\r\n"
                    + "Content-Disposition: form-data; name=\"imagenes\"; filename=\""
                    + nombre + "\"\r\n"
                    + "Content-Type: " + tipo + "\r\n\r\n"
            ));
            partes.add(HttpRequest.BodyPublishers.ofFile(imagen.toPath()));
            partes.add(texto("\r\n"));
        }

        partes.add(texto("--" + limite + "--\r\n"));
        return HttpRequest.BodyPublishers.concat(
                partes.toArray(HttpRequest.BodyPublisher[]::new)
        );
    }

    private HttpRequest.BodyPublisher texto(String valor) {
        return HttpRequest.BodyPublishers.ofString(valor, StandardCharsets.UTF_8);
    }

    private String nombreArchivoSeguro(String nombre) {
        String seguro = nombre == null ? "imagen" : nombre
                .replaceAll("[\\r\\n\"\\\\]", "_")
                .replaceAll("[^\\p{ASCII}]", "_");
        return seguro.isBlank() ? "imagen" : seguro;
    }

    private String tipoContenido(String nombre) {
        String minusculas = nombre.toLowerCase(Locale.ROOT);
        if (minusculas.endsWith(".png")) return "image/png";
        if (minusculas.endsWith(".jpg") || minusculas.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        throw new IncidenteApiException("Sólo se permiten imágenes PNG o JPEG.");
    }

    private String mensajeError(HttpResponse<String> response) {
        if (response.statusCode() == 401) {
            return "La API rechazó la credencial. Verifique INCIDENTES_API_TOKEN.";
        }
        if (response.statusCode() == 403) {
            return "La credencial no tiene permiso para enviar incidentes.";
        }

        try {
            ApiErrorResponse error = objectMapper.readValue(
                    response.body(),
                    ApiErrorResponse.class
            );
            if (error.message() != null && !error.message().isBlank()) {
                return error.message();
            }
        } catch (Exception ignored) {
            // Si el cuerpo no tiene el formato esperado, se informa el estado HTTP.
        }
        return "La API respondió con el código HTTP " + response.statusCode() + ".";
    }

    private record ApiErrorResponse(String message) {
    }

    private URI crearUri(
            String estado,
            String texto,
            LocalDate desde,
            LocalDate hasta,
            Integer asignadoA
    ) {
        String ruta = urlBase + "/api/incidentes";
        List<String> parametros = new ArrayList<>();
        agregarParametro(parametros, "estado", estado);
        agregarParametro(parametros, "texto", texto);
        agregarParametro(parametros, "desde", desde == null ? null : desde.toString());
        agregarParametro(parametros, "hasta", hasta == null ? null : hasta.toString());
        agregarParametro(parametros, "asignadoA", asignadoA == null ? null : asignadoA.toString());

        return URI.create(parametros.isEmpty() ? ruta : ruta + "?" + String.join("&", parametros));
    }

    private void agregarParametro(List<String> parametros, String nombre, String valor) {
        if (valor == null || valor.isBlank()) return;
        parametros.add(
                URLEncoder.encode(nombre, StandardCharsets.UTF_8)
                + "="
                + URLEncoder.encode(valor.trim(), StandardCharsets.UTF_8)
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
