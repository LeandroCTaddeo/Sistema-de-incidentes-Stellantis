package api;

public class IncidenteApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IncidenteApiException(String message) {
        super(message);
    }

    public IncidenteApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
