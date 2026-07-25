package ar.com.sistemaincidentes.api.expedientes;

public record ImagenAdjuntaResponse(
        int id,
        int incidenteId,
        String nombreArchivo,
        String contentType
) {
}
