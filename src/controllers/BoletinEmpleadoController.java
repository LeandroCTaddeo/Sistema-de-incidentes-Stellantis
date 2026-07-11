package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Incidente;
import models.Imagen;
import dao.ImagenDAO;
import services.VisorImagenService;

import java.io.File;

public class BoletinEmpleadoController {

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
    @FXML private Button btnEnviar;
    @FXML private Button btnAdjuntar;
    @FXML private FlowPane contenedorImagenes;

    private ImagenDAO imagenDAO = new ImagenDAO();

    @FXML
    public void initialize() {

        bloquearTodo();

    }

    private void bloquearTodo() {

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

        cmbPrioridad.getItems().clear();

        String prioridad =
                incidente.getPrioridad().name().substring(0,1) +
                incidente.getPrioridad().name().substring(1).toLowerCase();

        cmbPrioridad.getItems().add(prioridad);
        cmbPrioridad.getSelectionModel().select(prioridad);
        cmbPrioridad.getSelectionModel().selectFirst();

        contenedorImagenes.getChildren().clear();

        var imagenes = imagenDAO.obtenerPorIncidente(incidente.getId());
        for (Imagen img : imagenes) {
            ImageView vista = VisorImagenService.crearMiniatura(img, imagenes, 120, 90);
            if (vista != null) contenedorImagenes.getChildren().add(vista);
        }

    }

    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

}
