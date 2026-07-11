package services;

import dao.IncidenteDAO;
import models.Incidente;

public class IncidenteService {

    private IncidenteDAO dao = new IncidenteDAO();

    public int guardar(Incidente incidente) {

        return dao.guardar(incidente);

    }

}