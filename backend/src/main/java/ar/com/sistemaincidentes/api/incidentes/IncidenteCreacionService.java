package ar.com.sistemaincidentes.api.incidentes;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class IncidenteCreacionService {

    private final IncidenteEscrituraRepository repository;
    private final AlmacenamientoImagenEscrituraService almacenamiento;
    private final TransactionTemplate transacciones;
    private final int maximoImagenes;

    public IncidenteCreacionService(
            IncidenteEscrituraRepository repository,
            AlmacenamientoImagenEscrituraService almacenamiento,
            TransactionTemplate transacciones,
            @Value("${api.storage.max-images-per-incident:10}") int maximoImagenes
    ) {
        this.repository = repository;
        this.almacenamiento = almacenamiento;
        this.transacciones = transacciones;
        this.maximoImagenes = maximoImagenes;
    }

    public IncidenteCreadoResponse crear(
            IncidenteCreacionRequest incidente,
            List<MultipartFile> imagenes
    ) {
        List<MultipartFile> adjuntos = imagenes == null ? List.of() : imagenes;
        if (adjuntos.size() > maximoImagenes) {
            throw new IllegalArgumentException(
                    "Se permiten como máximo " + maximoImagenes + " imágenes por incidente."
            );
        }

        List<ImagenGuardada> guardadas = new ArrayList<>();
        try {
            IncidenteCreadoResponse resultado = transacciones.execute(estado -> {
                if (!repository.existeUsuario(incidente.usuarioId())) {
                    throw new IllegalArgumentException(
                            "El usuario que envía el incidente no está registrado."
                    );
                }

                int incidenteId = repository.insertarIncidente(incidente);
                for (MultipartFile adjunto : adjuntos) {
                    ImagenGuardada guardada = almacenamiento.almacenar(adjunto, incidenteId);
                    guardadas.add(guardada);
                    repository.insertarImagen(incidenteId, guardada.rutaRelativa());
                }
                return new IncidenteCreadoResponse(incidenteId, guardadas.size());
            });

            if (resultado == null) {
                throw new IllegalStateException("No se pudo completar la creación del incidente.");
            }
            return resultado;
        } catch (RuntimeException e) {
            guardadas.forEach(almacenamiento::eliminarSilenciosamente);
            throw e;
        }
    }
}
