package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database.Conexion;
import models.BoletinAdmin;

public class BoletinAdminDAO {

    public int guardar(BoletinAdmin b) {
        String sql = """
            INSERT INTO boletines_admin
            (
                incidente_id, administrador_id, titulo, descripcion,
                fecha_registro, fecha_emision, lugar,
                nombre_apellido, cargo, matricula, dni,
                area, superior_inmediato, historial, prioridad
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, b.getIncidenteId());
            stmt.setInt(2, b.getAdministradorId());
            stmt.setString(3, b.getTitulo());
            stmt.setString(4, b.getDescripcion());

            stmt.setDate(5, b.getFechaRegistro() != null ? Date.valueOf(b.getFechaRegistro()) : null);
            stmt.setDate(6, b.getFechaEmision() != null ? Date.valueOf(b.getFechaEmision()) : null);

            stmt.setString(7, b.getLugar());
            stmt.setString(8, b.getNombreApellido());
            stmt.setString(9, b.getCargo());
            stmt.setString(10, b.getMatricula());
            stmt.setString(11, b.getDni());
            stmt.setString(12, b.getArea());
            stmt.setString(13, b.getSuperiorInmediato());
            stmt.setString(14, b.getHistorial());
            stmt.setString(15, b.getPrioridad());

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (SQLException e) {
            System.out.println("Error al guardar boletín admin: " + e.getMessage());
        }

        return -1;
    }

    public void actualizar(BoletinAdmin b) {
        String sql = """
            UPDATE boletines_admin
            SET titulo = ?,
                descripcion = ?,
                fecha_registro = ?,
                fecha_emision = ?,
                lugar = ?,
                nombre_apellido = ?,
                cargo = ?,
                matricula = ?,
                dni = ?,
                area = ?,
                superior_inmediato = ?,
                historial = ?,
                prioridad = ?
            WHERE id = ?
        """;

        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, b.getTitulo());
            stmt.setString(2, b.getDescripcion());

            stmt.setDate(3, b.getFechaRegistro() != null ? Date.valueOf(b.getFechaRegistro()) : null);
            stmt.setDate(4, b.getFechaEmision() != null ? Date.valueOf(b.getFechaEmision()) : null);

            stmt.setString(5, b.getLugar());
            stmt.setString(6, b.getNombreApellido());
            stmt.setString(7, b.getCargo());
            stmt.setString(8, b.getMatricula());
            stmt.setString(9, b.getDni());
            stmt.setString(10, b.getArea());
            stmt.setString(11, b.getSuperiorInmediato());
            stmt.setString(12, b.getHistorial());
            stmt.setString(13, b.getPrioridad());
            stmt.setInt(14, b.getId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Error al actualizar boletín admin: " + e.getMessage());
        }
    }

    public List<BoletinAdmin> obtenerPorIncidente(int incidenteId) {
        List<BoletinAdmin> boletines = new ArrayList<>();

        String sql = """
            SELECT *
            FROM boletines_admin
            WHERE incidente_id = ?
            ORDER BY fecha_creacion DESC
        """;

        try (Connection conn = Conexion.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, incidenteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                boletines.add(mapear(rs));
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener boletines admin: " + e.getMessage());
        }

        return boletines;
    }

    private BoletinAdmin mapear(ResultSet rs) throws SQLException {
        BoletinAdmin b = new BoletinAdmin();

        b.setId(rs.getInt("id"));
        b.setIncidenteId(rs.getInt("incidente_id"));
        b.setAdministradorId(rs.getInt("administrador_id"));
        b.setTitulo(rs.getString("titulo"));
        b.setDescripcion(rs.getString("descripcion"));

        Date fechaRegistro = rs.getDate("fecha_registro");
        if (fechaRegistro != null) b.setFechaRegistro(fechaRegistro.toLocalDate());

        Date fechaEmision = rs.getDate("fecha_emision");
        if (fechaEmision != null) b.setFechaEmision(fechaEmision.toLocalDate());

        b.setLugar(rs.getString("lugar"));
        b.setNombreApellido(rs.getString("nombre_apellido"));
        b.setCargo(rs.getString("cargo"));
        b.setMatricula(rs.getString("matricula"));
        b.setDni(rs.getString("dni"));
        b.setArea(rs.getString("area"));
        b.setSuperiorInmediato(rs.getString("superior_inmediato"));
        b.setHistorial(rs.getString("historial"));
        b.setPrioridad(rs.getString("prioridad"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) b.setFechaCreacion(fechaCreacion.toLocalDateTime());

        return b;
    }
}