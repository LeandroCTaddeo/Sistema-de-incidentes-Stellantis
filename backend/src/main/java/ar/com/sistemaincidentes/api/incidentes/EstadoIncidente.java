package ar.com.sistemaincidentes.api.incidentes;

import java.util.Locale;

public enum EstadoIncidente {
    PENDIENTE,
    RESUELTO;

    public static EstadoIncidente desdeParametro(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        try {
            return valueOf(valor.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Estado inválido. Los valores permitidos son PENDIENTE y RESUELTO."
            );
        }
    }
}
