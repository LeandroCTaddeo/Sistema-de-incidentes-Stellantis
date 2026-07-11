package models;

public class Imagen {

    private int id;
    private int incidenteId;
    private String ruta;

    public Imagen(int id, int incidenteId, String ruta) {

        this.id = id;
        this.incidenteId = incidenteId;
        this.ruta = ruta;

    }

    public int getId() {
        return id;
    }

    public int getIncidenteId() {
        return incidenteId;
    }

    public String getRuta() {
        return ruta;
    }

}
