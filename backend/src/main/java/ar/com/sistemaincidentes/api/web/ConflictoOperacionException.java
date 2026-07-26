package ar.com.sistemaincidentes.api.web;

public class ConflictoOperacionException extends RuntimeException {

    public ConflictoOperacionException(String mensaje) {
        super(mensaje);
    }
}
