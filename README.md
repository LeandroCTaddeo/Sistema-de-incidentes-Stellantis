# Sistema de Gestión de Incidentes

Aplicación de escritorio interna para registrar, investigar y cerrar incidentes. Reemplaza el envío por correo del boletín de ocurrencia y conserva en un mismo expediente el boletín original, los boletines internos y las imágenes adjuntas.

## Funcionalidades principales

- Formulario de boletín de ocurrencia para empleados.
- Bandeja de incidentes pendientes para administradores.
- Expediente con boletín original y boletines internos.
- Resolución y búsqueda histórica de casos.
- Exportación del expediente completo a PDF.
- Visualización y descarga de imágenes adjuntas.
- Reportes por período, estado, prioridad y área o sector.

## Tecnologías

- Java 21
- JavaFX 21
- Maven Wrapper
- PostgreSQL
- OpenPDF

## Configuración local

La aplicación lee su configuración mediante variables de entorno. No se deben guardar credenciales dentro del repositorio.

| Variable | Obligatoria | Descripción |
| --- | --- | --- |
| `DB_USER` | Sí | Usuario de PostgreSQL. |
| `DB_PASSWORD` | Sí | Contraseña de PostgreSQL. |
| `DB_URL` | No | URL JDBC. Por defecto: `jdbc:postgresql://localhost:5432/sistema_incidentes`. |
| `INCIDENTES_FILES_PATH` | No | Carpeta administrada para las imágenes. En desarrollo usa `%USERPROFILE%\SistemaIncidentes\imagenes`. |

En producción, `INCIDENTES_FILES_PATH` debe apuntar a una ubicación central accesible por los equipos autorizados mediante la red interna o la VPN.

## Compilar y ejecutar

No es necesario instalar Maven: el repositorio incluye Maven Wrapper.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

En Eclipse, después de incorporar archivos nuevos, actualizar el proyecto con `F5` y ejecutar `Project > Clean`.

## Arquitectura actual

Durante la etapa de desarrollo:

```text
JavaFX -> JDBC -> PostgreSQL
       -> almacenamiento configurable de imágenes
```

Para una instalación empresarial definitiva se prevé mover el acceso a datos y archivos a un backend desplegado en un servidor interno:

```text
JavaFX -> HTTPS/API -> PostgreSQL
                   -> almacenamiento central de imágenes
```

La migración futura no requiere reemplazar las vistas JavaFX ni modificar el diseño de los boletines.

## Seguridad

- Las credenciales se mantienen fuera de Git.
- Los binarios generados y los archivos de configuración local están ignorados.
- En una puesta en producción, los roles y permisos deberán validarse también en el servidor.
