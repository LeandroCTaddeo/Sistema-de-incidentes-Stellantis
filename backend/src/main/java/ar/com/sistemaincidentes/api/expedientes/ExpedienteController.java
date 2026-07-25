package ar.com.sistemaincidentes.api.expedientes;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.com.sistemaincidentes.api.incidentes.IncidenteResponse;

@RestController
@RequestMapping("/api/incidentes/{incidenteId}")
public class ExpedienteController {

    private final ExpedienteConsultaService service;

    public ExpedienteController(ExpedienteConsultaService service) {
        this.service = service;
    }

    @GetMapping
    public IncidenteResponse obtener(@PathVariable int incidenteId) {
        return service.obtenerIncidente(incidenteId);
    }

    @GetMapping("/boletines")
    public List<BoletinInternoResponse> listarBoletines(@PathVariable int incidenteId) {
        return service.listarBoletines(incidenteId);
    }

    @GetMapping("/imagenes")
    public List<ImagenAdjuntaResponse> listarImagenes(@PathVariable int incidenteId) {
        return service.listarImagenes(incidenteId);
    }

    @GetMapping("/imagenes/{imagenId}/contenido")
    public ResponseEntity<org.springframework.core.io.Resource> obtenerImagen(
            @PathVariable int incidenteId,
            @PathVariable int imagenId
    ) {
        ImagenContenido contenido = service.obtenerImagen(incidenteId, imagenId);
        ContentDisposition disposicion = ContentDisposition.inline()
                .filename(contenido.nombreArchivo(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(contenido.mediaType())
                .contentLength(contenido.longitud())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposicion.toString())
                .body(contenido.recurso());
    }
}
