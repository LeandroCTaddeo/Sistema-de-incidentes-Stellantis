package controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import models.Incidente;
import services.IncidenteService;
import services.VisorImagenService;
import models.Prioridad;
import dao.UsuarioDAO;
import models.Usuario;
import models.Imagen;
import dao.ImagenDAO;
import javafx.scene.control.DatePicker;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.application.Platform;

public class BoletinController {

	private IncidenteService incidenteService = new IncidenteService();
	private List<File> imagenesSeleccionadas = new ArrayList<>();

	@FXML private TextField txtTitulo;
	@FXML private TextArea txtDescripcion;
	@FXML private ComboBox<String> cmbPrioridad;
	@FXML private Button btnAdjuntar;
	@FXML private Button btnEnviar;
	@FXML private FlowPane contenedorImagenes;

	@FXML private DatePicker dpFechaRegistro;
	@FXML private DatePicker dpFechaEmision;

	@FXML private TextField txtLugar;
	@FXML private TextField txtNombreApellido;
	@FXML private TextField txtCargo;
	@FXML private TextField txtMatricula;
	@FXML private TextField txtDni;
	@FXML private TextField txtArea;
	@FXML private TextField txtSuperiorInmediato;

	@FXML private TextArea txtHistorial;

	private Usuario usuarioActual;
	private UsuarioDAO usuarioDAO = new UsuarioDAO();

	private ImagenDAO imagenDAO = new ImagenDAO();

	@FXML
	public void initialize() {

		usuarioActual = usuarioDAO.obtenerUsuarioActual();

		System.out.println("Usuario: " + usuarioActual.getNombre());
		System.out.println("Rol: " + usuarioActual.getRol());

		for (Prioridad p : Prioridad.values()) {
			cmbPrioridad.getItems().add(
					p.name().substring(0,1) +
					p.name().substring(1).toLowerCase()
					);
		}
		cmbPrioridad.getSelectionModel().select("Media");

		btnEnviar.setOnAction(event -> enviarIncidente());
		btnAdjuntar.setOnAction(event -> seleccionarImagenes());
		configurarNavegacionTeclado();
		Platform.runLater(() -> dpFechaRegistro.requestFocus());
	}

