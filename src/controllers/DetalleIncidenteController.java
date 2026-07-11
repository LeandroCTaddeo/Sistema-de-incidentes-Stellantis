package controllers;

import dao.IncidenteDAO;
import dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import models.Incidente;
import models.Usuario;

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
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

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

        Usuario usuario = usuarioDAO.obtenerUsuarioActual();
        if (usuario == null || !dao.resolver(incidente.getId(), usuario.getId())) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Incidente");
            error.setHeaderText(null);
            error.setContentText("No se pudo resolver el incidente.");
            error.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Incidente");
        alert.setHeaderText(null);
        alert.setContentText("Incidente resuelto correctamente.");
        alert.showAndWait();

        btnResolver.getScene().getWindow().hide();

    }

}