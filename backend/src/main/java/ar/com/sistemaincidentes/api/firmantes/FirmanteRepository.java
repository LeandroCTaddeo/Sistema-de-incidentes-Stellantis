package ar.com.sistemaincidentes.api.firmantes;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class FirmanteRepository {

    private static final RowMapper<FirmanteResponse> MAPEADOR_FIRMANTE =
            (rs, fila) -> new FirmanteResponse(
                    rs.getInt("id"),
                    rs.getString("nombre"),
                    rs.getString("area_linea_1"),
                    rs.getString("area_linea_2"),
                    rs.getString("planta"),
                    rs.getBoolean("obligatorio"),
                    rs.getString("grupo_seleccion"),
                    rs.getInt("orden"),
                    rs.getBoolean("activo")
            );

    private static final RowMapper<FirmaExpedienteResponse> MAPEADOR_FIRMA =
            (rs, fila) -> new FirmaExpedienteResponse(
                    rs.getInt("firmante_id"),
                    rs.getInt("orden"),
                    rs.getString("nombre"),
                    rs.getString("area_linea_1"),
                    rs.getString("area_linea_2"),
                    rs.getString("planta"),
                    rs.getTimestamp("fecha_seleccion").toLocalDateTime()
            );

    private final JdbcTemplate jdbcTemplate;

    public FirmanteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<FirmanteResponse> listar(String planta, boolean incluirInactivos) {
        return jdbcTemplate.query(
                """
                SELECT id, nombre, area_linea_1, area_linea_2, planta,
                       obligatorio, grupo_seleccion, orden, activo
                FROM firmantes
                WHERE LOWER(planta) = LOWER(?)
                  AND (? = TRUE OR activo = TRUE)
                ORDER BY activo DESC, orden ASC, obligatorio DESC, nombre ASC, id ASC
                """,
                MAPEADOR_FIRMANTE,
                planta,
                incluirInactivos
        );
    }

    public Optional<FirmanteResponse> buscarPorId(int id) {
        return jdbcTemplate.query(
                """
                SELECT id, nombre, area_linea_1, area_linea_2, planta,
                       obligatorio, grupo_seleccion, orden, activo
                FROM firmantes
                WHERE id = ?
                """,
                MAPEADOR_FIRMANTE,
                id
        ).stream().findFirst();
    }

    public FirmanteResponse crear(
            String nombre,
            String areaLinea1,
            String areaLinea2,
            String planta,
            boolean obligatorio,
            String grupoSeleccion,
            int orden
    ) {
        Integer id = jdbcTemplate.queryForObject(
                """
                INSERT INTO firmantes (
                    nombre, area_linea_1, area_linea_2, planta,
                    obligatorio, grupo_seleccion, orden, activo
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)
                RETURNING id
                """,
                Integer.class,
                nombre,
                areaLinea1,
                areaLinea2,
                planta,
                obligatorio,
                grupoSeleccion,
                orden
        );
        return buscarPorId(id == null ? 0 : id)
                .orElseThrow(() -> new IllegalStateException(
                        "No se pudo recuperar el firmante creado."
                ));
    }

    public boolean actualizar(
            int id,
            String nombre,
            String areaLinea1,
            String areaLinea2
    ) {
        return jdbcTemplate.update(
                """
                UPDATE firmantes
                SET nombre = ?, area_linea_1 = ?, area_linea_2 = ?
                WHERE id = ?
                """,
                nombre,
                areaLinea1,
                areaLinea2,
                id
        ) == 1;
    }

    public boolean actualizarEstado(int id, boolean activo) {
        return jdbcTemplate.update(
                "UPDATE firmantes SET activo = ? WHERE id = ?",
                activo,
                id
        ) == 1;
    }

    public boolean existeNombre(String nombre, String planta, Integer excluirId) {
        Long cantidad = excluirId == null
                ? jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*) FROM firmantes
                        WHERE LOWER(nombre) = LOWER(?) AND LOWER(planta) = LOWER(?)
                        """,
                        Long.class,
                        nombre,
                        planta
                )
                : jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*) FROM firmantes
                        WHERE LOWER(nombre) = LOWER(?) AND LOWER(planta) = LOWER(?)
                          AND id <> ?
                        """,
                        Long.class,
                        nombre,
                        planta,
                        excluirId
                );
        return cantidad != null && cantidad > 0;
    }

    public long contarAlternativasActivas(String planta, String grupo, int excluirId) {
        Long cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM firmantes
                WHERE LOWER(planta) = LOWER(?)
                  AND grupo_seleccion = ?
                  AND activo = TRUE
                  AND id <> ?
                """,
                Long.class,
                planta,
                grupo,
                excluirId
        );
        return cantidad == null ? 0 : cantidad;
    }

    public long contarObligatoriosActivos(String planta, int excluirId) {
        Long cantidad = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM firmantes
                WHERE LOWER(planta) = LOWER(?)
                  AND obligatorio = TRUE
                  AND activo = TRUE
                  AND id <> ?
                """,
                Long.class,
                planta,
                excluirId
        );
        return cantidad == null ? 0 : cantidad;
    }

    public boolean incidenteExiste(int incidenteId) {
        Long cantidad = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM incidentes WHERE id = ?",
                Long.class,
                incidenteId
        );
        return cantidad != null && cantidad > 0;
    }

    public List<FirmaExpedienteResponse> obtenerSeleccion(int incidenteId) {
        return jdbcTemplate.query(
                """
                SELECT firmante_id, orden, nombre, area_linea_1, area_linea_2,
                       planta, fecha_seleccion
                FROM expediente_firmantes
                WHERE incidente_id = ?
                ORDER BY orden ASC
                """,
                MAPEADOR_FIRMA,
                incidenteId
        );
    }

    public void guardarSeleccion(
            int incidenteId,
            int administradorId,
            List<FirmanteResponse> firmantes
    ) {
        for (FirmanteResponse firmante : firmantes) {
            jdbcTemplate.update(
                    """
                    INSERT INTO expediente_firmantes (
                        incidente_id, orden, firmante_id, nombre,
                        area_linea_1, area_linea_2, planta, seleccionado_por,
                        fecha_seleccion
                    )
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    incidenteId,
                    firmante.orden(),
                    firmante.id(),
                    firmante.nombre(),
                    firmante.areaLinea1(),
                    firmante.areaLinea2(),
                    firmante.planta(),
                    administradorId,
                    Timestamp.valueOf(java.time.LocalDateTime.now())
            );
        }
    }
}
