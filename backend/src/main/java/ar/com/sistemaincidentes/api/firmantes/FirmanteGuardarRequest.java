package ar.com.sistemaincidentes.api.firmantes;

public record FirmanteGuardarRequest(
        String nombre,
        String areaLinea1,
        String areaLinea2,
        String tipo
) {
}
