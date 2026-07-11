package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.Conexion;
import models.Usuario;

public class UsuarioDAO {

    public Usuario obtenerUsuarioActual() {

        String usuarioWindows = System.getProperty("user.name");

        String sql = """
                SELECT *
                FROM usuarios
                WHERE usuario_windows = ?
                """;

        try {

            Connection conexion = Conexion.conectar();

            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setString(1, usuarioWindows);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("rol")
                );

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return null;

    }

}
