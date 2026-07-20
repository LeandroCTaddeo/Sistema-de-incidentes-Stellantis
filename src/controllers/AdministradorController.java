package controllers;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import dao.ImagenDAO;
import dao.IncidenteDAO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.DatePicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import models.Imagen;
import models.Incidente;
import services.VisorImagenService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AdministradorController {

    private static final DateTimeFormatter FORMATO_FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private VBox listaIncidentes;
    @FXML private Label lblTitulo;
    @FXML private Label lblEmpleado;
    @FXML private Label lblSector;
    @FXML private Label lblPrioridad;
    @FXML private Label lblEstado;
    @FXML private TextArea txtDescripcion;
    @FXML private FlowPane contenedorImagenes;
    @FXML private Button btnResolver;
    @FXML private Button btnExportar;
    @FXML private Button btnBandeja;
    @FXML private Button btnMisIncidentes;
    @FXML private Button btnReportes;
    @FXML private Label lblSeccion;
    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private HBox filtrosFechas;

    private Incidente incidenteSeleccionado;
    private boolean modoHistorico;
    private VBox tarjetaSeleccionada;

    private IncidenteDAO dao = new IncidenteDAO();
    private ImagenDAO imagenDAO = new ImagenDAO();

    @FXML
    public void initialize() {
        btnBandeja.setOnAction(e -> mostrarBandeja());
        btnMisIncidentes.setOnAction(e -> mostrarMisIncidentes());
        btnReportes.setOnAction(e -> abrirReportes());
        txtBuscar.textProperty().addListener((obs, anterior, actual) -> cargarLista());
        dpDesde.valueProperty().addListener((obs, anterior, actual) -> cargarLista());
        dpHasta.valueProperty().addListener((obs, anterior, actual) -> cargarLista());
        configurarDesenfoqueAlHacerClick();
        cargarLista();
    }

    private void configurarDesenfoqueAlHacerClick() {
        Platform.runLater(() -> {
            Parent raiz = txtBuscar.getScene().getRoot();
            raiz.setFocusTraversable(true);
            raiz.addEventFilter(MouseEvent.MOUSE_PRESSED, evento -> {
                if (!esCampoEditable(evento.getTarget())) {
                    raiz.requestFocus();
                }
            });
        });
    }

    private boolean esCampoEditable(Object objetivo) {
        if (!(objetivo instanceof Node nodo)) return false;

        while (nodo != null) {
            if (nodo instanceof TextInputControl campo && campo.isEditable()) return true;
            if (nodo instanceof DatePicker) return true;
            nodo = nodo.getParent();
        }
        return false;
    }

    private void cargarLista() {

        listaIncidentes.getChildren().clear();

        List<Incidente> incidentes;
        if (modoHistorico) {
            if (dpDesde.getValue() != null && dpHasta.getValue() != null
                    && dpDesde.getValue().isAfter(dpHasta.getValue())) {
                mostrarError("La fecha desde no puede ser posterior a la fecha hasta.");
                return;
            }
            incidentes = dao.buscarResueltos(txtBuscar.getText(), dpDesde.getValue(), dpHasta.getValue());
        } else {
            incidentes = dao.obtenerPendientes();
        }

        if (incidentes.isEmpty()) {
            Label vacio = new Label(modoHistorico
                    ? "No se encontraron incidentes resueltos."
                    : "No hay incidentes pendientes.");
            vacio.getStyleClass().add("empty-state");
            listaIncidentes.getChildren().add(vacio);
            return;
        }

        for (Incidente incidente : incidentes) {

            VBox tarjeta = new VBox(6);
            tarjeta.setPrefWidth(320);
            tarjeta.getStyleClass().add("incident-card");

            Label titulo = new Label("📄 " + incidente.getTitulo());
            titulo.getStyleClass().add("card-title");

            Label empleado = new Label("👤 " + incidente.getNombreEmpleado());
            empleado.getStyleClass().add("card-meta");
            String textoFecha = formatearFechaHora(incidente.getFecha());
            if (modoHistorico && incidente.getFechaResolucion() != null) {
                textoFecha = "Resuelto: " + incidente.getFechaResolucion()
                        .format(FORMATO_FECHA_HORA);
            }
            Label fecha = new Label("🕒 " + textoFecha);
            fecha.getStyleClass().add("card-meta");
            Label prioridad = new Label("Prioridad: " + incidente.getPrioridad());
            prioridad.getStyleClass().add("card-meta");

            Label estado = new Label();

            if (incidente.getEstado().equals("PENDIENTE")) {
                estado.setText("🔴 PENDIENTE");
                estado.getStyleClass().add("status-pending");
            } else {
                estado.setText("🟢 RESUELTO");
                estado.getStyleClass().add("status-resolved");
            }

            tarjeta.getChildren().addAll(titulo, empleado, fecha, prioridad, estado);

            tarjeta.setOnMouseClicked(event -> {
                seleccionarTarjeta(tarjeta);
                if (event.getClickCount() == 2) {
                    abrirDetalleCaso(incidente);
                } else {
                    cargarDetalle(incidente);
                }
            });

            listaIncidentes.getChildren().add(tarjeta);
        }
    }

    private String formatearFechaHora(String valor) {
        if (valor == null || valor.isBlank()) return "Sin fecha";
        try {
            return LocalDateTime.parse(valor).format(FORMATO_FECHA_HORA);
        } catch (DateTimeParseException e) {
            return valor.replace("T", " ");
        }
    }

    private void mostrarBandeja() {
        modoHistorico = false;
        activarNavegacion(btnBandeja);
        lblSeccion.setText("Bandeja de Incidentes");
        filtrosFechas.setVisible(false);
        filtrosFechas.setManaged(false);
        txtBuscar.setPromptText("Buscar...");
        txtBuscar.clear();
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        btnResolver.setVisible(true);
        btnResolver.setManaged(true);
        limpiarDetalle();
        cargarLista();
    }

    private void mostrarMisIncidentes() {
        modoHistorico = true;
        activarNavegacion(btnMisIncidentes);
        lblSeccion.setText("Mis incidentes resueltos");
        filtrosFechas.setVisible(true);
        filtrosFechas.setManaged(true);
        txtBuscar.setPromptText("N°, título, empleado, sector o área...");
        btnResolver.setVisible(false);
        btnResolver.setManaged(false);
        limpiarDetalle();
        cargarLista();
    }

    private void abrirReportes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Reportes.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Reportes de incidentes");
            stage.setScene(new Scene(root, 1200, 750));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la sección de reportes.");
        }
    }

    private void activarNavegacion(Button activo) {
        btnBandeja.getStyleClass().remove("nav-button-active");
        btnMisIncidentes.getStyleClass().remove("nav-button-active");
        activo.getStyleClass().add("nav-button-active");
    }

    private void seleccionarTarjeta(VBox tarjeta) {
        if (tarjetaSeleccionada != null) {
            tarjetaSeleccionada.getStyleClass().remove("incident-card-selected");
        }
        tarjetaSeleccionada = tarjeta;
        if (!tarjetaSeleccionada.getStyleClass().contains("incident-card-selected")) {
            tarjetaSeleccionada.getStyleClass().add("incident-card-selected");
        }
    }

    private void cargarDetalle(Incidente incidente) {

        incidenteSeleccionado = incidente;

        lblTitulo.setText(incidente.getTitulo());
        lblEmpleado.setText(incidente.getNombreEmpleado());
        lblSector.setText(incidente.getSector());
        lblPrioridad.setText(incidente.getPrioridad().toString());
        lblEstado.setText(incidente.getEstado());
        txtDescripcion.setText(incidente.getDescripcion());

        contenedorImagenes.getChildren().clear();

        var imagenes = imagenDAO.obtenerPorIncidente(incidente.getId());
        for (Imagen img : imagenes) {
            ImageView vista = VisorImagenService.crearMiniatura(img, imagenes, 120, 90);
            if (vista != null) contenedorImagenes.getChildren().add(vista);
        }
    }
    
    private void abrirDetalleCaso(Incidente incidente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DetalleCasoAdmin.fxml"));
            Parent root = loader.load();

            DetalleCasoAdminController controller = loader.getController();
            controller.cargarIncidente(incidente);
            controller.setOnCasoResuelto(() -> {
                incidenteSeleccionado = null;
                cargarLista();
                limpiarDetalle();
            });

            Stage stage = new Stage();
            stage.setTitle("Caso - " + incidente.getTitulo());
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir el detalle del caso.");
        }
    }
    private void limpiarDetalle() {
        tarjetaSeleccionada = null;
        lblTitulo.setText("Seleccione un incidente");
        lblEmpleado.setText("");
        lblSector.setText("");
        lblPrioridad.setText("");
        lblEstado.setText("");
        txtDescripcion.clear();
        contenedorImagenes.getChildren().clear();
    }
    private void mostrarError(String mensaje) {
        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
