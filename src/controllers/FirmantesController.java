package controllers;

import java.util.Optional;

import api.FirmanteApiClient;
import api.FirmanteApiResponse;
import api.FirmanteGuardarApiRequest;
import api.IncidenteApiException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

public class FirmantesController {

    @FXML private TableView<FirmanteApiResponse> tablaFirmantes;
    @FXML private TableColumn<FirmanteApiResponse, String> colNombre;
    @FXML private TableColumn<FirmanteApiResponse, String> colArea;
    @FXML private TableColumn<FirmanteApiResponse, String> colUbicacion;
    @FXML private TableColumn<FirmanteApiResponse, String> colTipo;
    @FXML private TableColumn<FirmanteApiResponse, String> colEstado;
    @FXML private Label lblFormulario;
    @FXML private Label lblEstado;
    @FXML private TextField txtNombre;
    @FXML private TextField txtArea;
    @FXML private TextField txtUbicacion;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCambiarEstado;
    @FXML private Button btnCerrar;

    private final FirmanteApiClient apiClient = new FirmanteApiClient();
    private FirmanteApiResponse seleccionado;

    @FXML
    public void initialize() {
        configurarTabla();
        limitar(txtNombre, 150);
        limitar(txtArea, 150);
        limitar(txtUbicacion, 150);
        cmbTipo.setItems(FXCollections.observableArrayList("ALTERNATIVA", "OBLIGATORIO"));

        tablaFirmantes.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> seleccionar(actual)
        );
        btnNuevo.setOnAction(evento -> prepararNuevo());
        btnGuardar.setOnAction(evento -> guardar());
        btnCambiarEstado.setOnAction(evento -> cambiarEstado());
        btnCerrar.setOnAction(evento -> cerrar());

        prepararNuevo();
        cargarFirmantes();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(dato -> valor(dato.getValue().nombre()));
        colArea.setCellValueFactory(dato -> valor(dato.getValue().areaLinea1()));
        colUbicacion.setCellValueFactory(dato -> valor(dato.getValue().areaLinea2()));
        colTipo.setCellValueFactory(dato -> valor(
                dato.getValue().obligatorio() ? "OBLIGATORIO" : "ALTERNATIVA"
        ));
        colEstado.setCellValueFactory(dato -> valor(
                dato.getValue().activo() ? "ACTIVO" : "DESHABILITADO"
        ));
        tablaFirmantes.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private SimpleStringProperty valor(String texto) {
        return new SimpleStringProperty(texto == null ? "" : texto);
    }

    private void limitar(TextField campo, int maximo) {
        campo.setTextFormatter(new TextFormatter<>(cambio ->
                cambio.getControlNewText().length() <= maximo ? cambio : null
        ));
    }

    private void cargarFirmantes() {
        try {
            Integer idSeleccionado = seleccionado == null ? null : seleccionado.id();
            var firmantes = apiClient.listar(true);
            tablaFirmantes.setItems(FXCollections.observableArrayList(firmantes));
            if (idSeleccionado != null) {
                firmantes.stream()
                        .filter(firmante -> firmante.id() == idSeleccionado)
                        .findFirst()
                        .ifPresent(firmante -> tablaFirmantes.getSelectionModel().select(firmante));
            }
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
        }
    }

    private void seleccionar(FirmanteApiResponse firmante) {
        if (firmante == null) return;
        seleccionado = firmante;
        lblFormulario.setText("Editar firmante");
        txtNombre.setText(firmante.nombre());
        txtArea.setText(firmante.areaLinea1());
        txtUbicacion.setText(firmante.areaLinea2());
        cmbTipo.setValue(firmante.tipo());
        cmbTipo.setDisable(true);
        actualizarEstadoVisual();
    }

