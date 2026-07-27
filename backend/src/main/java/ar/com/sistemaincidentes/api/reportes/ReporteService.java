package ar.com.sistemaincidentes.api.reportes;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReporteService {

    private final ReporteRepository repository;

    public ReporteService(ReporteRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public ReporteResponse obtener(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar las fechas desde y hasta.");
        }
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException(
                    "La fecha desde no puede ser posterior a la fecha hasta."
            );
        }
        return new ReporteResponse(
                repository.obtenerResumen(desde, hasta),
                repository.obtenerPorArea(desde, hasta),
                repository.obtenerPorPrioridad(desde, hasta)
        );
    }
}
