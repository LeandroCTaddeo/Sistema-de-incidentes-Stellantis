package ar.com.sistemaincidentes.api.firmantes;

import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.sistemaincidentes.api.security.AuthenticatedUserGuard;
import ar.com.sistemaincidentes.api.web.ConflictoOperacionException;
import ar.com.sistemaincidentes.api.web.RecursoNoEncontradoException;

@Service
public class FirmanteService {

    static final String PLANTA_ACTUAL = "Palomar";
    static final String GRUPO_ALTERNATIVAS = "RESPONSABLE_PALOMAR";

    private final FirmanteRepository repository;
    private final AuthenticatedUserGuard authenticatedUserGuard;

    public FirmanteService(
            FirmanteRepository repository,
            AuthenticatedUserGuard authenticatedUserGuard
    ) {
        this.repository = repository;
        this.authenticatedUserGuard = authenticatedUserGuard;
    }

    public List<FirmanteResponse> listar(boolean incluirInactivos) {
        return repository.listar(PLANTA_ACTUAL, incluirInactivos);
    }

    @Transactional
    public FirmanteResponse crear(FirmanteGuardarRequest request) {
        DatosFirmante datos = validar(request);
        validarNombreDisponible(datos.nombre(), null);
        if (datos.obligatorio()
                && repository.contarObligatoriosActivos(PLANTA_ACTUAL, 0) > 0) {
            throw new ConflictoOperacionException(
                    "Palomar ya tiene un firmante obligatorio. Edite ese registro para reemplazarlo."
            );
        }
        return repository.crear(
                datos.nombre(),
                datos.areaLinea1(),
                datos.areaLinea2(),
                PLANTA_ACTUAL,
                datos.obligatorio(),
                datos.grupoSeleccion(),
                datos.orden()
        );
    }

    @Transactional
    public FirmanteResponse actualizar(int id, FirmanteGuardarRequest request) {
        FirmanteResponse actual = obtener(id);
        DatosFirmante datos = validar(request);
        if (datos.obligatorio() != actual.obligatorio()) {
            throw new IllegalArgumentException(
                    "El tipo del firmante no puede cambiarse. Cree un firmante nuevo."
            );
        }
        validarNombreDisponible(datos.nombre(), id);
        if (!repository.actualizar(
                id, datos.nombre(), datos.areaLinea1(), datos.areaLinea2()
        )) {
            throw new RecursoNoEncontradoException("No se encontró el firmante indicado.");
        }
        return obtener(id);
    }

    @Transactional
    public FirmanteResponse cambiarEstado(int id, FirmanteEstadoRequest request) {
        if (request == null || request.activo() == null) {
            throw new IllegalArgumentException("Debe indicar el estado del firmante.");
        }
        FirmanteResponse actual = obtener(id);
        if (!request.activo() && actual.activo() && !actual.obligatorio()) {
            long alternativas = repository.contarAlternativasActivas(
                    actual.planta(), actual.grupoSeleccion(), actual.id()
            );
            if (alternativas == 0) {
                throw new ConflictoOperacionException(
                        "Debe quedar al menos un firmante seleccionable activo."
                );
            }
        }
        if (!request.activo() && actual.activo() && actual.obligatorio()
                && repository.contarObligatoriosActivos(actual.planta(), actual.id()) == 0) {
            throw new ConflictoOperacionException(
                    "Debe quedar al menos un firmante obligatorio activo."
            );
        }
        if (request.activo() && !actual.activo() && actual.obligatorio()
                && repository.contarObligatoriosActivos(actual.planta(), actual.id()) > 0) {
            throw new ConflictoOperacionException(
                    "Ya existe otro firmante obligatorio activo para Palomar."
            );
        }
        if (!repository.actualizarEstado(id, request.activo())) {
            throw new RecursoNoEncontradoException("No se encontró el firmante indicado.");
        }
        return obtener(id);
    }

    public List<FirmaExpedienteResponse> obtenerSeleccion(int incidenteId) {
        validarIncidente(incidenteId);
        return repository.obtenerSeleccion(incidenteId);
    }

    @Transactional
    public List<FirmaExpedienteResponse> seleccionar(
            int incidenteId,
            SeleccionFirmantesRequest request
    ) {
        validarIncidente(incidenteId);
        if (request == null || request.administradorId() == null
                || request.administradorId() <= 0) {
            throw new IllegalArgumentException("Debe indicar el administrador que firma.");
        }
        authenticatedUserGuard.validarUsuarioSolicitado(request.administradorId());

        List<FirmaExpedienteResponse> existente = repository.obtenerSeleccion(incidenteId);
        if (!existente.isEmpty()) {
            return existente;
        }

        Set<Integer> ids = validarIds(request.firmanteIds());
        List<FirmanteResponse> activos = repository.listar(PLANTA_ACTUAL, false);
        Map<Integer, FirmanteResponse> porId = activos.stream()
                .collect(Collectors.toMap(FirmanteResponse::id, firmante -> firmante));

        if (!porId.keySet().containsAll(ids)) {
            throw new IllegalArgumentException(
                    "La selección contiene un firmante inexistente o deshabilitado."
            );
        }

        List<FirmanteResponse> seleccionados = ids.stream()
                .map(porId::get)
                .sorted(Comparator.comparingInt(FirmanteResponse::orden))
                .toList();
        validarReglas(activos, seleccionados);

        repository.guardarSeleccion(
                incidenteId,
                request.administradorId(),
                seleccionados
        );
        return repository.obtenerSeleccion(incidenteId);
    }

