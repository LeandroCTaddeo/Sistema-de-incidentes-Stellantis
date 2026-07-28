package api;

import java.util.List;

public record SeleccionFirmantesApiRequest(
        int administradorId,
        List<Integer> firmanteIds
) {
}
