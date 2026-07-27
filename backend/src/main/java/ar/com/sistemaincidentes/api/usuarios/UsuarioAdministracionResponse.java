package ar.com.sistemaincidentes.api.usuarios;

public record UsuarioAdministracionResponse(
        int id,
        String nombre,
        String usuarioWindows,
        String sector,
        String rol,
        boolean activo,
        long casosAbiertos,
        long casosResueltos
) {
    public UsuarioAdministracionResponse(
            int id,
            String nombre,
            String usuarioWindows,
            String sector,
            String rol,
            boolean activo
    ) {
        this(id, nombre, usuarioWindows, sector, rol, activo, 0, 0);
    }
}
