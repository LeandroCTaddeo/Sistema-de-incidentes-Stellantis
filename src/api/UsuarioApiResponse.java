package api;

import models.Usuario;

public record UsuarioApiResponse(int id, String nombre, String rol) {

    public Usuario convertir() {
        return new Usuario(id, nombre, rol);
    }
}
