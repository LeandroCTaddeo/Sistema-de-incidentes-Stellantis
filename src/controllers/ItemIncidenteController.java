package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import models.Incidente;

public class ItemIncidenteController {

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblFecha;

    @FXML
    private Label lblEstado;

    public void cargar(Incidente incidente) {

        lblTitulo.setText(incidente.getTitulo());
        lblEstado.setText(incidente.getEstado());
        lblFecha.setText(incidente.getId() + "");

    }

}
