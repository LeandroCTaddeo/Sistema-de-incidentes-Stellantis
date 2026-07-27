package api;

public record UsuarioGestionApiResponse(
        int id,
        String nombre,
        String usuarioWindows,
        String sector,
        String rol,
        boolean activo,
        long casosAbiertos,
        long casosResueltos
) {
}
