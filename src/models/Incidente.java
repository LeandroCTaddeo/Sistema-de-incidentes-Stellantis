package models;

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

    // Constructor para crear un incidente nuevo
    public Incidente(String titulo,
                     String descripcion,
                     Prioridad prioridad,
                     int usuarioId) {

        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.usuarioId = usuarioId;

    }

    // Constructor para leer desde la base de datos
    public Incidente(int id,
            String titulo,
            String descripcion,
            Prioridad prioridad,
            String estado,
            String nombreEmpleado,
            String sector,
            String fecha) {

        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.estado = estado;
        this.nombreEmpleado = nombreEmpleado;
        this.sector = sector;
        this.fecha = fecha;
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

}