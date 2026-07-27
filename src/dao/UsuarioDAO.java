package dao;

import api.UsuarioApiClient;
import models.Usuario;

public class UsuarioDAO {

    private UsuarioApiClient apiClient;

    private UsuarioApiClient apiClient() {
        if (apiClient == null) {
            apiClient = new UsuarioApiClient();
        }
        return apiClient;
    }

    public Usuario obtenerUsuarioActual() {
        return apiClient().obtenerPorWindows(System.getProperty("user.name"));
    }
}
