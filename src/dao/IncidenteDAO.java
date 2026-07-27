package dao;

import java.time.LocalDate;
import java.util.List;

import api.AdministracionApiClient;
import api.IncidenteApiClient;
import models.Incidente;

public class IncidenteDAO {

    private IncidenteApiClient apiClient;
    private AdministracionApiClient administracionApiClient;

    private IncidenteApiClient apiClient() {
        if (apiClient == null) {
            apiClient = new IncidenteApiClient();
        }
        return apiClient;
    }

    private AdministracionApiClient administracionApiClient() {
        if (administracionApiClient == null) {
            administracionApiClient = new AdministracionApiClient();
        }
        return administracionApiClient;
    }

    public List<Incidente> obtenerTodos() {
        return apiClient().listar(null);
    }

    public List<Incidente> obtenerPendientes() {
        return apiClient().listar("PENDIENTE");
    }

    public List<Incidente> obtenerResueltos() {
        return apiClient().listar("RESUELTO");
    }

    public List<Incidente> buscarResueltos(String texto, LocalDate desde, LocalDate hasta) {
        return apiClient().buscarResueltos(texto, desde, hasta);
    }

    public boolean resolver(int id, int administradorId) {
        return administracionApiClient().resolverIncidente(id, administradorId);
    }
}
