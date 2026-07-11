package controllers;

import dao.IncidenteDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import models.Incidente;

public class DetalleIncidenteController {

    @FXML
    private Label lblTitulo;

    @FXML
    private Label lblPrioridad;

    @FXML
    private Label lblEstado;

    @FXML
    private TextArea txtDescripcion;

    @FXML
    private Button btnResolver;

    private Incidente incidente;

    private IncidenteDAO dao = new IncidenteDAO();

    @FXML
    public void initialize() {

        btnResolver.setOnAction(e -> resolver());

    }

    public void cargarIncidente(Incidente incidente) {

        this.incidente = incidente;

        lblTitulo.setText(incidente.getTitulo());
        txtDescripcion.setText(incidente.getDescripcion());
        lblPrioridad.setText(incidente.getPrioridad().toString());
        lblEstado.setText(incidente.getEstado());

    }

    private void resolver() {

        dao.resolver(incidente.getId());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Incidente");
        alert.setHeaderText(null);
        alert.setContentText("Incidente resuelto correctamente.");
        alert.showAndWait();

        btnResolver.getScene().getWindow().hide();

    }

}