package ar.com.sistemaincidentes.api.web;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> argumentoInvalido(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        return respuesta(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> baseDeDatosNoDisponible(
            DataAccessException exception,
            HttpServletRequest request
    ) {
        return respuesta(
                HttpStatus.SERVICE_UNAVAILABLE,
                "La base de datos no está disponible en este momento.",
                request.getRequestURI()
        );
    }

    private ResponseEntity<ApiErrorResponse> respuesta(
            HttpStatus estado,
            String mensaje,
            String ruta
    ) {
        ApiErrorResponse cuerpo = new ApiErrorResponse(
                Instant.now(),
                estado.value(),
                estado.getReasonPhrase(),
                mensaje,
                ruta
        );
        return ResponseEntity.status(estado).body(cuerpo);
    }

    public record ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
    }
}
