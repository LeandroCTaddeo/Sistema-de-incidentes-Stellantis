package controllers;

import java.util.Optional;

import api.GestionUsuariosApiClient;
import api.IncidenteApiException;
import api.UsuarioGestionApiResponse;
import api.UsuarioGuardarApiRequest;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;
import javafx.util.Duration;
import dao.IncidenteDAO;

public class UsuariosController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<UsuarioGestionApiResponse> tablaUsuarios;
    @FXML private TableColumn<UsuarioGestionApiResponse, String> colNombre;
    @FXML private TableColumn<UsuarioGestionApiResponse, String> colUsuario;
    @FXML private TableColumn<UsuarioGestionApiResponse, String> colSector;
    @FXML private TableColumn<UsuarioGestionApiResponse, String> colRol;
    @FXML private TableColumn<UsuarioGestionApiResponse, String> colEstado;
    @FXML private TableColumn<UsuarioGestionApiResponse, String> colAbiertos;
    @FXML private TableColumn<UsuarioGestionApiResponse, String> colResueltos;
    @FXML private ListView<String> listaCasos;
    @FXML private Label lblFormulario;
    @FXML private Label lblEstadoSeleccionado;
    @FXML private TextField txtNombre;
    @FXML private TextField txtUsuarioWindows;
    @FXML private TextField txtSector;
    @FXML private ComboBox<String> cmbRol;
    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCambiarEstado;
    @FXML private Button btnCerrar;

    private final GestionUsuariosApiClient apiClient = new GestionUsuariosApiClient();
    private final IncidenteDAO incidenteDAO = new IncidenteDAO();
    private final PauseTransition pausaBusqueda = new PauseTransition(Duration.millis(300));
    private UsuarioGestionApiResponse seleccionado;

    @FXML
    public void initialize() {
        configurarTabla();
        configurarCampos();
        cmbRol.setItems(FXCollections.observableArrayList("ADMIN"));
        cmbRol.getSelectionModel().select("ADMIN");

        tablaUsuarios.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, actual) -> seleccionar(actual)
        );
        txtBuscar.textProperty().addListener((obs, anterior, actual) -> {
            pausaBusqueda.setOnFinished(evento -> cargarUsuarios());
            pausaBusqueda.playFromStart();
        });
        btnNuevo.setOnAction(evento -> prepararNuevo());
        btnGuardar.setOnAction(evento -> guardar());
        btnCambiarEstado.setOnAction(evento -> cambiarEstado());
        btnCerrar.setOnAction(evento -> cerrar());

        prepararNuevo();
        cargarUsuarios();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(dato -> valor(dato.getValue().nombre()));
        colUsuario.setCellValueFactory(dato -> valor(dato.getValue().usuarioWindows()));
        colSector.setCellValueFactory(dato -> valor(dato.getValue().sector()));
        colRol.setCellValueFactory(dato -> valor(dato.getValue().rol()));
        colEstado.setCellValueFactory(dato ->
                valor(dato.getValue().activo() ? "ACTIVO" : "DESHABILITADO")
        );
        colAbiertos.setCellValueFactory(dato ->
                valor(String.valueOf(dato.getValue().casosAbiertos()))
        );
        colResueltos.setCellValueFactory(dato ->
                valor(String.valueOf(dato.getValue().casosResueltos()))
        );
        tablaUsuarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    }

    private SimpleStringProperty valor(String texto) {
        return new SimpleStringProperty(texto == null ? "" : texto);
    }

    private void configurarCampos() {
        limitar(txtNombre, 150);
        limitar(txtUsuarioWindows, 150);
        limitar(txtSector, 100);
    }

    private void limitar(TextField campo, int maximo) {
        campo.setTextFormatter(new TextFormatter<>(cambio ->
                cambio.getControlNewText().length() <= maximo ? cambio : null
        ));
    }

    private void cargarUsuarios() {
        try {
            Integer idSeleccionado = seleccionado == null ? null : seleccionado.id();
            var usuarios = apiClient.listar(txtBuscar.getText());
            tablaUsuarios.setItems(FXCollections.observableArrayList(usuarios));

            if (idSeleccionado != null) {
                usuarios.stream()
                        .filter(usuario -> usuario.id() == idSeleccionado)
                        .findFirst()
                        .ifPresent(usuario -> tablaUsuarios.getSelectionModel().select(usuario));
            }
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
        }
    }

    private void seleccionar(UsuarioGestionApiResponse usuario) {
        if (usuario == null) return;
        seleccionado = usuario;
        lblFormulario.setText("Editar administrador");
        txtNombre.setText(usuario.nombre());
        txtUsuarioWindows.setText(usuario.usuarioWindows());
        txtSector.setText(usuario.sector());
        cmbRol.setValue(usuario.rol());
        cargarCasos(usuario.id());
        actualizarEstadoVisual();
    }

    private void prepararNuevo() {
        seleccionado = null;
        tablaUsuarios.getSelectionModel().clearSelection();
        lblFormulario.setText("Nuevo administrador");
        txtNombre.clear();
        txtUsuarioWindows.clear();
        txtSector.clear();
        cmbRol.setValue("ADMIN");
        lblEstadoSeleccionado.setText("El administrador se creará activo");
        listaCasos.getItems().clear();
        lblEstadoSeleccionado.getStyleClass().removeAll("status-active", "status-inactive");
        lblEstadoSeleccionado.getStyleClass().add("status-active");
        btnCambiarEstado.setDisable(true);
        btnCambiarEstado.setText("Deshabilitar");
        txtNombre.requestFocus();
    }

    private void guardar() {
        if (!validarFormulario()) return;
        UsuarioGuardarApiRequest request = new UsuarioGuardarApiRequest(
                txtNombre.getText().trim(),
                txtUsuarioWindows.getText().trim(),
                txtSector.getText().trim(),
                "ADMIN"
        );

        try {
            UsuarioGestionApiResponse guardado = seleccionado == null
                    ? apiClient.crear(request)
                    : apiClient.actualizar(seleccionado.id(), request);
            seleccionado = guardado;
            cargarUsuarios();
            seleccionarEnTabla(guardado.id());
            mostrarInformacion("Administrador guardado correctamente.");
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
        }
    }

    private boolean validarFormulario() {
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mostrarError("Debe indicar el nombre y apellido.");
            txtNombre.requestFocus();
            return false;
        }
        if (txtUsuarioWindows.getText() == null || txtUsuarioWindows.getText().isBlank()) {
            mostrarError("Debe indicar el usuario de Windows.");
            txtUsuarioWindows.requestFocus();
            return false;
        }
        if (cmbRol.getValue() == null) {
            mostrarError("Debe seleccionar un rol.");
            cmbRol.requestFocus();
            return false;
        }
        return true;
    }

    private void cargarCasos(int administradorId) {
        try {
            var casos = incidenteDAO.obtenerAsignados(administradorId).stream()
                    .map(caso -> "Expediente N° " + caso.getId() + " · " + caso.getTitulo())
                    .toList();
            listaCasos.setItems(FXCollections.observableArrayList(casos));
        } catch (IncidenteApiException e) {
            listaCasos.setItems(FXCollections.observableArrayList(
                    "No se pudieron cargar los casos actuales."
            ));
        }
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
            UsuarioGestionApiResponse actualizado = apiClient.cambiarEstado(
                    seleccionado.id(), nuevoEstado
            );
            seleccionado = actualizado;
            cargarUsuarios();
            seleccionarEnTabla(actualizado.id());
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
        }
    }

    private void seleccionarEnTabla(int id) {
        tablaUsuarios.getItems().stream()
                .filter(usuario -> usuario.id() == id)
                .findFirst()
                .ifPresent(usuario -> tablaUsuarios.getSelectionModel().select(usuario));
    }

    private void actualizarEstadoVisual() {
        boolean activo = seleccionado.activo();
        lblEstadoSeleccionado.setText(activo ? "Usuario activo" : "Usuario deshabilitado");
        lblEstadoSeleccionado.getStyleClass().removeAll("status-active", "status-inactive");
        lblEstadoSeleccionado.getStyleClass().add(activo ? "status-active" : "status-inactive");
        btnCambiarEstado.setDisable(false);
        btnCambiarEstado.setText(activo ? "Deshabilitar" : "Habilitar");
        btnCambiarEstado.getStyleClass().removeAll("button-danger", "button-success");
        btnCambiarEstado.getStyleClass().add(activo ? "button-danger" : "button-success");
    }

    private void cerrar() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    private void mostrarInformacion(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Gestión de administradores");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Gestión de administradores");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje == null ? "No se pudo completar la operación." : mensaje);
        alerta.showAndWait();
    }
}
