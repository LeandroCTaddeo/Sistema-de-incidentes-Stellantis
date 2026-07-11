package controllers;

import java.io.File;

import dao.ImagenDAO;
import dao.IncidenteDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
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

    private Incidente incidenteSeleccionado;

    private IncidenteDAO dao = new IncidenteDAO();
    private ImagenDAO imagenDAO = new ImagenDAO();

    @FXML
    public void initialize() {
        cargarLista();
    }

    private void cargarLista() {

        listaIncidentes.getChildren().clear();

        for (Incidente incidente : dao.obtenerPendientes()) {

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
            Label fecha = new Label("🕒 " + incidente.getFecha().replace("T", " "));
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