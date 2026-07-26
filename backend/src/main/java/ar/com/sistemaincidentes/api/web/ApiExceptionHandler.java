package ar.com.sistemaincidentes.api.web;

import java.time.Instant;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> validacionInvalida(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String mensaje = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(this::mensajeValidacion)
                .orElse("Los datos enviados no son válidos.");
        return respuesta(HttpStatus.BAD_REQUEST, mensaje, request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> contenidoInvalido(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return respuesta(
                HttpStatus.BAD_REQUEST,
                "El contenido del boletín no tiene un formato válido.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> cargaDemasiadoGrande(
            MaxUploadSizeExceededException exception,
            HttpServletRequest request
    ) {
        return respuesta(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Las imágenes adjuntas superan el tamaño máximo permitido.",
                request.getRequestURI()
        );
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> recursoNoEncontrado(
            RecursoNoEncontradoException exception,
            HttpServletRequest request
    ) {
        return respuesta(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConflictoOperacionException.class)
    public ResponseEntity<ApiErrorResponse> conflictoOperacion(
            ConflictoOperacionException exception,
            HttpServletRequest request
    ) {
        return respuesta(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

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

    private String mensajeValidacion(FieldError error) {
        return "El campo '" + error.getField() + "' "
                + (error.getDefaultMessage() == null
                        ? "no es válido."
                        : error.getDefaultMessage() + ".");
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
