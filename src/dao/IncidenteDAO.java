package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import database.Conexion;
import models.Incidente;

import java.util.ArrayList;
import java.util.List;
import java.sql.ResultSet;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;

import api.IncidenteApiClient;
import api.AdministracionApiClient;
import models.Prioridad;

public class IncidenteDAO {

	private IncidenteApiClient apiClient;
	private AdministracionApiClient administracionApiClient;

	private boolean usarApiParaLecturas() {
		return "API".equalsIgnoreCase(
				System.getenv().getOrDefault("INCIDENTES_DATA_SOURCE", "JDBC")
		);
	}

	private IncidenteApiClient apiClient() {
		if (apiClient == null) {
			apiClient = new IncidenteApiClient();
		}
		return apiClient;
	}

	private AdministracionApiClient administracionApiClient() {
		if (administracionApiClient == null) {
			administracionApiClient = new AdministracionApiClient();
		}
		return administracionApiClient;
	}

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
		if (usarApiParaLecturas()) return apiClient().listar(null);
		return obtenerPorEstado(null);
	}

	public List<Incidente> obtenerPendientes() {
		if (usarApiParaLecturas()) return apiClient().listar("PENDIENTE");
		return obtenerPorEstado("PENDIENTE");
	}

	public List<Incidente> obtenerResueltos() {
		if (usarApiParaLecturas()) return apiClient().listar("RESUELTO");
		return obtenerPorEstado("RESUELTO");
	}

	public List<Incidente> buscarResueltos(String texto, LocalDate desde, LocalDate hasta) {
		List<Incidente> lista = new ArrayList<>();
		String termino = texto == null ? "" : texto.trim().toLowerCase();

		StringBuilder sql = new StringBuilder("""
				SELECT i.*, u.nombre, u.sector
				FROM incidentes i
				JOIN usuarios u ON i.usuario_id = u.id
				WHERE i.estado = 'RESUELTO'
				""");

		if (!termino.isBlank()) {
			sql.append("""
					AND (
					    LOWER(i.titulo) LIKE ?
					 OR LOWER(u.nombre) LIKE ?
					 OR LOWER(COALESCE(i.area, '')) LIKE ?
					 OR LOWER(COALESCE(u.sector, '')) LIKE ?
					 OR CAST(i.id AS TEXT) LIKE ?
					)
					""");
		}

		if (desde != null) sql.append(" AND COALESCE(i.fecha_resolucion, i.fecha) >= ? ");
		if (hasta != null) sql.append(" AND COALESCE(i.fecha_resolucion, i.fecha) < ? ");
		sql.append(" ORDER BY COALESCE(i.fecha_resolucion, i.fecha) DESC, i.id DESC");

		try (Connection conexion = Conexion.conectar();
			 PreparedStatement ps = conexion.prepareStatement(sql.toString())) {

			int parametro = 1;
			if (!termino.isBlank()) {
				String patron = "%" + termino + "%";
				for (int i = 0; i < 5; i++) ps.setString(parametro++, patron);
			}
			if (desde != null) ps.setTimestamp(parametro++, Timestamp.valueOf(desde.atStartOfDay()));
			if (hasta != null) ps.setTimestamp(parametro++, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));

			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) lista.add(mapear(rs));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return lista;
	}

	private List<Incidente> obtenerPorEstado(String estado) {

		List<Incidente> lista = new ArrayList<>();

		String sqlBase = """
				SELECT
				i.*,
				u.nombre,
				u.sector
				FROM incidentes i
				JOIN usuarios u
				ON i.usuario_id = u.id
				""";
		String sql = sqlBase
				+ (estado == null ? "" : " WHERE i.estado = ? ")
				+ " ORDER BY i.id DESC";

		try {

			Connection conexion = Conexion.conectar();

			PreparedStatement ps = conexion.prepareStatement(sql);
			if (estado != null) {
				ps.setString(1, estado);
			}

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				lista.add(mapear(rs));
			}

			rs.close();
			ps.close();
			conexion.close();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return lista;

	}

	private Incidente mapear(ResultSet rs) throws Exception {
		return new Incidente(
				rs.getInt("id"), rs.getString("titulo"), rs.getString("descripcion"),
				Prioridad.valueOf(rs.getString("prioridad")), rs.getString("estado"),
				rs.getInt("usuario_id"),
				rs.getString("nombre"), rs.getString("sector"),
				rs.getTimestamp("fecha").toLocalDateTime().toString(),
				obtenerFecha(rs, "fecha_registro"), obtenerFecha(rs, "fecha_emision"),
				rs.getString("lugar"), rs.getString("nombre_apellido"), rs.getString("cargo"),
				rs.getString("matricula"), rs.getString("dni"), rs.getString("area"),
				rs.getString("superior_inmediato"), rs.getString("historial"),
				obtenerFechaHora(rs, "fecha_resolucion"), obtenerEntero(rs, "resuelto_por")
		);
	}

	private java.time.LocalDate obtenerFecha(ResultSet rs, String columna) throws Exception {
		Date fecha = rs.getDate(columna);
		return fecha == null ? null : fecha.toLocalDate();
	}

	private java.time.LocalDateTime obtenerFechaHora(ResultSet rs, String columna) throws Exception {
		Timestamp fecha = rs.getTimestamp(columna);
		return fecha == null ? null : fecha.toLocalDateTime();
	}

	private Integer obtenerEntero(ResultSet rs, String columna) throws Exception {
		int valor = rs.getInt(columna);
		return rs.wasNull() ? null : valor;
	}
	
	public boolean resolver(int id, int administradorId) {
		if (usarApiParaLecturas()) {
			return administracionApiClient().resolverIncidente(id, administradorId);
		}

	    String sql = """
	            UPDATE incidentes
	            SET estado='RESUELTO',
	                fecha_resolucion=CURRENT_TIMESTAMP,
	                resuelto_por=?
	            WHERE id=? AND estado <> 'RESUELTO'
	            """;

	    try {

	        Connection conexion = Conexion.conectar();

	        PreparedStatement ps = conexion.prepareStatement(sql);

	        ps.setInt(1, administradorId);
	        ps.setInt(2, id);

	        int filas = ps.executeUpdate();

	        ps.close();
	        conexion.close();
	        return filas == 1;

	    } catch (Exception e) {

	        e.printStackTrace();

	    }

	    return false;

	}
}
