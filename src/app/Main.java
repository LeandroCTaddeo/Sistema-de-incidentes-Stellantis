package app;

import javafx.application.Application;
import api.IncidenteApiException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import dao.UsuarioDAO;
import database.DatabaseException;
import models.Usuario;

public class Main extends Application {

	@Override
	public void start(Stage stage) {
	    try {
	        UsuarioDAO usuarioDAO = new UsuarioDAO();
	        Usuario usuario = usuarioDAO.obtenerUsuarioActual();

	        if (usuario == null) {
	            throw new IllegalStateException(
	                    "El usuario de Windows actual no está registrado en el sistema."
	            );
	        }

	        FXMLLoader loader;

	        if ("ADMIN".equals(usuario.getRol())) {
	            loader = new FXMLLoader(getClass().getResource("/views/Administrador.fxml"));
	        } else {
	            loader = new FXMLLoader(getClass().getResource("/views/Boletin.fxml"));
	        }

	        Scene scene = new Scene(loader.load(), 1000, 700);

	        stage.setTitle("Sistema de Gestión de Incidentes");
	        stage.setScene(scene);
	        stage.setMaximized(true);
	        stage.show();
	        } catch (DatabaseException | IncidenteApiException | IllegalStateException e) {
	        mostrarErrorInicio(e.getMessage());
	    } catch (Exception e) {
	        mostrarErrorInicio("No se pudo iniciar la aplicación. Revise la configuración e inténtelo nuevamente.");
	    }
	}

	private void mostrarErrorInicio(String detalle) {
	    Alert alerta = new Alert(AlertType.ERROR);
	    alerta.setTitle("No se pudo iniciar el sistema");
	    alerta.setHeaderText("Error de conexión o configuración");
	    alerta.setContentText(detalle);
	    alerta.showAndWait();
	}
}
