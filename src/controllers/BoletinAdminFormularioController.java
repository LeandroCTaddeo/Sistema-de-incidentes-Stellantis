package controllers;

import dao.BoletinAdminDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.BoletinAdmin;

public class BoletinAdminFormularioController {

    @FXML private DatePicker dpFechaRegistro;
    @FXML private DatePicker dpFechaEmision;

    @FXML private TextField txtLugar;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescripcion;

    @FXML private TextField txtNombreApellido;
    @FXML private TextField txtCargo;
    @FXML private TextField txtMatricula;
    @FXML private TextField txtDni;

    @FXML private TextField txtArea;
    @FXML private TextField txtSuperiorInmediato;

    @FXML private TextArea txtHistorial;

    @FXML private ComboBox<String> cmbPrioridad;

    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private BoletinAdmin boletin;
    private boolean guardado = false;

    private BoletinAdminDAO boletinDAO = new BoletinAdminDAO();

    @FXML
    public void initialize() {
        cmbPrioridad.getItems().addAll("Baja", "Media", "Alta");
        cmbPrioridad.getSelectionModel().select("Media");

        btnGuardar.setOnAction(e -> guardar());
        btnCancelar.setOnAction(e -> cerrar());
    }

    public void cargarBoletin(BoletinAdmin boletin) {
        this.boletin = boletin;

        dpFechaRegistro.setValue(boletin.getFechaRegistro());
        dpFechaEmision.setValue(boletin.getFechaEmision());

        txtLugar.setText(valor(boletin.getLugar()));
        txtTitulo.setText(valor(boletin.getTitulo()));
        txtDescripcion.setText(valor(boletin.getDescripcion()));

        txtNombreApellido.setText(valor(boletin.getNombreApellido()));
        txtCargo.setText(valor(boletin.getCargo()));
        txtMatricula.setText(valor(boletin.getMatricula()));
        txtDni.setText(valor(boletin.getDni()));

        txtArea.setText(valor(boletin.getArea()));
        txtSuperiorInmediato.setText(valor(boletin.getSuperiorInmediato()));

        txtHistorial.setText(valor(boletin.getHistorial()));

        if (boletin.getPrioridad() != null && !boletin.getPrioridad().isBlank()) {
            cmbPrioridad.getSelectionModel().select(formatearPrioridad(boletin.getPrioridad()));
        }
    }

    private void guardar() {
        if (boletin == null) return;

        String titulo = txtTitulo.getText().trim();

        if (titulo.isEmpty()) {
            mostrarError("Debe ingresar un título.");
            return;
        }

        boletin.setFechaRegistro(dpFechaRegistro.getValue());
        boletin.setFechaEmision(dpFechaEmision.getValue());

        boletin.setLugar(txtLugar.getText().trim());
        boletin.setTitulo(titulo);
        boletin.setDescripcion(txtDescripcion.getText().trim());

        boletin.setNombreApellido(txtNombreApellido.getText().trim());
        boletin.setCargo(txtCargo.getText().trim());
        boletin.setMatricula(txtMatricula.getText().trim());
        boletin.setDni(txtDni.getText().trim());

        boletin.setArea(txtArea.getText().trim());
        boletin.setSuperiorInmediato(txtSuperiorInmediato.getText().trim());

        boletin.setHistorial(txtHistorial.getText().trim());
        boletin.setPrioridad(cmbPrioridad.getValue().toUpperCase());

        if (boletin.getId() == 0) {
            int id = boletinDAO.guardar(boletin);

            if (id == -1) {
                mostrarError("No se pudo guardar el boletín.");
                return;
            }

            boletin.setId(id);
        } else {
            boletinDAO.actualizar(boletin);
        }

        guardado = true;
        cerrar();
    }

    public boolean fueGuardado() {
        return guardado;
    }

    private void cerrar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    private String formatearPrioridad(String prioridad) {
        String p = prioridad.toLowerCase();
        return p.substring(0, 1).toUpperCase() + p.substring(1);
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
