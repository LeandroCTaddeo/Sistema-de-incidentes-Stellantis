package services;

import java.io.File;
import java.util.List;

import api.IncidenteApiClient;
import models.Incidente;

public class IncidenteService {

    private IncidenteApiClient apiClient;

    public int guardarConImagenes(Incidente incidente, List<File> imagenes) {
        return apiClient().crear(incidente, imagenes).id();
    }

    private IncidenteApiClient apiClient() {
        if (apiClient == null) apiClient = new IncidenteApiClient();
        return apiClient;
    }

}
