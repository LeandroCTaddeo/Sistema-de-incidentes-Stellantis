package controllers;

import java.io.File;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import dao.ImagenDAO;
import dao.IncidenteDAO;
import dao.UsuarioDAO;
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
import models.Usuario;
import api.AsignacionIncidenteApiResponse;
import api.IncidenteApiException;
import services.PDFService;
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
    @FXML private Button btnMisCasos;
    @FXML private Button btnHistorial;
    @FXML private Button btnReportes;
    @FXML private Button btnUsuarios;
    @FXML private Button btnTomarCaso;
    @FXML private Button btnLiberarCaso;
    @FXML private Label lblResponsable;
    @FXML private Label lblUsuarioActual;
    @FXML private Label lblSeccion;
    @FXML private TextField txtBuscar;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private HBox filtrosFechas;

    private Incidente incidenteSeleccionado;
    private Vista vista = Vista.BANDEJA;
    private VBox tarjetaSeleccionada;
    private Usuario usuarioActual;

    private IncidenteDAO dao = new IncidenteDAO();
    private ImagenDAO imagenDAO = new ImagenDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final PDFService pdfService = new PDFService();

    private enum Vista { BANDEJA, MIS_CASOS, HISTORIAL }

    @FXML
    public void initialize() {
        usuarioActual = usuarioDAO.obtenerUsuarioActual();
        lblUsuarioActual.setText(usuarioActual.getNombre());
        btnBandeja.setOnAction(e -> mostrarBandeja());
        btnMisCasos.setOnAction(e -> mostrarMisCasos());
        btnHistorial.setOnAction(e -> mostrarHistorial());
        btnReportes.setOnAction(e -> abrirReportes());
        btnUsuarios.setOnAction(e -> abrirUsuarios());
        btnTomarCaso.setOnAction(e -> tomarCaso());
        btnLiberarCaso.setOnAction(e -> liberarCaso());
        btnResolver.setOnAction(e -> resolverSeleccionado());
        btnExportar.setOnAction(e -> exportarSeleccionado());
        txtBuscar.textProperty().addListener((obs, anterior, actual) -> cargarLista());
        dpDesde.valueProperty().addListener((obs, anterior, actual) -> cargarLista());
        dpHasta.valueProperty().addListener((obs, anterior, actual) -> cargarLista());
        configurarDesenfoqueAlHacerClick();
        limpiarDetalle();
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
        if (vista == Vista.HISTORIAL) {
            if (dpDesde.getValue() != null && dpHasta.getValue() != null
                    && dpDesde.getValue().isAfter(dpHasta.getValue())) {
                mostrarError("La fecha desde no puede ser posterior a la fecha hasta.");
                return;
            }
            incidentes = dao.buscarResueltos(txtBuscar.getText(), dpDesde.getValue(), dpHasta.getValue());
        } else if (vista == Vista.MIS_CASOS) {
            incidentes = dao.obtenerAsignados(usuarioActual.getId());
            incidentes = filtrarIncidentes(incidentes, txtBuscar.getText());
        } else {
            incidentes = dao.obtenerPendientes();
            incidentes = filtrarIncidentes(incidentes, txtBuscar.getText());
        }

        if (incidentes.isEmpty()) {
            Label vacio = new Label(vista == Vista.HISTORIAL
                    ? "No se encontraron incidentes resueltos."
                    : vista == Vista.MIS_CASOS
                            ? "No tenés casos asignados actualmente."
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
            if (vista == Vista.HISTORIAL && incidente.getFechaResolucion() != null) {
                textoFecha = "Resuelto: " + incidente.getFechaResolucion()
                        .format(FORMATO_FECHA_HORA);
            }
            Label fecha = new Label("🕒 " + textoFecha);
            fecha.getStyleClass().add("card-meta");
            Label prioridad = new Label("Prioridad: " + incidente.getPrioridad());
            prioridad.getStyleClass().add("card-meta");

            Label estado = new Label();

            if (incidente.getEstado().equals("PENDIENTE")) {
                if (incidente.getAsignadoA() == null) {
                    estado.setText("🔴 SIN RESPONSABLE");
                    estado.getStyleClass().add("status-pending");
                } else {
                    estado.setText("🔵 A cargo de " + incidente.getNombreResponsable());
                    estado.getStyleClass().add("status-assigned");
                }
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

    private List<Incidente> filtrarIncidentes(List<Incidente> incidentes, String texto) {
        String termino = normalizarBusqueda(texto);
        if (termino.isBlank()) return incidentes;

        return incidentes.stream()
                .filter(incidente ->
                        String.valueOf(incidente.getId()).contains(termino)
                        || contiene(incidente.getTitulo(), termino)
                        || contiene(incidente.getNombreEmpleado(), termino)
                        || contiene(incidente.getNombreApellido(), termino)
                        || contiene(incidente.getSector(), termino)
                        || contiene(incidente.getArea(), termino)
                        || contiene(incidente.getLugar(), termino)
                        || contiene(incidente.getDescripcion(), termino)
                )
                .toList();
    }

    private boolean contiene(String valor, String termino) {
        return normalizarBusqueda(valor).contains(termino);
    }

    private String normalizarBusqueda(String valor) {
        if (valor == null) return "";

        return Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .trim();
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
        vista = Vista.BANDEJA;
        activarNavegacion(btnBandeja);
        lblSeccion.setText("Bandeja de Incidentes");
        filtrosFechas.setVisible(false);
        filtrosFechas.setManaged(false);
        txtBuscar.setPromptText("Buscar...");
        txtBuscar.clear();
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        limpiarDetalle();
        cargarLista();
    }

    private void mostrarMisCasos() {
        vista = Vista.MIS_CASOS;
        activarNavegacion(btnMisCasos);
        lblSeccion.setText("Mis casos en investigación");
        filtrosFechas.setVisible(false);
        filtrosFechas.setManaged(false);
        txtBuscar.setPromptText("Buscar en mis casos...");
        txtBuscar.clear();
        limpiarDetalle();
        cargarLista();
    }

    private void mostrarHistorial() {
        vista = Vista.HISTORIAL;
        activarNavegacion(btnHistorial);
        lblSeccion.setText("Historial de incidentes resueltos");
        filtrosFechas.setVisible(true);
        filtrosFechas.setManaged(true);
        txtBuscar.setPromptText("N°, título, empleado, sector o área...");
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

    private void abrirUsuarios() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Usuarios.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Gestión de administradores");
            stage.setScene(new Scene(root, 1200, 750));
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir la gestión de administradores.");
        }
    }

    private void activarNavegacion(Button activo) {
        btnBandeja.getStyleClass().remove("nav-button-active");
        btnMisCasos.getStyleClass().remove("nav-button-active");
        btnHistorial.getStyleClass().remove("nav-button-active");
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
        lblResponsable.setText(incidente.getAsignadoA() == null
                ? "Sin responsable"
                : incidente.getNombreResponsable());
        txtDescripcion.setText(incidente.getDescripcion());

        contenedorImagenes.getChildren().clear();

        var imagenes = imagenDAO.obtenerPorIncidente(incidente.getId());
        for (Imagen img : imagenes) {
            ImageView vista = VisorImagenService.crearMiniatura(img, imagenes, 120, 90);
            if (vista != null) contenedorImagenes.getChildren().add(vista);
        }
        actualizarAcciones();
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
            controller.setOnAsignacionCambiada(() -> {
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
        lblResponsable.setText("");
        txtDescripcion.clear();
        contenedorImagenes.getChildren().clear();
        actualizarAcciones();
    }

    private void actualizarAcciones() {
        boolean seleccionado = incidenteSeleccionado != null;
        boolean pendiente = seleccionado
                && "PENDIENTE".equalsIgnoreCase(incidenteSeleccionado.getEstado());
        boolean sinResponsable = pendiente && incidenteSeleccionado.getAsignadoA() == null;
        boolean propio = pendiente && incidenteSeleccionado.estaAsignadoA(usuarioActual.getId());
        mostrarBoton(btnTomarCaso, sinResponsable);
        mostrarBoton(btnLiberarCaso, propio);
        mostrarBoton(btnResolver, propio);
        btnExportar.setDisable(!seleccionado);
    }

    private void mostrarBoton(Button boton, boolean mostrar) {
        boton.setVisible(mostrar);
        boton.setManaged(mostrar);
    }

    private void tomarCaso() {
        if (incidenteSeleccionado == null) return;
        try {
            AsignacionIncidenteApiResponse asignacion = dao.tomar(
                    incidenteSeleccionado.getId(), usuarioActual.getId()
            );
            incidenteSeleccionado.asignarA(
                    asignacion.administradorId(),
                    asignacion.nombreResponsable(),
                    asignacion.fechaAsignacion()
            );
            cargarLista();
            limpiarDetalle();
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
            cargarLista();
        }
    }

    private void liberarCaso() {
        if (incidenteSeleccionado == null) return;
        try {
            dao.liberar(incidenteSeleccionado.getId(), usuarioActual.getId());
            incidenteSeleccionado.liberarAsignacion();
            cargarLista();
            limpiarDetalle();
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
            cargarLista();
        }
    }

    private void resolverSeleccionado() {
        if (incidenteSeleccionado == null || !incidenteSeleccionado.estaAsignadoA(usuarioActual.getId())) return;
        javafx.scene.control.Alert confirmacion = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION,
                "¿Desea marcar el expediente N° " + incidenteSeleccionado.getId() + " como resuelto?",
                javafx.scene.control.ButtonType.OK,
                javafx.scene.control.ButtonType.CANCEL
        );
        if (confirmacion.showAndWait().orElse(javafx.scene.control.ButtonType.CANCEL)
                != javafx.scene.control.ButtonType.OK) return;
        try {
            dao.resolver(incidenteSeleccionado.getId(), usuarioActual.getId());
            cargarLista();
            limpiarDetalle();
        } catch (IncidenteApiException e) {
            mostrarError(e.getMessage());
        }
    }

    private void exportarSeleccionado() {
        if (incidenteSeleccionado == null) return;
        try {
            pdfService.exportarExpediente(incidenteSeleccionado, btnExportar.getScene().getWindow());
        } catch (Exception e) {
            mostrarError("No se pudo exportar el expediente. "
                    + (e.getMessage() == null ? "" : e.getMessage()));
        }
    }
    private void mostrarError(String mensaje) {
        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
