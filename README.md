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
| `INCIDENTES_DATA_SOURCE` | No | `JDBC` por defecto. Usar `API` para leer la bandeja y el expediente mediante el backend. |
| `INCIDENTES_API_URL` | No | URL del backend. Por defecto: `http://127.0.0.1:8080`. |
| `INCIDENTES_API_TOKEN` | Sí, en modo `API` | Token interno que debe coincidir con `API_INTERNAL_TOKEN` del backend. |

En producción, `INCIDENTES_FILES_PATH` debe apuntar a una ubicación central accesible por los equipos autorizados mediante la red interna o la VPN.

## Compilar y ejecutar

No es necesario instalar Maven: el repositorio incluye Maven Wrapper.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

En Eclipse, después de incorporar archivos nuevos, actualizar el proyecto con `F5` y ejecutar `Project > Clean`.

## API interna en desarrollo

El repositorio incluye un backend independiente en `backend/`. Esta primera etapa no reemplaza todavía el acceso JDBC del cliente JavaFX ni modifica sus pantallas.

Variables utilizadas por el backend:

| Variable | Obligatoria | Descripción |
| --- | --- | --- |
| `DB_USER` | Sí | Usuario de PostgreSQL utilizado sólo por el servidor. |
| `DB_PASSWORD` | Sí | Contraseña de PostgreSQL utilizada sólo por el servidor. |
| `DB_URL` | No | URL JDBC del servidor PostgreSQL. |
| `API_HOST` | No | Interfaz de escucha. Por defecto: `127.0.0.1`. |
| `API_PORT` | No | Puerto HTTP local. Por defecto: `8080`. |
| `API_INTERNAL_TOKEN` | Sí | Token interno exigido por los endpoints protegidos. No debe guardarse en Git. |
| `API_INTERNAL_USER` | No | Identidad técnica asociada al token. Por defecto: `desktop-local`. |
| `API_INTERNAL_ROLE` | No | Rol técnico asociado al token. Por defecto: `ADMIN`. |
| `INCIDENTES_FILES_PATH` | No | Carpeta administrada desde la que el servidor entrega las imágenes. |
| `INCIDENTES_ALLOW_LEGACY_ABSOLUTE_PATHS` | No | Compatibilidad temporal con rutas absolutas antiguas. Por seguridad, el valor predeterminado es `false`. |
| `INCIDENTES_MAX_IMAGES` | No | Cantidad máxima de imágenes por incidente. Por defecto: `10`. |
| `INCIDENTES_MAX_IMAGE_BYTES` | No | Tamaño máximo de cada imagen en bytes. Por defecto: `10485760` (10 MB). |
| `INCIDENTES_MAX_IMAGE_DIMENSION` | No | Máximo permitido para ancho o alto de una imagen. Por defecto: `12000`. |
| `INCIDENTES_MAX_REQUEST_SIZE` | No | Tamaño máximo de la solicitud multipart. Por defecto: `50MB`. |

Compilar y ejecutar la API desde la raíz del repositorio:

```powershell
.\mvnw.cmd -f backend\pom.xml clean verify
.\mvnw.cmd -f backend\pom.xml spring-boot:run
```

Endpoints iniciales:

- `GET http://127.0.0.1:8080/api/health`
- `GET http://127.0.0.1:8080/api/incidentes`
- `GET http://127.0.0.1:8080/api/incidentes?estado=PENDIENTE`
- `GET http://127.0.0.1:8080/api/incidentes?estado=RESUELTO`
- `GET http://127.0.0.1:8080/api/incidentes/{id}`
- `GET http://127.0.0.1:8080/api/incidentes/{id}/boletines`
- `GET http://127.0.0.1:8080/api/incidentes/{id}/imagenes`
- `GET http://127.0.0.1:8080/api/incidentes/{id}/imagenes/{imagenId}/contenido`
- `POST http://127.0.0.1:8080/api/incidentes`

El endpoint de salud es público. Para consultar incidentes se debe enviar el token:

```powershell
$env:API_INTERNAL_TOKEN = "un-token-local-largo-y-aleatorio"
$env:INCIDENTES_API_TOKEN = $env:API_INTERNAL_TOKEN
$env:INCIDENTES_DATA_SOURCE = "API"
```

El token compartido es una protección transitoria para el desarrollo local de la
arquitectura. No reemplaza la autenticación corporativa, no debe incluirse en el
repositorio y no debe utilizarse para exponer la API en Internet. La instalación
empresarial deberá usar HTTPS y Corporate ID/SSO (OIDC), con usuarios y roles
validados por el servidor.

Esta etapa escucha sólo en la computadora local. No se debe cambiar `API_HOST` para exponer el servicio en la red hasta incorporar autenticación y HTTPS.

Para probar la bandeja, la lectura del expediente y el envío del boletín del empleado a través de la API, primero se inicia el backend y luego se ejecuta JavaFX con `INCIDENTES_DATA_SOURCE=API`. El envío utiliza una solicitud multipart que incluye el boletín y todas sus imágenes. El backend valida los campos, el usuario, el contenido real de las imágenes y sus límites antes de completar la operación.

El cliente descarga las imágenes autorizadas a una carpeta temporal de la sesión, sin recibir ni conocer su ruta física en el servidor. La creación de boletines internos, la resolución y las funciones que todavía no fueron migradas continúan usando JDBC durante esta transición.

La creación del incidente y el registro de sus imágenes se ejecutan dentro de una transacción. Si falla la base de datos o el almacenamiento, se revierte la operación y se eliminan los archivos parciales.

Las imágenes administradas deben guardarse dentro de `INCIDENTES_FILES_PATH`. La compatibilidad con rutas absolutas antiguas sólo debe habilitarse de manera temporal durante una migración controlada; no se recomienda para una instalación nueva.

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
