package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Conexion {

	private static final String URL =
	        System.getenv().getOrDefault("DB_URL",
	                "jdbc:postgresql://localhost:5432/sistema_incidentes");

	private static final String USER = System.getenv("DB_USER");
	private static final String PASSWORD = System.getenv("DB_PASSWORD");

    private Conexion() {
    }

    public static Connection conectar() {
        validarConfiguracion();

        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new DatabaseException(
                    "No se pudo conectar con la base de datos. Verifique el servidor, la red o VPN y vuelva a intentarlo.",
                    e
            );
        }
    }

    private static void validarConfiguracion() {
        if (USER == null || USER.isBlank() || PASSWORD == null || PASSWORD.isBlank()) {
            throw new DatabaseException(
                    "Falta configurar DB_USER o DB_PASSWORD para iniciar la aplicación."
            );
        }
    }
}
