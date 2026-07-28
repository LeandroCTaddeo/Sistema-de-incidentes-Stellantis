package ar.com.sistemaincidentes.api.firmantes;

public record FirmanteResponse(
        int id,
        String nombre,
        String areaLinea1,
        String areaLinea2,
        String planta,
        boolean obligatorio,
        String grupoSeleccion,
        int orden,
        boolean activo
) {
}
