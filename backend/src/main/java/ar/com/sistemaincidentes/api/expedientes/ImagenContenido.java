package ar.com.sistemaincidentes.api.expedientes;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record ImagenContenido(
        Resource recurso,
        String nombreArchivo,
        MediaType mediaType,
        long longitud
) {
}
