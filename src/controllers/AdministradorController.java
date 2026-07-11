package controllers;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.ImagenDAO;
import dao.IncidenteDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import models.Imagen;
import models.Incidente;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AdministradorController {

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
        cargarLista();
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
            vacio.setStyle("-fx-text-fill:#777; -fx-padding:15;");
            listaIncidentes.getChildren().add(vacio);
            return;
        }

        for (Incidente incidente : incidentes) {

            VBox tarjeta = new VBox(6);
            tarjeta.setPrefWidth(320);

            tarjeta.setStyle("""
                    -fx-background-color:white;
                    -fx-background-radius:10;
                    -fx-border-radius:10;
                    -fx-border-color:#D6D6D6;
                    -fx-padding:12;
                    """);

            Label titulo = new Label("📄 " + incidente.getTitulo());
            titulo.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");

            Label empleado = new Label("👤 " + incidente.getNombreEmpleado());
            String textoFecha = incidente.getFecha().replace("T", " ");
            if (modoHistorico && incidente.getFechaResolucion() != null) {
                textoFecha = "Resuelto: " + incidente.getFechaResolucion()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
            Label fecha = new Label("🕒 " + textoFecha);
            Label prioridad = new Label("Prioridad: " + incidente.getPrioridad());

            Label estado = new Label();

            if (incidente.getEstado().equals("PENDIENTE")) {
                estado.setText("🔴 PENDIENTE");
                estado.setStyle("-fx-text-fill:#D32F2F; -fx-font-weight:bold;");
            } else {
                estado.setText("🟢 RESUELTO");
                estado.setStyle("-fx-text-fill:#2E7D32; -fx-font-weight:bold;");
            }

            tarjeta.getChildren().addAll(titulo, empleado, fecha, prioridad, estado);

            tarjeta.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    abrirDetalleCaso(incidente);
                } else {
                    cargarDetalle(incidente);
                }
            });

            listaIncidentes.getChildren().add(tarjeta);
        }
    }

    private void mostrarBandeja() {
        modoHistorico = false;
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

    private void cargarDetalle(Incidente incidente) {

        incidenteSeleccionado = incidente;

        lblTitulo.setText(incidente.getTitulo());
        lblEmpleado.setText(incidente.getNombreEmpleado());
        lblSector.setText(incidente.getSector());
        lblPrioridad.setText(incidente.getPrioridad().toString());
        lblEstado.setText(incidente.getEstado());
        txtDescripcion.setText(incidente.getDescripcion());

        contenedorImagenes.getChildren().clear();

        for (Imagen img : imagenDAO.obtenerPorIncidente(incidente.getId())) {

            File archivo = new File(img.getRuta());

            if (archivo.exists()) {
                Image imagen = new Image(archivo.toURI().toString(), 120, 120, true, true);
                ImageView vista = new ImageView(imagen);
                contenedorImagenes.getChildren().add(vista);
            }
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
