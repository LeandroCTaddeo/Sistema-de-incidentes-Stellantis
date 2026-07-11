package models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class BoletinAdmin {

    private int id;
    private int incidenteId;
    private int administradorId;

    private String titulo;
    private String descripcion;

    private LocalDate fechaRegistro;
    private LocalDate fechaEmision;
    private String lugar;
    private String nombreApellido;
    private String cargo;
    private String matricula;
    private String dni;
    private String area;
    private String superiorInmediato;
    private String historial;
    private String prioridad;

    private LocalDateTime fechaCreacion;

    public BoletinAdmin() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIncidenteId() { return incidenteId; }
    public void setIncidenteId(int incidenteId) { this.incidenteId = incidenteId; }

    public int getAdministradorId() { return administradorId; }
    public void setAdministradorId(int administradorId) { this.administradorId = administradorId; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }

    public String getNombreApellido() { return nombreApellido; }
    public void setNombreApellido(String nombreApellido) { this.nombreApellido = nombreApellido; }

    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getSuperiorInmediato() { return superiorInmediato; }
    public void setSuperiorInmediato(String superiorInmediato) { this.superiorInmediato = superiorInmediato; }

    public String getHistorial() { return historial; }
    public void setHistorial(String historial) { this.historial = historial; }

    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}