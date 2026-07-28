package api;

public record FirmanteApiResponse(
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
    public String tipo() {
        return obligatorio ? "OBLIGATORIO" : "ALTERNATIVA";
    }
}
