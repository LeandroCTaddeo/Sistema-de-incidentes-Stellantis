package database;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {

	private static final String URL =
	        System.getenv().getOrDefault("DB_URL",
	                "jdbc:postgresql://localhost:5432/sistema_incidentes");

	private static final String USER = System.getenv("DB_USER");
	private static final String PASSWORD = System.getenv("DB_PASSWORD");

    public static Connection conectar() {

        try {

            return DriverManager.getConnection(URL, USER, PASSWORD);

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }

    }

}