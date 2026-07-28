# Sistema de Gestión de Incidentes

Aplicación de escritorio interna para registrar, investigar y cerrar incidentes. Reemplaza el envío por correo del boletín de ocurrencia y conserva en un mismo expediente el boletín original, los boletines internos y las imágenes adjuntas.

## Funcionalidades principales

- Formulario de boletín de ocurrencia para empleados.
- Bandeja de incidentes pendientes con responsable visible.
- Asignación atómica de casos y sección personal "Mis casos".
- Expediente con boletín original y boletines internos.
- Resolución y búsqueda histórica de casos.
- Exportación del expediente completo a PDF.
- Visualización y descarga de imágenes adjuntas.
- Reportes por período, estado, prioridad y área o sector.
- Gestión de administradores, estados de acceso y carga de trabajo.

## Arquitectura actual

La interfaz continúa siendo una aplicación de escritorio JavaFX. El acceso a los datos y al almacenamiento se realiza exclusivamente mediante el backend:

```text
JavaFX -> HTTP/API -> PostgreSQL
                  -> almacenamiento central de imágenes
```

JavaFX no contiene el driver de PostgreSQL, no abre conexiones JDBC y no necesita conocer las credenciales de la base de datos. Esta separación permite desplegar el backend en un servidor interno sin modificar las vistas ni el diseño de los boletines.

## Tecnologías

### Aplicación de escritorio

- Java 21
- JavaFX 21
- Maven
- OpenPDF
- Jackson

### Backend

- Java 21
- Spring Boot 3.5
- Spring Security
- PostgreSQL
- JDBC administrado por Spring

## Configuración de JavaFX

La aplicación de escritorio utiliza estas variables de entorno:

| Variable | Obligatoria | Descripción |
| --- | --- | --- |
| `INCIDENTES_API_URL` | No | URL del backend. En desarrollo usa `http://127.0.0.1:8080`; en producción debe usar `https://`. |
| `INCIDENTES_API_TOKEN` | Sólo desarrollo | Token interno local. Debe coincidir con `API_INTERNAL_TOKEN`. |
| `INCIDENTES_ACCESS_TOKEN` | Entorno corporativo | Access token OIDC obtenido mediante Corporate ID. Tiene prioridad sobre el token local y sólo se transmite por HTTPS. |

`DB_USER`, `DB_PASSWORD`, `DB_URL`, `INCIDENTES_FILES_PATH` e `INCIDENTES_DATA_SOURCE` no se utilizan en JavaFX.

## Configuración del backend

| Variable | Obligatoria | Descripción |
| --- | --- | --- |
| `DB_USER` | Sí | Usuario de PostgreSQL utilizado únicamente por el backend. |
| `DB_PASSWORD` | Sí | Contraseña de PostgreSQL utilizada únicamente por el backend. |
| `DB_URL` | No | URL JDBC. Por defecto: `jdbc:postgresql://localhost:5432/sistema_incidentes`. |
| `API_HOST` | No | Interfaz de escucha. En desarrollo usa `127.0.0.1`. |
| `API_PORT` | No | Puerto HTTP. Por defecto: `8080`. |
| `API_INTERNAL_TOKEN` | Sí | Token interno exigido por los endpoints protegidos. No debe guardarse en Git. |
| `API_INTERNAL_USER` | No | Identidad técnica asociada al token. |
| `API_INTERNAL_ROLE` | No | Rol técnico asociado al token: `EMPLOYEE` o `ADMIN`. |
| `INCIDENTES_FILES_PATH` | No | Carpeta administrada por el backend para las imágenes. |
| `INCIDENTES_ALLOW_LEGACY_ABSOLUTE_PATHS` | No | Compatibilidad temporal con rutas absolutas antiguas. El valor predeterminado es `false`. |
| `INCIDENTES_MAX_IMAGES` | No | Máximo de imágenes por incidente. Por defecto: `10`. |
| `INCIDENTES_MAX_IMAGE_BYTES` | No | Tamaño máximo de cada imagen. Por defecto: 10 MB. |
| `INCIDENTES_MAX_IMAGE_DIMENSION` | No | Máximo permitido para ancho o alto. Por defecto: `12000`. |
| `INCIDENTES_MAX_REQUEST_SIZE` | No | Tamaño máximo de una solicitud multipart. Por defecto: `50MB`. |

### Perfil corporativo: HTTPS y OIDC

El perfil `corporate` deshabilita la API key local, valida access tokens JWT contra el proveedor corporativo y obtiene el usuario desde el token firmado. Requiere:

