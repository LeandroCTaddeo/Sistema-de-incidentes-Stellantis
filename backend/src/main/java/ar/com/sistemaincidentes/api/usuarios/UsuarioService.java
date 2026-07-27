package ar.com.sistemaincidentes.api.usuarios;

import org.springframework.stereotype.Service;

import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

@Service
public class UsuarioService {

    private static final int LONGITUD_MAXIMA_USUARIO = 150;

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioResponse obtenerActual(String usuarioWindows) {
        String usuario = validarUsuario(usuarioWindows);
        return repository.buscarPorUsuarioWindows(usuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario de Windows actual no está registrado en el sistema."
                ));
    }

    private String validarUsuario(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el usuario de Windows.");
        }
        String usuario = valor.trim();
        if (usuario.length() > LONGITUD_MAXIMA_USUARIO) {
            throw new IllegalArgumentException("El usuario de Windows es demasiado largo.");
        }
        if (usuario.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("El usuario de Windows contiene caracteres inválidos.");
        }
        return usuario;
    }
}
