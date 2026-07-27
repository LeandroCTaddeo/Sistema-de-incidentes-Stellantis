package api;

import java.net.URI;

final class ApiAutenticacion {

    private static final String PREFIJO_BEARER = "Bearer ";

    private ApiAutenticacion() {
    }

    static String credencialDesdeEntorno() {
        String accessToken = System.getenv("INCIDENTES_ACCESS_TOKEN");
        if (accessToken != null && !accessToken.isBlank()) {
            return PREFIJO_BEARER + accessToken.trim();
        }

        String apiToken = System.getenv("INCIDENTES_API_TOKEN");
        if (apiToken != null && !apiToken.isBlank()) {
            return apiToken.trim();
        }
        throw new IncidenteApiException(
                "Falta configurar INCIDENTES_ACCESS_TOKEN para SSO "
                + "o INCIDENTES_API_TOKEN para desarrollo local."
        );
    }

    static String nombreCabecera(String credencial) {
        return esBearer(credencial) ? "Authorization" : "X-API-Key";
    }

    static String valorCabecera(String credencial) {
        if (credencial == null || credencial.isBlank()) {
            throw new IncidenteApiException("La credencial de la API no puede estar vacía.");
        }
        return credencial.trim();
    }

    static boolean esBearer(String credencial) {
        return credencial != null && credencial.startsWith(PREFIJO_BEARER);
    }

    static void validarTransporte(String urlBase, String credencial) {
        if (!esBearer(credencial)) return;
        URI uri;
        try {
            uri = URI.create(urlBase);
        } catch (IllegalArgumentException e) {
            throw new IncidenteApiException("INCIDENTES_API_URL no es una URL válida.", e);
        }
        if (!"https".equalsIgnoreCase(uri.getScheme())) {
            throw new IncidenteApiException(
                    "La autenticación corporativa requiere una URL HTTPS."
            );
        }
    }
}
