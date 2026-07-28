package api;

public record FirmanteGuardarApiRequest(
        String nombre,
        String areaLinea1,
        String areaLinea2,
        String tipo
) {
}
