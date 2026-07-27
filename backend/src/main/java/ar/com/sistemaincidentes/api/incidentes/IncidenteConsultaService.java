package ar.com.sistemaincidentes.api.incidentes;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

@Service
public class IncidenteConsultaService {

    private final IncidenteConsultaRepository repository;

    public IncidenteConsultaService(IncidenteConsultaRepository repository) {
        this.repository = repository;
    }

    public List<IncidenteResponse> listar(String estado) {
        return listar(estado, null, null, null);
    }

    public List<IncidenteResponse> listar(
            String estado,
            String texto,
            LocalDate desde,
            LocalDate hasta
    ) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta."
            );
        }
        return repository.listar(
                EstadoIncidente.desdeParametro(estado),
                normalizarTexto(texto),
                desde,
                hasta
        );
    }

    public List<IncidenteResponse> listarAsignados(
            String estado,
            String texto,
            LocalDate desde,
            LocalDate hasta,
            Integer asignadoA
    ) {
        if (asignadoA == null || asignadoA <= 0) {
            throw new IllegalArgumentException("El administrador asignado no es válido.");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta."
            );
        }
        return repository.listar(
                EstadoIncidente.desdeParametro(estado),
                normalizarTexto(texto),
                desde,
                hasta,
                asignadoA
        );
    }

    private String normalizarTexto(String valor) {
        if (valor == null) return "";
        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
