package ar.com.sistemaincidentes.api.expedientes;

record ImagenAdjuntaArchivo(
        int id,
        int incidenteId,
        String ruta
) {
}
