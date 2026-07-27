package ar.com.sistemaincidentes.api.usuarios;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.sistemaincidentes.api.web.ConflictoOperacionException;
import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

@Service
public class UsuarioService {

    private static final int LONGITUD_MAXIMA_USUARIO = 150;
    private static final int LONGITUD_MAXIMA_NOMBRE = 150;
    private static final int LONGITUD_MAXIMA_SECTOR = 100;
    private static final Set<String> ROLES = Set.of("ADMIN", "EMPLEADO");

    private final UsuarioRepository repository;

    public UsuarioService(UsuarioRepository repository) {
        this.repository = repository;
    }

    public UsuarioResponse obtenerActual(String usuarioWindows) {
        String usuario = validarUsuarioWindows(usuarioWindows);
        return repository.buscarPorUsuarioWindows(usuario)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "El usuario de Windows actual no está registrado o está deshabilitado."
                ));
    }

    public List<UsuarioAdministracionResponse> listar(String busqueda) {
        String termino = busqueda == null ? "" : busqueda.trim();
        if (termino.length() > 150) {
            throw new IllegalArgumentException("La búsqueda es demasiado larga.");
        }
        return repository.listar(termino);
    }

    @Transactional
    public UsuarioAdministracionResponse crear(UsuarioGuardarRequest request) {
        DatosValidados datos = validarDatos(request);
        validarUsuarioWindowsDisponible(datos.usuarioWindows(), null);
        return repository.crear(
                datos.nombre(),
                datos.usuarioWindows(),
                datos.sector(),
                datos.rol()
        );
    }

    @Transactional
    public UsuarioAdministracionResponse actualizar(int id, UsuarioGuardarRequest request) {
        UsuarioAdministracionResponse actual = obtenerAdministrable(id);
        DatosValidados datos = validarDatos(request);
        validarUsuarioWindowsDisponible(datos.usuarioWindows(), id);
        validarConservaAdministrador(actual, datos.rol(), actual.activo());

        if (!repository.actualizar(
                id,
                datos.nombre(),
                datos.usuarioWindows(),
                datos.sector(),
                datos.rol()
        )) {
            throw new RecursoNoEncontradoException("No se encontró el usuario indicado.");
        }
        return obtenerAdministrable(id);
    }

    @Transactional
    public UsuarioAdministracionResponse cambiarEstado(int id, UsuarioEstadoRequest request) {
        if (request == null || request.activo() == null) {
            throw new IllegalArgumentException("Debe indicar el estado del usuario.");
        }
        UsuarioAdministracionResponse actual = obtenerAdministrable(id);
        validarConservaAdministrador(actual, actual.rol(), request.activo());

        if (!repository.actualizarEstado(id, request.activo())) {
            throw new RecursoNoEncontradoException("No se encontró el usuario indicado.");
        }
        return obtenerAdministrable(id);
    }

    private UsuarioAdministracionResponse obtenerAdministrable(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador del usuario no es válido.");
        }
        return repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el usuario indicado."
                ));
    }

    private DatosValidados validarDatos(UsuarioGuardarRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Debe enviar los datos del usuario.");
        }
        String nombre = validarTextoObligatorio(
                request.nombre(), "nombre", LONGITUD_MAXIMA_NOMBRE
        );
        String usuarioWindows = validarUsuarioWindows(request.usuarioWindows());
        String sector = validarTextoOpcional(request.sector(), "sector", LONGITUD_MAXIMA_SECTOR);
        String rol = validarRol(request.rol());
        return new DatosValidados(nombre, usuarioWindows, sector, rol);
    }

    private void validarUsuarioWindowsDisponible(String usuarioWindows, Integer excluirId) {
        if (repository.existeUsuarioWindows(usuarioWindows, excluirId)) {
            throw new ConflictoOperacionException(
                    "Ya existe un usuario con esa cuenta de Windows."
            );
        }
    }

    private void validarConservaAdministrador(
            UsuarioAdministracionResponse actual,
            String nuevoRol,
            boolean nuevoActivo
    ) {
        boolean dejaDeGestionarCasos = actual.activo()
                && "ADMIN".equals(actual.rol())
                && (!nuevoActivo || !"ADMIN".equals(nuevoRol));
        if (dejaDeGestionarCasos && actual.casosAbiertos() > 0) {
            throw new ConflictoOperacionException(
                    "El administrador tiene casos abiertos. Debe liberarlos o resolverlos antes."
            );
        }
        boolean quitaAdministradorActivo = actual.activo()
                && "ADMIN".equals(actual.rol())
                && (!nuevoActivo || !"ADMIN".equals(nuevoRol));
        if (quitaAdministradorActivo
                && repository.contarAdministradoresActivosExcepto(actual.id()) == 0) {
            throw new ConflictoOperacionException(
                    "Debe quedar al menos un administrador activo."
            );
        }
    }

    private String validarUsuarioWindows(String valor) {
        return validarTextoObligatorio(
                valor, "usuario de Windows", LONGITUD_MAXIMA_USUARIO
        );
    }

    private String validarRol(String valor) {
        String rol = validarTextoObligatorio(valor, "rol", 20)
                .toUpperCase(Locale.ROOT);
        if (!ROLES.contains(rol)) {
            throw new IllegalArgumentException("El rol debe ser ADMIN o EMPLEADO.");
        }
        return rol;
    }

    private String validarTextoObligatorio(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el " + campo + ".");
        }
        String texto = valor.trim();
        validarTexto(texto, campo, maximo);
        return texto;
    }

    private String validarTextoOpcional(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        String texto = valor.trim();
        validarTexto(texto, campo, maximo);
        return texto;
    }

    private void validarTexto(String texto, String campo, int maximo) {
        if (texto.length() > maximo) {
            throw new IllegalArgumentException("El " + campo + " es demasiado largo.");
        }
        if (texto.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("El " + campo + " contiene caracteres inválidos.");
        }
    }

    private record DatosValidados(
            String nombre,
            String usuarioWindows,
            String sector,
            String rol
    ) {
    }
}
