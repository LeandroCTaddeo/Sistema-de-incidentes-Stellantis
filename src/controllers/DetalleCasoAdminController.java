package controllers;

import java.util.List;
import java.util.Optional;

import dao.BoletinAdminDAO;
import dao.UsuarioDAO;
import dao.IncidenteDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.BoletinAdmin;
import models.Incidente;
import models.Usuario;
import services.PDFService;

public class DetalleCasoAdminController {

    @FXML private Button btnVolver;
    @FXML private Button btnNuevoBoletin;
    @FXML private Button btnBoletinOriginal;
    @FXML private Button btnExportar;
    @FXML private Button btnResolver;

    @FXML private Label lblTituloCaso;
    @FXML private VBox contenedorBoletines;
    @FXML private VBox panelVistaPrevia;

    private Incidente incidenteActual;
    private Usuario usuarioActual;
    private Runnable onCasoResuelto;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final BoletinAdminDAO boletinAdminDAO = new BoletinAdminDAO();
    private final PDFService pdfService = new PDFService();
    private final IncidenteDAO incidenteDAO = new IncidenteDAO();

    @FXML
    public void initialize() {
        usuarioActual = usuarioDAO.obtenerUsuarioActual();

        btnVolver.setOnAction(e -> volver());
        btnBoletinOriginal.setOnAction(e -> mostrarVistaPreviaOriginal());
        btnBoletinOriginal.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) abrirBoletinEmpleado();
        });

        btnNuevoBoletin.setOnAction(e -> crearNuevoBoletin());
        btnExportar.setOnAction(e -> exportarPDF());
        btnResolver.setOnAction(e -> resolverCaso());
    }

    public void cargarIncidente(Incidente incidente) {
        this.incidenteActual = incidente;

        lblTituloCaso.setText("Expediente N° " + incidente.getId() + " - " + incidente.getTitulo());

        cargarBoletines();
        mostrarVistaPreviaOriginal();
        actualizarEstadoControles();
    }

    public void setOnCasoResuelto(Runnable onCasoResuelto) {
        this.onCasoResuelto = onCasoResuelto;
    }

    private void mostrarVistaPreviaOriginal() {
        if (incidenteActual == null) return;

        panelVistaPrevia.getChildren().clear();

        Label titulo = new Label("📄 Boletín original del empleado");
        titulo.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");

        Label caso = new Label("Caso: " + valor(incidenteActual.getTitulo()));
        Label empleado = new Label("Empleado: " + valor(incidenteActual.getNombreEmpleado()));
        Label sector = new Label("Sector: " + valor(incidenteActual.getSector()));
        Label prioridad = new Label("Prioridad: " + valor(String.valueOf(incidenteActual.getPrioridad())));
        Label estado = new Label("Estado: " + valor(incidenteActual.getEstado()));
        Label fecha = new Label("Fecha: " + valor(incidenteActual.getFecha()));

        Label descripcionTitulo = new Label("Descripción:");
        descripcionTitulo.setStyle("-fx-font-weight:bold;");

        Label descripcion = new Label(valor(incidenteActual.getDescripcion()));
        descripcion.setWrapText(true);

        Label ayuda = new Label("Doble clic en el documento de la izquierda para abrir el formulario completo.");
        ayuda.setStyle("-fx-text-fill:#777;");

        panelVistaPrevia.getChildren().addAll(
                titulo, caso, empleado, sector, prioridad, estado, fecha,
                descripcionTitulo, descripcion, ayuda
        );
    }

    private void mostrarVistaPreviaBoletin(BoletinAdmin boletin) {
        panelVistaPrevia.getChildren().clear();

        Label titulo = new Label("📄 " + valor(boletin.getTitulo()));
        titulo.setStyle("-fx-font-size:22px; -fx-font-weight:bold;");

        Label fecha = new Label("Fecha creación: " + valor(
                boletin.getFechaCreacion() != null ? boletin.getFechaCreacion().toString() : ""
        ));

        Label prioridad = new Label("Prioridad: " + valor(boletin.getPrioridad()));
        Label lugar = new Label("Lugar: " + valor(boletin.getLugar()));
        Label area = new Label("Área: " + valor(boletin.getArea()));
        Label involucrado = new Label("Involucrado: " + valor(boletin.getNombreApellido()));

        Label descTitulo = new Label("Descripción:");
        descTitulo.setStyle("-fx-font-weight:bold;");

        Label descripcion = new Label(valor(boletin.getDescripcion()));
        descripcion.setWrapText(true);

        Label historialTitulo = new Label("Historial:");
        historialTitulo.setStyle("-fx-font-weight:bold;");

        Label historial = new Label(valor(boletin.getHistorial()));
        historial.setWrapText(true);

        Label ayuda = new Label("Doble clic sobre el boletín para editarlo.");
        ayuda.setStyle("-fx-text-fill:#777;");

        panelVistaPrevia.getChildren().addAll(
                titulo, fecha, prioridad, lugar, area, involucrado,
                descTitulo, descripcion,
                historialTitulo, historial,
                ayuda
        );
    }

    private void cargarBoletines() {
        contenedorBoletines.getChildren().clear();

        if (incidenteActual == null) return;

        List<BoletinAdmin> boletines =
                boletinAdminDAO.obtenerPorIncidente(incidenteActual.getId());

        if (boletines.isEmpty()) {
            Label vacio = new Label("No hay boletines internos.");
            vacio.setStyle("-fx-text-fill:#777;");
            contenedorBoletines.getChildren().add(vacio);
            return;
        }

        int numero = 1;

        for (BoletinAdmin boletin : boletines) {
            Button boton = new Button("📄 Boletín interno N° " + numero + " - " + valor(boletin.getTitulo()));
            boton.setMaxWidth(Double.MAX_VALUE);

            boton.setOnAction(e -> mostrarVistaPreviaBoletin(boletin));
            boton.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !estaResuelto()) {
                    abrirFormularioBoletin(boletin);
                }
            });

            contenedorBoletines.getChildren().add(boton);
            numero++;
        }
    }

    private void abrirBoletinEmpleado() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Boletin.fxml"));
            Parent root = loader.load();

            BoletinController controller = loader.getController();
            controller.cargarIncidente(incidenteActual);
            controller.modoLectura();

            Stage stage = new Stage();
            stage.setTitle("Boletín Original");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir el boletín original.");
        }
    }

    private void crearNuevoBoletin() {
        if (incidenteActual == null || estaResuelto()) return;

        BoletinAdmin boletin = new BoletinAdmin();
        boletin.setIncidenteId(incidenteActual.getId());
        boletin.setAdministradorId(usuarioActual.getId());
        boletin.setTitulo("Nuevo boletín interno");
        boletin.setDescripcion("");
        boletin.setPrioridad("MEDIA");

        abrirFormularioBoletin(boletin);
    }

    private void abrirFormularioBoletin(BoletinAdmin boletin) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/BoletinAdminFormulario.fxml"));
            Parent root = loader.load();

            BoletinAdminFormularioController controller = loader.getController();
            controller.cargarBoletin(boletin);

            Stage stage = new Stage();
            stage.setTitle("Boletín interno");
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.showAndWait();

            if (controller.fueGuardado()) {
                cargarBoletines();
                mostrarVistaPreviaBoletin(boletin);
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo abrir el formulario del boletín.");
        }
    }

    private void exportarPDF() {
        try {
            boolean exportado = pdfService.exportarExpediente(
                    incidenteActual,
                    btnExportar.getScene().getWindow()
            );

            if (exportado) {
                mostrarInfo("Expediente exportado correctamente.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("No se pudo exportar el expediente.");
        }
    }

    private void volver() {
        btnVolver.getScene().getWindow().hide();
    }

    private void resolverCaso() {
        if (incidenteActual == null || usuarioActual == null || estaResuelto()) return;

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Resolver expediente");
        confirmacion.setHeaderText("Resolver expediente N° " + incidenteActual.getId());
        confirmacion.setContentText(
                "El caso saldrá de la Bandeja y quedará disponible en Mis incidentes. ¿Desea continuar?"
        );

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) return;

        if (!incidenteDAO.resolver(incidenteActual.getId(), usuarioActual.getId())) {
            mostrarError("No se pudo resolver el expediente o ya estaba resuelto.");
            return;
        }

        incidenteActual.marcarResuelto(usuarioActual.getId(), java.time.LocalDateTime.now());
        actualizarEstadoControles();
        mostrarInfo("El expediente fue marcado como resuelto.");

        if (onCasoResuelto != null) onCasoResuelto.run();
        btnResolver.getScene().getWindow().hide();
    }

    private boolean estaResuelto() {
        return incidenteActual != null && "RESUELTO".equalsIgnoreCase(incidenteActual.getEstado());
    }

    private void actualizarEstadoControles() {
        boolean resuelto = estaResuelto();
        btnNuevoBoletin.setDisable(resuelto);
        btnResolver.setDisable(resuelto);
        btnResolver.setText(resuelto ? "Caso resuelto" : "Resolver caso");
    }

    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    private void mostrarInfo(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Información");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Error");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}