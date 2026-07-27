package api;

public record UsuarioGuardarApiRequest(
        String nombre,
        String usuarioWindows,
        String sector,
        String rol
) {
}
