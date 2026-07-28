package ar.com.sistemaincidentes.api.firmantes;

import java.util.List;

public record SeleccionFirmantesRequest(
        Integer administradorId,
        List<Integer> firmanteIds
) {
}
