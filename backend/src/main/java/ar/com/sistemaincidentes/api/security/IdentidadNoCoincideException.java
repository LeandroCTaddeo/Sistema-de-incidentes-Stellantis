package ar.com.sistemaincidentes.api.security;

public class IdentidadNoCoincideException extends RuntimeException {

    public IdentidadNoCoincideException(String message) {
        super(message);
    }
}
