package dao;

import java.util.List;

import api.AdministracionApiClient;
import api.ExpedienteApiClient;
import models.BoletinAdmin;

public class BoletinAdminDAO {

    private ExpedienteApiClient expedienteApiClient;
    private AdministracionApiClient administracionApiClient;

    private ExpedienteApiClient expedienteApiClient() {
        if (expedienteApiClient == null) {
            expedienteApiClient = new ExpedienteApiClient();
        }
        return expedienteApiClient;
    }

    private AdministracionApiClient administracionApiClient() {
        if (administracionApiClient == null) {
            administracionApiClient = new AdministracionApiClient();
        }
        return administracionApiClient;
    }

    public int guardar(BoletinAdmin boletin) {
        return administracionApiClient().crearBoletin(boletin);
    }

    public void actualizar(BoletinAdmin boletin) {
        administracionApiClient().actualizarBoletin(boletin);
    }

    public List<BoletinAdmin> obtenerPorIncidente(int incidenteId) {
        return expedienteApiClient().listarBoletines(incidenteId);
    }
}
