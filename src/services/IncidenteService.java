package services;

import java.io.File;
import java.util.List;

import api.IncidenteApiClient;
import dao.IncidenteDAO;
import models.Incidente;

public class IncidenteService {

    private IncidenteDAO dao = new IncidenteDAO();
    private IncidenteApiClient apiClient;

    public int guardar(Incidente incidente) {

        return dao.guardar(incidente);

    }

    public int guardarConImagenes(Incidente incidente, List<File> imagenes) {
        return apiClient().crear(incidente, imagenes).id();
    }

    public boolean usarApiParaEscrituras() {
        return "API".equalsIgnoreCase(
                System.getenv().getOrDefault("INCIDENTES_DATA_SOURCE", "JDBC")
        );
    }

    private IncidenteApiClient apiClient() {
        if (apiClient == null) apiClient = new IncidenteApiClient();
        return apiClient;
    }

}