| Variable | Descripción |
| --- | --- |
| `SPRING_PROFILES_ACTIVE=corporate` | Activa la seguridad corporativa. |
| `OIDC_ISSUER_URI` | URL oficial del emisor OIDC. |
| `OIDC_AUDIENCE` | Identificador/audience registrado para esta API. |
| `OIDC_USERNAME_CLAIM` | Claim que contiene la cuenta corporativa. Predeterminado: `preferred_username`. |
| `OIDC_ROLES_CLAIM` | Claim que contiene los permisos. Predeterminado: `roles`. |
| `OIDC_ADMIN_ROLE` | Rol corporativo que se traduce a administrador. |
| `OIDC_EMPLOYEE_ROLE` | Rol corporativo que se traduce a empleado. |
| `TLS_KEYSTORE_PATH` | Ruta al certificado de servidor PKCS#12. |
| `TLS_KEYSTORE_PASSWORD` | Contraseña del almacén, mantenida fuera de Git. |
| `TLS_KEY_ALIAS` | Alias del certificado. Predeterminado: `incidentes-api`. |

El certificado debe ser emitido por una CA confiable para las computadoras de la empresa. El cliente no desactiva la validación TLS.

## Compilar y ejecutar

El repositorio incluye Maven Wrapper, por lo que no es necesario instalar Maven.

### Backend

```powershell
.\mvnw.cmd -f backend\pom.xml clean verify
.\mvnw.cmd -f backend\pom.xml spring-boot:run
```

### JavaFX

Con el backend iniciado y las variables de la API configuradas:

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd javafx:run
```

En Eclipse, después de incorporar archivos nuevos, actualizar el proyecto con `F5` y ejecutar `Project > Clean`.

## API interna

Principales recursos disponibles:

- `GET /api/health`
- `GET /api/usuarios/actual`
- `GET /api/usuarios`
- `POST /api/usuarios`
- `PUT /api/usuarios/{id}`
- `PATCH /api/usuarios/{id}/estado`
- `GET /api/incidentes`
- `POST /api/incidentes`
- `GET /api/incidentes/{id}`
- `GET /api/incidentes/{id}/boletines`
- `POST /api/incidentes/{id}/boletines`
- `PUT /api/incidentes/{id}/boletines/{boletinId}`
- `POST /api/incidentes/{id}/asignacion`
- `DELETE /api/incidentes/{id}/asignacion`
- `GET /api/incidentes/{id}/imagenes`
- `GET /api/incidentes/{id}/imagenes/{imagenId}/contenido`
- `PATCH /api/incidentes/{id}/resolucion`
- `GET /api/reportes`

El envío de un boletín utiliza una solicitud multipart que incluye los datos y todas las imágenes. El backend valida los campos, el usuario, el contenido real de los archivos y sus límites antes de completar la operación.

Las imágenes consultadas se descargan a una caché temporal de la sesión. El cliente nunca recibe ni conoce su ruta física dentro del servidor.

## Base de datos

Las migraciones SQL versionadas se encuentran en `database/migrations`. Deben aplicarse de manera controlada y conservarse en el historial del repositorio. Para disponer de todas las funciones actuales se deben aplicar, en orden, `V5__gestion_usuarios.sql`, `V6__asignacion_incidentes.sql` y `V7__firmantes_expediente.sql`.

### Firmas del expediente

La migración V7 crea el catálogo configurable de firmantes de Palomar y conserva una copia histórica de la selección realizada para cada expediente. Al exportar por primera vez:

- López Carlos Argentino se incorpora obligatoriamente.
- El administrador selecciona exactamente uno entre los firmantes alternativos activos.
- La selección queda guardada y se reutiliza en exportaciones posteriores.
- El bloque identificatorio se agrega al final del expediente, antes de la página de cierre.

Los firmantes se administran desde la opción **Firmantes** de la vista de administración. Deshabilitar o editar un firmante no modifica las firmas ya guardadas en expedientes anteriores.

## Seguridad

- Las credenciales y los tokens se mantienen fuera de Git.
- Sólo el backend tiene acceso a PostgreSQL y al almacenamiento administrado.
- El backend valida permisos para las operaciones administrativas.
- Los binarios y archivos de configuración local están ignorados.

El token compartido queda limitado al perfil local de desarrollo. El perfil `corporate` exige HTTPS y Corporate ID/SSO mediante OIDC, toma la identidad desde el token firmado e ignora cualquier nombre de usuario enviado como parámetro por el cliente.

La obtención interactiva del access token en JavaFX (inicio de sesión mediante navegador y PKCE) requiere que el equipo de Identidad entregue el issuer, client ID, audience, claims y URI de redirección autorizada. Hasta contar con esos datos, el cliente ya admite un access token mediante `INCIDENTES_ACCESS_TOKEN`, pero no inventa ni almacena credenciales corporativas.
