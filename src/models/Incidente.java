package models;

import java.time.LocalDate;

public class Incidente {

    private int id;
    private String titulo;
    private String descripcion;
    private Prioridad prioridad;
    private String estado;
    private int usuarioId;
    private String nombreEmpleado;
    private String sector;
    private String fecha;
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

    // Constructor para crear un incidente nuevo
    public Incidente(String titulo, String descripcion, Prioridad prioridad, int usuarioId,
                     LocalDate fechaRegistro, LocalDate fechaEmision, String lugar,
                     String nombreApellido, String cargo, String matricula, String dni,
                     String area, String superiorInmediato, String historial) {

        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.usuarioId = usuarioId;
        this.fechaRegistro = fechaRegistro;
        this.fechaEmision = fechaEmision;
        this.lugar = lugar;
        this.nombreApellido = nombreApellido;
        this.cargo = cargo;
        this.matricula = matricula;
        this.dni = dni;
        this.area = area;
        this.superiorInmediato = superiorInmediato;
        this.historial = historial;
    }

    // Constructor para leer desde la base de datos
    public Incidente(int id,
            String titulo,
            String descripcion,
            Prioridad prioridad,
            String estado,
            String nombreEmpleado,
            String sector,
            String fecha, LocalDate fechaRegistro, LocalDate fechaEmision, String lugar,
            String nombreApellido, String cargo, String matricula, String dni,
            String area, String superiorInmediato, String historial) {

        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.nombreEmpleado = nombreEmpleado;
        this.sector = sector;
        this.fecha = fecha;
        this.fechaRegistro = fechaRegistro;
        this.fechaEmision = fechaEmision;
        this.lugar = lugar;
        this.nombreApellido = nombreApellido;
        this.cargo = cargo;
        this.matricula = matricula;
        this.dni = dni;
        this.area = area;
        this.superiorInmediato = superiorInmediato;
        this.historial = historial;
    }

    public int getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Prioridad getPrioridad() {
        return prioridad;
    }

    public String getEstado() {
        return estado;
    }

    public int getUsuarioId() {
        return usuarioId;
    }
    
    public String getNombreEmpleado() {
        return nombreEmpleado;
    }

    public String getSector() {
        return sector;
    }
    
    public String getFecha() {
        return fecha;
    }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public LocalDate getFechaEmision() { return fechaEmision; }
    public String getLugar() { return lugar; }
    public String getNombreApellido() { return nombreApellido; }
    public String getCargo() { return cargo; }
    public String getMatricula() { return matricula; }
    public String getDni() { return dni; }
    public String getArea() { return area; }
    public String getSuperiorInmediato() { return superiorInmediato; }
    public String getHistorial() { return historial; }

}
