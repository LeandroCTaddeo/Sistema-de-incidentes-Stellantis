package dao;

import java.util.List;

import api.ExpedienteApiClient;
import models.Imagen;

public class ImagenDAO {

    private ExpedienteApiClient apiClient;

    private ExpedienteApiClient apiClient() {
        if (apiClient == null) {
            apiClient = new ExpedienteApiClient();
        }
        return apiClient;
    }

    public List<Imagen> obtenerPorIncidente(int incidenteId) {
        return apiClient().listarImagenes(incidenteId);
    }
}
