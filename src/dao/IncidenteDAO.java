package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import database.Conexion;
import models.Incidente;

import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Date;
import models.Prioridad;

public class IncidenteDAO {

	public int guardar(Incidente incidente) {

		    String sql = """
		        INSERT INTO incidentes
		        (titulo, descripcion, prioridad, usuario_id,
		         fecha_registro, fecha_emision, lugar, nombre_apellido,
		         cargo, matricula, dni, area, superior_inmediato, historial)
		        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
		        """;

		    try {

		        Connection conexion = Conexion.conectar();

		        PreparedStatement ps = conexion.prepareStatement(
		                sql,
		                PreparedStatement.RETURN_GENERATED_KEYS
		        );

		        ps.setString(1, incidente.getTitulo());
		        ps.setString(2, incidente.getDescripcion());
		        ps.setString(3, incidente.getPrioridad().toString());
		        ps.setInt(4, incidente.getUsuarioId());
		        ps.setObject(5, incidente.getFechaRegistro());
		        ps.setObject(6, incidente.getFechaEmision());
		        ps.setString(7, incidente.getLugar());
		        ps.setString(8, incidente.getNombreApellido());
		        ps.setString(9, incidente.getCargo());
		        ps.setString(10, incidente.getMatricula());
		        ps.setString(11, incidente.getDni());
		        ps.setString(12, incidente.getArea());
		        ps.setString(13, incidente.getSuperiorInmediato());
		        ps.setString(14, incidente.getHistorial());

		        ps.executeUpdate();

		        ResultSet rs = ps.getGeneratedKeys();

		        int id = 0;

		        if (rs.next()) {
		            id = rs.getInt(1);
		        }

		        rs.close();
		        ps.close();
		        conexion.close();

		        return id;

		    } catch (Exception e) {

		        e.printStackTrace();

		    }

		    return 0;

		}

	public List<Incidente> obtenerTodos() {

		List<Incidente> lista = new ArrayList<>();

		String sql = """
				SELECT
				i.*,
				u.nombre,
				u.sector
				FROM incidentes i
				JOIN usuarios u
				ON i.usuario_id = u.id
				ORDER BY i.id DESC
				""";

		try {

			Connection conexion = Conexion.conectar();

			PreparedStatement ps = conexion.prepareStatement(sql);

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {

				lista.add(
						new Incidente(
						        rs.getInt("id"),
						        rs.getString("titulo"),
						        rs.getString("descripcion"),
						        Prioridad.valueOf(rs.getString("prioridad")),
						        rs.getString("estado"),
						        rs.getString("nombre"),
						        rs.getString("sector"),
						        rs.getTimestamp("fecha").toLocalDateTime().toString(),
						        obtenerFecha(rs, "fecha_registro"),
						        obtenerFecha(rs, "fecha_emision"),
						        rs.getString("lugar"),
						        rs.getString("nombre_apellido"),
						        rs.getString("cargo"),
						        rs.getString("matricula"),
						        rs.getString("dni"),
						        rs.getString("area"),
						        rs.getString("superior_inmediato"),
						        rs.getString("historial")
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

	private java.time.LocalDate obtenerFecha(ResultSet rs, String columna) throws Exception {
		Date fecha = rs.getDate(columna);
		return fecha == null ? null : fecha.toLocalDate();
	}
	
	public void resolver(int id) {

	    String sql = """
	            UPDATE incidentes
	            SET estado='RESUELTO'
	            WHERE id=?
	            """;

	    try {

	        Connection conexion = Conexion.conectar();

	        PreparedStatement ps = conexion.prepareStatement(sql);

	        ps.setInt(1, id);

	        ps.executeUpdate();

	        ps.close();
	        conexion.close();

	    } catch (Exception e) {

	        e.printStackTrace();

	    }

	}
}
