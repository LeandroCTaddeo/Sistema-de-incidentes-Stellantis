package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import database.Conexion;
import database.DatabaseException;
import models.Usuario;

public class UsuarioDAO {

    public Usuario obtenerUsuarioActual() {

        String usuarioWindows = System.getProperty("user.name");

        String sql = """
                SELECT *
                FROM usuarios
                WHERE usuario_windows = ?
                """;

        try (Connection conexion = Conexion.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, usuarioWindows);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("rol")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("No se pudo consultar el usuario de Windows en la base de datos.", e);
        }

        return null;

    }

}
