package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import database.Conexion;
import java.sql.Connection;
import dao.UsuarioDAO;
import models.Usuario;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {

	    UsuarioDAO usuarioDAO = new UsuarioDAO();

	    Usuario usuario = usuarioDAO.obtenerUsuarioActual();

	    FXMLLoader loader;

	    if (usuario.getRol().equals("ADMIN")) {

	        loader = new FXMLLoader(getClass().getResource("/views/Administrador.fxml"));

	    } else {

	        loader = new FXMLLoader(getClass().getResource("/views/Boletin.fxml"));

	    }

	    Scene scene = new Scene(loader.load(), 1000, 700);

	    stage.setTitle("Sistema de Gestión de Incidentes");
	    stage.setScene(scene);
	    stage.setMaximized(true);
	    stage.show();

	}
}
