package services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import api.FirmanteApiResponse;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

public class SelectorFirmantesDialog {

    public Optional<List<Integer>> seleccionar(
            List<FirmanteApiResponse> firmantes,
            Window owner
    ) {
        List<FirmanteApiResponse> obligatorios = firmantes.stream()
                .filter(FirmanteApiResponse::obligatorio)
                .toList();
        List<FirmanteApiResponse> alternativas = firmantes.stream()
                .filter(firmante -> !firmante.obligatorio())
                .toList();

        if (obligatorios.isEmpty() || alternativas.isEmpty()) {
            throw new IllegalStateException(
                    "La configuración de firmantes de Palomar está incompleta."
            );
        }

        Dialog<List<Integer>> dialogo = new Dialog<>();
        dialogo.setTitle("Firmas del expediente");
        dialogo.setHeaderText("Seleccione las firmas que aparecerán al final del expediente");
        if (owner != null) dialogo.initOwner(owner);

        ButtonType exportar = new ButtonType(
                "Continuar exportación", ButtonBar.ButtonData.OK_DONE
        );
        dialogo.getDialogPane().getButtonTypes().addAll(exportar, ButtonType.CANCEL);

        VBox contenido = new VBox(12);
        contenido.setPadding(new Insets(8));
        contenido.setPrefWidth(520);

        Label ayuda = new Label(
                "Los firmantes quedarán guardados en el expediente y no podrán cambiarse "
                        + "en exportaciones posteriores."
        );
        ayuda.setWrapText(true);
        ayuda.getStyleClass().add("card-meta");
        contenido.getChildren().add(ayuda);

        Label tituloObligatorios = new Label("Firma obligatoria");
        tituloObligatorios.getStyleClass().add("section-label");
        contenido.getChildren().add(tituloObligatorios);
        for (FirmanteApiResponse firmante : obligatorios) {
            CheckBox fijo = new CheckBox(descripcion(firmante));
            fijo.setSelected(true);
            fijo.setDisable(true);
            fijo.setWrapText(true);
            contenido.getChildren().add(fijo);
        }

        Label tituloAlternativas = new Label("Seleccione una firma de Palomar");
        tituloAlternativas.getStyleClass().add("section-label");
        contenido.getChildren().add(tituloAlternativas);

        ToggleGroup grupo = new ToggleGroup();
        for (FirmanteApiResponse firmante : alternativas) {
            RadioButton opcion = new RadioButton(descripcion(firmante));
            opcion.setUserData(firmante);
            opcion.setToggleGroup(grupo);
            opcion.setWrapText(true);
            contenido.getChildren().add(opcion);
        }

        dialogo.getDialogPane().setContent(contenido);
        var css = getClass().getResource("/resources/css/admin.css");
        if (css != null) dialogo.getDialogPane().getStylesheets().add(css.toExternalForm());

        Button botonExportar = (Button) dialogo.getDialogPane().lookupButton(exportar);
        botonExportar.setDisable(true);
        grupo.selectedToggleProperty().addListener(
                (obs, anterior, actual) -> botonExportar.setDisable(actual == null)
        );

        dialogo.setResultConverter(boton -> {
            if (boton != exportar || grupo.getSelectedToggle() == null) return null;
            List<Integer> ids = new ArrayList<>();
            obligatorios.forEach(firmante -> ids.add(firmante.id()));
            FirmanteApiResponse elegido = (FirmanteApiResponse)
                    grupo.getSelectedToggle().getUserData();
            ids.add(elegido.id());
            return List.copyOf(ids);
        });

        return dialogo.showAndWait();
    }

    private String descripcion(FirmanteApiResponse firmante) {
        return firmante.nombre() + "\n"
                + firmante.areaLinea1() + " - " + firmante.areaLinea2();
    }
}