    private void prepararNuevo() {
        seleccionado = null;
        tablaFirmantes.getSelectionModel().clearSelection();
        lblFormulario.setText("Nuevo firmante");
        txtNombre.clear();
        txtArea.setText("Security and Facilities");
        txtUbicacion.setText("Palomar Plant");
        cmbTipo.setDisable(false);
        cmbTipo.setValue("ALTERNATIVA");
        lblEstado.setText("El firmante se creará activo para Palomar");
        lblEstado.getStyleClass().removeAll("status-active", "status-inactive");
        lblEstado.getStyleClass().add("status-active");
        btnCambiarEstado.setDisable(true);
        btnCambiarEstado.setText("Deshabilitar");
        txtNombre.requestFocus();
    }

    private void guardar() {
        if (!validarFormulario()) return;
        FirmanteGuardarApiRequest request = new FirmanteGuardarApiRequest(
                txtNombre.getText().trim(),
                txtArea.getText().trim(),
                txtUbicacion.getText().trim(),
                cmbTipo.getValue()
        );
        try {
            FirmanteApiResponse guardado = seleccionado == null
                    ? apiClient.crear(request)
                    : apiClient.actualizar(seleccionado.id(), request);
            seleccionado = guardado;
            cargarFirmantes();
            seleccionarEnTabla(guardado.id());
            mostrarInformacion("Firmante guardado correctamente.");
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
        }
    }

    private boolean validarFormulario() {
        if (vacio(txtNombre)) {
            mostrarError("Debe indicar el nombre y apellido.");
            txtNombre.requestFocus();
            return false;
        }
        if (vacio(txtArea)) {
            mostrarError("Debe indicar el área del firmante.");
            txtArea.requestFocus();
            return false;
        }
        if (vacio(txtUbicacion)) {
            mostrarError("Debe indicar la ubicación mostrada en la firma.");
            txtUbicacion.requestFocus();
            return false;
        }
        if (cmbTipo.getValue() == null) {
            mostrarError("Debe seleccionar el tipo de firmante.");
            return false;
        }
        return true;
    }

    private boolean vacio(TextField campo) {
        return campo.getText() == null || campo.getText().isBlank();
    }

    private void cambiarEstado() {
        if (seleccionado == null) return;
        boolean nuevoEstado = !seleccionado.activo();
        String accion = nuevoEstado ? "habilitar" : "deshabilitar";
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cambio de estado");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(
                "¿Desea " + accion + " a " + seleccionado.nombre() + "?"
        );
        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) return;

        try {
            FirmanteApiResponse actualizado = apiClient.cambiarEstado(
                    seleccionado.id(), nuevoEstado
            );
            seleccionado = actualizado;
            cargarFirmantes();
            seleccionarEnTabla(actualizado.id());
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
        }
    }

    private void seleccionarEnTabla(int id) {
        tablaFirmantes.getItems().stream()
                .filter(firmante -> firmante.id() == id)
                .findFirst()
                .ifPresent(firmante -> tablaFirmantes.getSelectionModel().select(firmante));
    }

    private void actualizarEstadoVisual() {
        boolean activo = seleccionado.activo();
        lblEstado.setText(activo ? "Firmante activo" : "Firmante deshabilitado");
        lblEstado.getStyleClass().removeAll("status-active", "status-inactive");
        lblEstado.getStyleClass().add(activo ? "status-active" : "status-inactive");
        btnCambiarEstado.setDisable(seleccionado.obligatorio() && activo);
        btnCambiarEstado.setText(
                seleccionado.obligatorio() && activo
                        ? "Firma obligatoria"
                        : activo ? "Deshabilitar" : "Habilitar"
        );
        btnCambiarEstado.getStyleClass().removeAll("button-danger", "button-success");
        btnCambiarEstado.getStyleClass().add(activo ? "button-danger" : "button-success");
    }

    private void cerrar() {
        ((Stage) btnCerrar.getScene().getWindow()).close();
    }

    private void mostrarInformacion(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Gestión de firmantes");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Gestión de firmantes");
        alerta.setHeaderText(null);
        alerta.setContentText(
                mensaje == null ? "No se pudo completar la operación." : mensaje
        );
        alerta.showAndWait();
    }
}
