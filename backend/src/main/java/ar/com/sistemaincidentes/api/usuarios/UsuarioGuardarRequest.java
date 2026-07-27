package ar.com.sistemaincidentes.api.usuarios;

public record UsuarioGuardarRequest(
        String nombre,
        String usuarioWindows,
        String sector,
        String rol
) {
}
