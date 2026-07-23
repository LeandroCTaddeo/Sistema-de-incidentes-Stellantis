package ar.com.sistemaincidentes.api.incidentes;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class IncidenteConsultaService {

    private final IncidenteConsultaRepository repository;

    public IncidenteConsultaService(IncidenteConsultaRepository repository) {
        this.repository = repository;
    }

    public List<IncidenteResponse> listar(String estado) {
        return repository.listar(EstadoIncidente.desdeParametro(estado));
    }
}