	private void configurarNavegacionTeclado() {
		List<Node> orden = List.of(
				dpFechaRegistro,
				dpFechaEmision,
				txtLugar,
				txtTitulo,
				txtDescripcion,
				txtNombreApellido,
				txtCargo,
				txtMatricula,
				txtDni,
				txtArea,
				txtSuperiorInmediato,
				txtHistorial,
				cmbPrioridad,
				btnEnviar
		);

		for (int i = 0; i < orden.size() - 1; i++) {
			Node actual = orden.get(i);
			Node siguiente = orden.get(i + 1);

			if (actual instanceof TextArea) {
				actual.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
					if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
						event.consume();
						siguiente.requestFocus();
					}
				});
			} else {
				actual.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
					if (event.getCode() == KeyCode.ENTER) {
						event.consume();
						siguiente.requestFocus();
					}
				});
			}
		}
	}

	private void seleccionarImagenes() {
		FileChooser chooser = new FileChooser();
		chooser.setTitle("Seleccionar imágenes");
		chooser.getExtensionFilters().add(
				new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
				);

		List<File> nuevasImagenes =
				chooser.showOpenMultipleDialog(btnAdjuntar.getScene().getWindow());

		if (nuevasImagenes == null) return;

		for (File archivo : nuevasImagenes) {
			if (!imagenesSeleccionadas.contains(archivo)) {
				imagenesSeleccionadas.add(archivo);
				agregarMiniatura(archivo);
			}
		}
	}

	private void agregarMiniatura(File archivo) {
		Image imagen = new Image(archivo.toURI().toString(), 80, 80, true, true);
		ImageView vista = new ImageView(imagen);

		Button eliminar = new Button("X");
		eliminar.setOnAction(event -> {
			imagenesSeleccionadas.remove(archivo);
			contenedorImagenes.getChildren().remove(eliminar.getParent());
		});

		VBox caja = new VBox(5);
		caja.getChildren().addAll(vista, eliminar);

		contenedorImagenes.getChildren().add(caja);
	}

	private void enviarIncidente() {
		String titulo = txtTitulo.getText().trim();
		String descripcion = txtDescripcion.getText().trim();

		if (titulo.isEmpty()) {
			mostrarError("Debe ingresar un título.");
			return;
		}

		if (descripcion.isEmpty()) {
			mostrarError("Debe ingresar una descripción.");
			return;
		}

		Incidente incidente = new Incidente(
				titulo,
				descripcion,
				Prioridad.valueOf(cmbPrioridad.getValue().toUpperCase()),
				usuarioActual.getId(),
				dpFechaRegistro.getValue(),
				dpFechaEmision.getValue(),
				txtLugar.getText().trim(),
				txtNombreApellido.getText().trim(),
				txtCargo.getText().trim(),
				txtMatricula.getText().trim(),
				txtDni.getText().trim(),
				txtArea.getText().trim(),
				txtSuperiorInmediato.getText().trim(),
				txtHistorial.getText().trim()
				);

		int idIncidente = incidenteService.guardar(incidente);

		for (File archivo : imagenesSeleccionadas) {

			imagenDAO.guardar(
					idIncidente,
					archivo.getAbsolutePath()
					);

		}

		Alert alerta = new Alert(Alert.AlertType.INFORMATION);
		alerta.setTitle("Correcto");
		alerta.setHeaderText(null);
		alerta.setContentText("Incidente enviado correctamente.");
		alerta.showAndWait();

		limpiarFormulario();
	}

	private void limpiarFormulario() {
		dpFechaRegistro.setValue(null);
		dpFechaEmision.setValue(null);

		txtLugar.clear();
		txtTitulo.clear();
		txtDescripcion.clear();

		txtNombreApellido.clear();
		txtCargo.clear();
		txtMatricula.clear();
		txtDni.clear();

		txtArea.clear();
		txtSuperiorInmediato.clear();

		txtHistorial.clear();

		cmbPrioridad.getSelectionModel().select("Media");

		imagenesSeleccionadas.clear();
		contenedorImagenes.getChildren().clear();
	}
	
	public void modoLectura() {
		dpFechaRegistro.setDisable(true);
		dpFechaEmision.setDisable(true);
		txtLugar.setEditable(false);

	    txtTitulo.setEditable(false);
	    txtDescripcion.setEditable(false);
	    txtNombreApellido.setEditable(false);
	    txtCargo.setEditable(false);
	    txtMatricula.setEditable(false);
	    txtDni.setEditable(false);
	    txtArea.setEditable(false);
	    txtSuperiorInmediato.setEditable(false);
	    txtHistorial.setEditable(false);

	    cmbPrioridad.setDisable(true);

	    btnEnviar.setVisible(false);
	    btnAdjuntar.setVisible(false);

	}

	private void mostrarError(String mensaje) {
		Alert alerta = new Alert(Alert.AlertType.ERROR);
		alerta.setTitle("Error");
		alerta.setHeaderText(null);
		alerta.setContentText(mensaje);
		alerta.showAndWait();
	}
	
	public void cargarIncidente(Incidente incidente) {

	    txtTitulo.setText(incidente.getTitulo());
	    txtDescripcion.setText(incidente.getDescripcion());
	    dpFechaRegistro.setValue(incidente.getFechaRegistro());
	    dpFechaEmision.setValue(incidente.getFechaEmision());
	    txtLugar.setText(valor(incidente.getLugar()));
	    txtNombreApellido.setText(valor(incidente.getNombreApellido()));
	    txtCargo.setText(valor(incidente.getCargo()));
	    txtMatricula.setText(valor(incidente.getMatricula()));
	    txtDni.setText(valor(incidente.getDni()));
	    txtArea.setText(valor(incidente.getArea()));
	    txtSuperiorInmediato.setText(valor(incidente.getSuperiorInmediato()));
	    txtHistorial.setText(valor(incidente.getHistorial()));
	    cargarImagenesLectura(incidente.getId());

	    cmbPrioridad.setValue(
	            incidente.getPrioridad().name().substring(0,1)
	            + incidente.getPrioridad().name().substring(1).toLowerCase());

	    txtTitulo.setEditable(false);
	    txtDescripcion.setEditable(false);

	    cmbPrioridad.setDisable(true);

	    btnEnviar.setVisible(false);
	    btnAdjuntar.setVisible(false);
	}

	private void cargarImagenesLectura(int incidenteId) {
		List<Imagen> imagenes = imagenDAO.obtenerPorIncidente(incidenteId);
		contenedorImagenes.getChildren().clear();

		for (Imagen adjunto : imagenes) {
			ImageView miniatura = VisorImagenService.crearMiniatura(adjunto, imagenes, 120, 90);
			if (miniatura != null) contenedorImagenes.getChildren().add(miniatura);
		}
	}

	private String valor(String texto) {
		return texto == null ? "" : texto;
	}
	
}
