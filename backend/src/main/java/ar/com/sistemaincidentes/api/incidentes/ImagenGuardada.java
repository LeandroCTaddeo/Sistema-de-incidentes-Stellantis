package ar.com.sistemaincidentes.api.incidentes;

import java.nio.file.Path;

record ImagenGuardada(
        String rutaRelativa,
        Path rutaFisica
) {
}