    private void validarReglas(
            List<FirmanteResponse> activos,
            List<FirmanteResponse> seleccionados
    ) {
        Set<Integer> seleccionadosIds = seleccionados.stream()
                .map(FirmanteResponse::id)
                .collect(Collectors.toSet());

        for (FirmanteResponse obligatorio : activos.stream()
                .filter(FirmanteResponse::obligatorio)
                .toList()) {
            if (!seleccionadosIds.contains(obligatorio.id())) {
                throw new IllegalArgumentException(
                        "Debe incluir al firmante obligatorio " + obligatorio.nombre() + "."
                );
            }
        }

        Map<String, List<FirmanteResponse>> grupos = activos.stream()
                .filter(firmante -> !firmante.obligatorio())
                .collect(Collectors.groupingBy(FirmanteResponse::grupoSeleccion));
        for (Map.Entry<String, List<FirmanteResponse>> grupo : grupos.entrySet()) {
            long cantidad = grupo.getValue().stream()
                    .filter(firmante -> seleccionadosIds.contains(firmante.id()))
                    .count();
            if (cantidad != 1) {
                throw new IllegalArgumentException(
                        "Debe seleccionar exactamente un firmante alternativo."
                );
            }
        }

        Set<Integer> ordenes = new HashSet<>();
        for (FirmanteResponse seleccionado : seleccionados) {
            if (!ordenes.add(seleccionado.orden())) {
                throw new ConflictoOperacionException(
                        "La configuración de firmantes tiene posiciones duplicadas."
                );
            }
        }
        if (seleccionados.size() != 2) {
            throw new ConflictoOperacionException(
                    "Palomar debe tener un firmante seleccionable y uno obligatorio."
            );
        }
    }

    private Set<Integer> validarIds(List<Integer> valores) {
        if (valores == null || valores.isEmpty()) {
            throw new IllegalArgumentException("Debe seleccionar los firmantes del expediente.");
        }
        Set<Integer> ids = new LinkedHashSet<>();
        for (Integer id : valores) {
            if (id == null || id <= 0) {
                throw new IllegalArgumentException("La selección de firmantes no es válida.");
            }
            if (!ids.add(id)) {
                throw new IllegalArgumentException("No puede seleccionar dos veces al mismo firmante.");
            }
        }
        return ids;
    }

    private FirmanteResponse obtener(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("El identificador del firmante no es válido.");
        }
        return repository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró el firmante indicado."
                ));
    }

    private void validarIncidente(int incidenteId) {
        if (incidenteId <= 0 || !repository.incidenteExiste(incidenteId)) {
            throw new RecursoNoEncontradoException("No se encontró el expediente indicado.");
        }
    }

    private DatosFirmante validar(FirmanteGuardarRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Debe enviar los datos del firmante.");
        }
        String nombre = textoObligatorio(request.nombre(), "nombre", 150);
        String areaLinea1 = textoObligatorio(request.areaLinea1(), "área", 150);
        String areaLinea2 = textoObligatorio(request.areaLinea2(), "ubicación", 150);
        String tipo = textoObligatorio(request.tipo(), "tipo", 20)
                .toUpperCase(Locale.ROOT);
        return switch (tipo) {
            case "OBLIGATORIO" -> new DatosFirmante(
                    nombre, areaLinea1, areaLinea2, true, null, 2
            );
            case "ALTERNATIVA" -> new DatosFirmante(
                    nombre, areaLinea1, areaLinea2, false, GRUPO_ALTERNATIVAS, 1
            );
            default -> throw new IllegalArgumentException(
                    "El tipo debe ser OBLIGATORIO o ALTERNATIVA."
            );
        };
    }

    private String textoObligatorio(String valor, String campo, int maximo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el " + campo + ".");
        }
        String texto = valor.trim();
        if (texto.length() > maximo || texto.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("El " + campo + " no es válido.");
        }
        return texto;
    }

    private void validarNombreDisponible(String nombre, Integer excluirId) {
        if (repository.existeNombre(nombre, PLANTA_ACTUAL, excluirId)) {
            throw new ConflictoOperacionException(
                    "Ya existe un firmante con ese nombre en Palomar."
            );
        }
    }

    private record DatosFirmante(
            String nombre,
            String areaLinea1,
            String areaLinea2,
            boolean obligatorio,
            String grupoSeleccion,
            int orden
    ) {
    }
}
