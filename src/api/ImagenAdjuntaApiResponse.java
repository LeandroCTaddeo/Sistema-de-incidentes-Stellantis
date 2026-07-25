package api;

public record ImagenAdjuntaApiResponse(
        int id,
        int incidenteId,
        String nombreArchivo,
        String contentType
) {
}
