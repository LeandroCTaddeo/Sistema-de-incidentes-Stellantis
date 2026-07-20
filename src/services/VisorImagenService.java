package services;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import models.Imagen;

public final class VisorImagenService {

    private static final AlmacenamientoImagenService ALMACENAMIENTO =
            new AlmacenamientoImagenService();

    private VisorImagenService() {}

    public static ImageView crearMiniatura(
            Imagen adjunto, List<Imagen> todas, double ancho, double alto) {

        File archivo = resolverArchivo(adjunto);
        if (archivo == null) return null;
        if (!archivo.isFile()) return null;

        ImageView vista = new ImageView(
                new Image(archivo.toURI().toString(), ancho, alto, true, true)
        );
        vista.setFitWidth(ancho);
        vista.setFitHeight(alto);
        vista.setPreserveRatio(true);
        vista.setSmooth(true);
        vista.getStyleClass().add("attachment-thumbnail");
        Tooltip.install(vista, new Tooltip("Doble clic para ampliar"));

        vista.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                abrir(todas, adjunto, vista.getScene().getWindow());
            }
        });

        return vista;
    }

    public static void abrir(List<Imagen> adjuntos, Imagen seleccionado, Window owner) {
        List<File> archivos = new ArrayList<>();
        for (Imagen adjunto : adjuntos) {
            File archivo = resolverArchivo(adjunto);
            if (archivo != null && archivo.isFile()) archivos.add(archivo);
        }

        if (archivos.isEmpty()) {
            mostrarError("No se encontró el archivo de imagen.", owner);
            return;
        }

        File seleccionadoArchivo = resolverArchivo(seleccionado);
        int posicionInicial = 0;
        for (int i = 0; i < archivos.size(); i++) {
            if (seleccionadoArchivo != null && archivos.get(i).equals(seleccionadoArchivo)) {
                posicionInicial = i;
                break;
            }
        }

        Stage stage = new Stage();
        if (owner != null) stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        stage.setTitle("Imagen adjunta");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("image-viewer-root");

        Label titulo = new Label();
        titulo.getStyleClass().add("image-viewer-title");
        BorderPane.setAlignment(titulo, Pos.CENTER);
        root.setTop(titulo);

        ImageView imagenView = new ImageView();
        imagenView.setPreserveRatio(true);
        imagenView.setSmooth(true);

        StackPane centro = new StackPane(imagenView);
        centro.getStyleClass().add("image-viewer-canvas");
        imagenView.fitWidthProperty().bind(centro.widthProperty().subtract(40));
        imagenView.fitHeightProperty().bind(centro.heightProperty().subtract(40));
        root.setCenter(centro);

        Button anterior = new Button("Anterior");
        Button siguiente = new Button("Siguiente");
        Button guardar = new Button("Guardar imagen");
        Button guardarTodas = new Button("Guardar todas");
        Button cerrar = new Button("Cerrar");
        anterior.getStyleClass().add("button-secondary");
        siguiente.getStyleClass().add("button-secondary");
        guardar.getStyleClass().add("button-primary");
        guardarTodas.getStyleClass().add("button-secondary");
        cerrar.getStyleClass().add("button-secondary");

        HBox acciones = new HBox(10, anterior, siguiente, guardar, guardarTodas, cerrar);
        acciones.setAlignment(Pos.CENTER);
        acciones.getStyleClass().add("image-viewer-actions");
        root.setBottom(acciones);

        int[] posicion = { posicionInicial };
        Runnable actualizar = () -> {
            File actual = archivos.get(posicion[0]);
            imagenView.setImage(new Image(actual.toURI().toString()));
            titulo.setText(actual.getName() + "  ·  " + (posicion[0] + 1) + " de " + archivos.size());
            anterior.setDisable(posicion[0] == 0);
            siguiente.setDisable(posicion[0] == archivos.size() - 1);
        };

        anterior.setOnAction(e -> { posicion[0]--; actualizar.run(); });
        siguiente.setOnAction(e -> { posicion[0]++; actualizar.run(); });
        guardar.setOnAction(e -> guardarArchivo(archivos.get(posicion[0]), stage));
        guardarTodas.setOnAction(e -> guardarTodos(archivos, stage));
        cerrar.setOnAction(e -> stage.close());

        Scene scene = new Scene(root, 1100, 760);
        var css = VisorImagenService.class.getResource("/resources/css/admin.css");
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        stage.setScene(scene);
        stage.setMinWidth(700);
        stage.setMinHeight(500);
        stage.setMaximized(true);
        actualizar.run();
        stage.show();
    }

    private static void guardarArchivo(File origen, Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar imagen");
        chooser.setInitialFileName(origen.getName());
        File destino = chooser.showSaveDialog(owner);
        if (destino == null) return;

        try {
            Files.copy(origen.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            mostrarError("No se pudo guardar la imagen.", owner);
        }
    }

    private static void guardarTodos(List<File> archivos, Window owner) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Guardar imágenes adjuntas");
        File carpeta = chooser.showDialog(owner);
        if (carpeta == null) return;

        try {
            for (File origen : archivos) {
                Path destino = destinoDisponible(carpeta.toPath(), origen.getName());
                Files.copy(origen.toPath(), destino);
            }
        } catch (Exception e) {
            mostrarError("No se pudieron guardar todas las imágenes.", owner);
        }
    }

    private static Path destinoDisponible(Path carpeta, String nombre) {
        Path destino = carpeta.resolve(nombre);
        if (!Files.exists(destino)) return destino;

        int punto = nombre.lastIndexOf('.');
        String base = punto > 0 ? nombre.substring(0, punto) : nombre;
        String extension = punto > 0 ? nombre.substring(punto) : "";
        int numero = 2;
        while (Files.exists(destino)) {
            destino = carpeta.resolve(base + "_" + numero + extension);
            numero++;
        }
        return destino;
    }

    private static File resolverArchivo(Imagen adjunto) {
        try {
            return ALMACENAMIENTO.resolver(adjunto.getRuta());
        } catch (StorageException e) {
            return null;
        }
    }

    private static void mostrarError(String mensaje, Window owner) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        if (owner != null) alerta.initOwner(owner);
        alerta.setTitle("Imágenes adjuntas");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
