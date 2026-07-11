package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import database.Conexion;
import models.Imagen;

public class ImagenDAO {

    public void guardar(int incidenteId, String ruta) {

        String sql = """
                INSERT INTO imagenes
                (incidente_id,ruta)
                VALUES (?,?)
                """;

        try {

            Connection conexion = Conexion.conectar();

            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, incidenteId);
            ps.setString(2, ruta);

            ps.executeUpdate();

            ps.close();
            conexion.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    public List<Imagen> obtenerPorIncidente(int incidenteId) {

        List<Imagen> lista = new ArrayList<>();

        String sql = """
                SELECT *
                FROM imagenes
                WHERE incidente_id=?
                ORDER BY id ASC
                """;

        try {

            Connection conexion = Conexion.conectar();

            PreparedStatement ps = conexion.prepareStatement(sql);

            ps.setInt(1, incidenteId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                lista.add(

                        new Imagen(
                                rs.getInt("id"),
                                rs.getInt("incidente_id"),
                                rs.getString("ruta")
                        )

                );

            }

            rs.close();
            ps.close();
            conexion.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;

    }

}
