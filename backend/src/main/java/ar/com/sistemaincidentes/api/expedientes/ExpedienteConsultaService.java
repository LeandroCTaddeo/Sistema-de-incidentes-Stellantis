package ar.com.sistemaincidentes.api.expedientes;

import java.util.List;

import org.springframework.stereotype.Service;

import ar.com.sistemaincidentes.api.incidentes.IncidenteConsultaRepository;
import ar.com.sistemaincidentes.api.incidentes.IncidenteResponse;
import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

@Service
public class ExpedienteConsultaService {

    private final IncidenteConsultaRepository incidenteRepository;
    private final ExpedienteConsultaRepository expedienteRepository;
    private final AlmacenamientoImagenApiService almacenamientoImagenes;

    public ExpedienteConsultaService(
            IncidenteConsultaRepository incidenteRepository,
            ExpedienteConsultaRepository expedienteRepository,
            AlmacenamientoImagenApiService almacenamientoImagenes
    ) {
        this.incidenteRepository = incidenteRepository;
        this.expedienteRepository = expedienteRepository;
        this.almacenamientoImagenes = almacenamientoImagenes;
    }

    public IncidenteResponse obtenerIncidente(int incidenteId) {
        validarId(incidenteId);
        return incidenteRepository.buscarPorId(incidenteId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el incidente solicitado."
                ));
    }

    public List<BoletinInternoResponse> listarBoletines(int incidenteId) {
        obtenerIncidente(incidenteId);
        return expedienteRepository.listarBoletines(incidenteId);
    }

    public List<ImagenAdjuntaResponse> listarImagenes(int incidenteId) {
        obtenerIncidente(incidenteId);
        return expedienteRepository.listarImagenes(incidenteId).stream()
                .map(almacenamientoImagenes::describir)
                .toList();
    }

    public ImagenContenido obtenerImagen(int incidenteId, int imagenId) {
        validarId(incidenteId);
        validarId(imagenId);

        ImagenAdjuntaArchivo imagen = expedienteRepository
                .buscarImagen(incidenteId, imagenId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la imagen solicitada para este incidente."
                ));
        return almacenamientoImagenes.abrir(imagen);
    }

    private void validarId(int id) {
        if (id <= 0) throw new IllegalArgumentException("El identificador debe ser mayor que cero.");
    }
}
