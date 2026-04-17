# Changelog

Todos los cambios notables en este proyecto se documentarán en este archivo.

## [v0.6.2] - 2026-04-17
- **UI/UX**: Estandarización global del patrón de "Cabezal Flotante" (Floating Header) en todos los paneles de la aplicación (Estudiante, Profesor y Administrador).
- **UI/UX**: Mejora de la profundidad visual mediante el uso de tarjetas elevadas con transparencia (alpha 0.95f) y elevación de 8.dp para un efecto de superposición.
- **Admin**: Refactorización de los paneles de Usuarios y Centros, integrando `SecondaryTabRow` con contadores de elementos y cabezales unificados.
- **Profesor**: Actualización estética de la gestión de calificaciones y horarios, incluyendo sub-vistas de alumnos y detalle de notas con el nuevo diseño de tarjetas elevadas.
- **Componentes**: Rediseño de `CustomSearchBar` para mejorar el contraste visual y la sensación de profundidad ("hole effect") sobre superficies elevadas.

## [v0.6.1] - 2026-04-16
- **UI/UX**: Estandarización de la capitalización de turnos en toda la aplicación (Perfil, Registro, Filtros, Diálogos) para mejorar la legibilidad.
- **Admin**: Mejora en `HorariosAdminScreen` para mostrar el identificador único de la clase (`claseId`) en el encabezado basándose en el modelo `Clase.kt`.
- **Admin**: Implementación de filtros avanzados por Curso, Turno y Ciclo en el diálogo de asignación de asignaturas para profesores.
- **Admin**: Optimización de la gestión de usuarios: la actualización de contadores y miembros de grupos (`Clase`) ahora está centralizada en el repositorio.
- **UI/UX**: Incrementada la elevación tonal y de sombra en la `BottomNavBar` para un efecto de profundidad más pronunciado y estético.

## [v0.6.0] - 2026-04-16
- **Notificaciones**: Implementado sistema de alertas administrativas mediante FCM (HTTP v1). Los administradores ahora reciben notificaciones en tiempo real cuando un nuevo usuario se registra en su centro.
- **Modelo de Datos**: Centralización de la lista de departamentos en `User.Profesor.DEPARTAMENTOS` para asegurar la integridad de datos en todos los flujos de la aplicación.
- **Registro**: Integrada la selección de departamento obligatoria para profesores en los flujos de registro estándar y Google Sign-In.
- **UI/UX**: Refactorización mayor de `HomeScreen` utilizando `HorizontalPager` para la navegación entre pestañas, mejorando la fluidez y permitiendo transiciones gestuales.
- **UI/UX**: Implementación de lógica de FAB (Floating Action Button) contextual en la Home, adaptando su comportamiento y visibilidad según la pestaña activa y el estado de navegación.
- **Seguridad**: Configurada la autenticación OAuth2 para FCM utilizando `service-account.json` desde los assets, garantizando el envío seguro de mensajes a través de la API v1.

## [v0.5.9] - 2026-04-16
- **Modelo de Datos**: Introducción de la entidad `Clase.kt` para representar grupos específicos de alumnos. Incluye campos para `tutorId`, `estudiantesIds` y `asignaturasIds`, permitiendo una gestión granular de la relación entre cursos globales y grupos reales.
- **Admin**: Implementación de la generación masiva de clases mediante `WriteBatch` de Firestore, automatizando la creación de documentos en la colección `clases` según la configuración de cursos y turnos.
- **Admin**: Inclusión de un botón de acción masiva en el panel de centros para poblar la base de datos de forma eficiente (hasta 500 operaciones por lote).
- **Corrección**: Sincronización de callbacks de navegación en `HomeScreen` para evitar errores de compilación tras la actualización del panel administrativo.
- **Admin**: Implementación de la asignación de tutores a grupos (clases) directamente desde el panel de gestión de centros.
- **Arquitectura**: Integración del patrón `DialogState` y `DialogOrchestrator` para la gestión centralizada de los diálogos de asignación de tutor.
- **Modelo de Datos**: Evolución de la entidad `Clase.kt` incorporando campos para `estudiantesIds`, `asignaturasIds` y un sistema de ID normalizado (**ACRONIMO + TURNO + CICLO**) para mejorar la indexación y trazabilidad.
- **Repositorio**: Ampliación de `AdminRepository` para gestionar la persistencia de tutores asignados a grupos específicos.

## [v0.5.8] - 2026-04-16
- **Admin**: Refactorización de `CentrosAdminPanel.kt` para mejorar la UX de gestión de ciclos.
- **Admin**: Implementación de cabecera de tutor flotante con acceso directo a horarios en `CiclosScreen`.
- **Admin**: Mejora en la visualización de perfiles de profesores: ahora se permite abrir el perfil del tutor incluso si no tiene una cuenta vinculada (usando fallback por nombre).
- **UI**: Limpieza visual en paneles administrativos eliminando el botón de retroceso redundante en `AdminHeader` (gestión delegada al TopAppBar).
- **UI**: Implementación de efectos de profundidad y transparencia en tarjetas flotantes para mejorar la jerarquía visual durante el scroll.

## [v0.5.7] - 2026-04-16
- **Arquitectura**: Refactorización de `MainActivity` para delegar la lógica de negocio y configuración de SDKs a componentes especializados.
- **Refactorización**: Movida la inicialización del SDK de Cloudinary a `GestorEstudiantilApp` para asegurar una instancia única global.
- **Notificaciones**: Creado `NotificationHandlers.kt` con componentes `FcmTokenManager` y `NotificationPermissionGate` para desacoplar la gestión de tokens y permisos de la Activity principal.
- **Notificaciones**: Mejorada la lógica de permisos de notificación con soporte para "Rationale" y feedback visual mediante Toasts.
- **Limpieza**: Eliminación de dependencias técnicas de Firebase y Manifest en `MainActivity`, transformándola en un contenedor puramente declarativo de UI y navegación.

## [v0.5.6] - 2026-04-15
- **Navegación**: Refactorización mayor del sistema de navegación hacia un modelo de **Estado Hoisteado** (`HomeState`).
- **Navegación**: Implementación de backstacks independientes por pestaña en `HomeScreen`, preservando el estado al cambiar entre secciones.
- **Navegación**: Transición a un flujo de autenticación **declarativo** en `AppNavigation.kt`, donde la pantalla raíz se deriva automáticamente del estado del `AuthViewModel`.
- **UI/UX**: Mejoradas las transiciones de pantalla con animaciones combinadas de deslizamiento y desvanecimiento (`slideIn` + `fadeIn`).
- **UI/UX**: Optimizada la transparencia de la `BottomNavBar` permitiendo que el contenido fluya por debajo, manteniendo el efecto de desenfoque (*glassmorphism*).
- **Refactorización**: Eliminación de lógica de navegación imperativa y rutas redundantes para mejorar la consistencia y escalabilidad.

## [v0.5.5] - 2026-04-15
- **UI/UX**: Implementación de `AnimatedVisibility` con efectos de fundido (`fadeIn`/`fadeOut`) para los indicadores de carga en `AuthScreen` y `RegisterStep2Screen`.
- **UI/UX**: Añadida transición animada en la `TopAppBar` de la `HomeScreen` mediante `AnimatedContent`, permitiendo un cambio fluido entre el logo y el título de perfil.
- **UI/UX**: Implementado indicador de carga (`CircularProgressIndicator`) con transición suave durante el proceso de cierre de sesión.
- **Refactorización**: Centralización del cierre de sesión en `AuthViewModel` y `AuthRepository`, permitiendo una gestión de estado más reactiva y visual durante el *logout*.

## [v0.5.4] - 2026-04-15
- **Autenticación con GitHub**: Se ha implementado la logica de login con GitHub.
- **UI/UX**: Realizados varios cambios visuales en la interfaz de usuario.
- **Refactorización de Arquitectura**: Se ha movido la lógica del Listener de eventos en `AppNavigation` a la capa de domain + ViewModel.
- **Corrección**: Los departamentos no se asignaban correctamente a los usuarios.
- **Corrección**: Login con GitHub ahora funciona correctamente.

## [v0.5.3] - 2026-04-14
- **Refactorización de Arquitectura**: Migración completa del modelo `User` a una `sealed class` con jerarquía de tipos (`Estudiante`, `Profesor`, `Admin`, `Incompleto`). Esta estructura permite el uso de *smart casting* y *exhaustive when* en toda la aplicación, eliminando errores de casting manual y simplificando la lógica basada en roles.
- **Modelo de Datos**: Evolución del perfil `User.Profesor` con la incorporación del campo `turno` (matutino/vespertino) y sustitución del campo genérico `cursoOArea` por `departamento` para una clasificación docente más precisa.
- **Sincronización en Cascada**: Implementada actualización atómica mediante `WriteBatch` en `AdminRepository`. Ahora, al asignar o desasignar un profesor a una asignatura, el cambio se propaga instantáneamente a todos los slots de horario relacionados, asegurando que los alumnos vean la información actualizada en tiempo real.
- **Robustez de Horarios**: Mejorada la función `guardarHorario` para heredar automáticamente los datos del docente desde la asignatura, evitando discrepancias de datos en la creación de nuevos horarios.
- **UI/UX**: Actualizado el panel de horarios del estudiante para resaltar el nombre del profesor con negrita y color negro, mejorando la legibilidad sobre fondos de color.
- **Administración**: Adaptadas `EditUserScreen` y `EditUserDialog` para permitir la edición del turno y departamento en perfiles de profesor.
- **Perfil**: Añadida la visualización del turno asignado en `ProfileScreen` para el rol de profesor.

## [v0.5.2] - 2026-04-14
- **Branding**: Actualización completa de la identidad visual de la aplicación, incluyendo nuevos logotipos y recursos gráficos.
- **Autenticación**: Preparación de la infraestructura para el inicio de sesión con GitHub; implementado el botón de acceso en la pantalla de autenticación (funcionalidad en desarrollo).
- **Refactorización**: Migración de la lógica de negocio de `ProfileScreen` hacia el patrón Repository/ViewModel. Las actualizaciones de perfil ahora se gestionan a través de `SettingsViewModel` y `UserRepository`, mejorando la mantenibilidad y separación de conceptos.
- **Repositorio**: Añadidas funciones de actualización de nombre e imagen de perfil en `UserRepositoryImpl` con soporte para persistencia en Firebase Firestore.

## [v0.5.1] - 2026-04-14
- **Calificaciones**: Unificación del sistema de evaluación. Ahora las notas incluyen feedback detallado (comentarios) y archivos adjuntos devueltos por el profesor.
- **Estudiantes**: Implementado el diálogo `VerDetalleEvaluacion` que permite consultar el feedback del profesor y descargar archivos adjuntos directamente desde la tarjeta de calificación.
- **Profesorado**: Optimización del flujo de calificación; el guardado ahora actualiza simultáneamente la entrega específica y el historial global de evaluaciones del alumno.
- **UI/UX**: `EvaluacionCard` mejorada con indicadores visuales de adjuntos (clip), previsualización de comentarios y respuesta táctil (clicable).
- **Correcciones**: Integrada la visualización de la imagen del alumno en el panel de entregas del profesor y resueltos errores de nulabilidad en URLs de adjuntos.

## [v0.5.0] - 2026-04-13
- **Navegación**: Refactorización del manejo del backstack en `HomeScreen` para minimizar la aplicación en lugar de cerrarla al estar en la pestaña inicial, evitando cierres de sesión accidentales.
- **Administración**: Integración de filtros por "Turno" (Matutino/Vespertino) en el panel de gestión de usuarios.
- **UI/UX**: Actualización de `FilterByDialog` con selectores de turno para mejorar la precisión en la búsqueda de usuarios.

## [v0.4.9] - 2026-04-13
- **Refactorización de FloatingPill**: Transformado el componente `FloatingPill` en un menú desplegable dinámico que actúa como disparador de acciones.
- **Mejora de UI en UnidadCard**: Integradas las acciones de "Editar Unidad" y "Eliminar Unidad" dentro del menú `FloatingPill`, eliminando botones redundantes de la tarjeta para un diseño más limpio.
- **Estilización de Menús**: Las opciones del menú ahora utilizan un fondo `surfaceColor` sin bordes, mejorando la integración visual con el tema de la aplicación.
- **Consistencia de Acciones**: Implementado el uso de `MenuItem` para estandarizar iconos, colores y comportamientos (incluyendo acciones destructivas) en los menús de acción.

## [v0.4.8] - 2026-04-13
- **Filtros de Seguridad**: Implementado filtrado estricto por Turno y Ciclo en el `ProfesorRepository`. Ahora los profesores solo ven a los alumnos que corresponden exactamente a su grupo, evitando la mezcla de estudiantes de diferentes turnos o años.
- **Sincronización de Horarios**: Corregida la desincronización de datos entre la asignación de materias y los horarios. Al asignar un profesor desde el panel de administración, el cambio se refleja automáticamente en todos los bloques horarios correspondientes.
- **Desacoplamiento de Estado**: Refactorizado el `AdminViewModel` para separar la lista de asignaturas del curso de la lista de asignaturas disponibles para asignación, eliminando el error que causaba que la vista principal se "resetease" al asignar un docente.
- **Estandarización de Títulos**: Actualizado el formato de títulos en los paneles de calificaciones y horarios a "Acrónimo Curso + Turno + Ciclo" (ej. "DAMV2") para una identificación rápida y consistente.
- **Mejora de UX en Calificaciones**: Ajustada la `AsignaturaCard` para profesores; ahora muestra el curso al que pertenece la materia en lugar del nombre del propio profesor, facilitando la gestión multiclase.

## [v0.4.7] - 2026-04-13
- **Estandarización de UI**: Unificados los componentes de selección (dropdowns) mediante el uso de `CustomOptionsTextField` en toda la aplicación para asegurar la integridad de los datos.
- **Mejora de UX**: Implementado estilo "pill" con `RoundedCornerShape(20.dp)` en campos de selección, restringiendo la entrada a opciones válidas y mejorando la estética visual.
- **Administración**: Actualizadas las pantallas `EditUserScreen` y `EditAsignaturaScreen` para usar selectores estandarizados en los campos de "Ciclo" y "Número de Ciclo".
- **Consistencia**: Verificada y asegurada la uniformidad de los componentes de selección en los flujos de Registro, Configuración de Google y Diálogos comunes.
- **Refactorización**: Implementado el componente reutilizable `FloatingPill` para menús de opciones, aplicado inicialmente en `UnidadCard`.

## [v0.4.6] - 2026-04-13
- **Mejora de UI**: Rediseñados los botones de acción en las pantallas de edición de Administración como "píldoras flotantes".
- **UI/UX**: Implementado `RoundedCornerShape(24.dp)` y elevación en los contenedores de botones para una estética más moderna y coherente.
- **Corrección de Diseño**: Ajustado el posicionamiento de los botones de guardado/cancelado para elevarlos sobre la `BottomNavBar` flotante, evitando solapamientos.
- **Estandarización**: Aplicado el nuevo estilo de botones y espaciado de formulario de 180dp en todas las pantallas de edición (Centros, Cursos, Asignaturas, Usuarios).

## [v0.4.5] - 2026-04-13
- **Característica**: Integración del ítem de "Perfil" en la barra de navegación inferior para todos los roles (ADMIN, PROFESOR, ESTUDIANTE).
- **Mejora de UI**: El icono de la pestaña Perfil ahora muestra dinámicamente la imagen del usuario (`AsyncImage`) con un tamaño optimizado de 24dp.
- **Navegación**: Implementada navegación por pestañas independiente para el perfil mediante el `HorizontalPager` en `HomeScreen`.
- **UX**: Ocultación automática de la `TopAppBar` global al navegar a la pestaña de Perfil para evitar duplicidad de elementos y maximizar el espacio.
- **Refinamiento de UI**: Eliminado el botón de retroceso redundante en `ProfileScreen` al integrarse como pestaña principal de la Home.
- **Corrección de Diseño**: Ajustados los paddings del `Scaffold` en `HomeScreen` para permitir que la pantalla de perfil ocupe todo el alto disponible sin espacios en blanco.

## [v0.4.4] - 2026-04-12
- **Característica**: Implementación de efecto "Liquid Glass" en la barra de navegación inferior (`BottomNavBar`) mediante la integración de la librería Haze (v1.7.2).
- **Mejora de UI**: Configuración de desenfoque de fondo dinámico (`hazeEffect`) con estilos de materiales Cupertino y bordes definidos para una estética premium.
- **Estabilidad**: Corregido bucle infinito de redirección en el inicio de sesión con Google al detectar sesiones existentes con perfil incompleto.
- **Refactorización**: Limpieza de advertencias de deprecación en ViewModels de Hilt y optimización de backstacks internos en `HomeScreen` y `AuthScreen`.
- **UI**: Estandarización de badges de identificación de curso ("Acrónimo + Turno + Ciclo") en las tarjetas de materia de los profesores para una rápida identificación.
- **Corrección**: Resuelta colisión de contextos `@Composable` en la configuración de efectos visuales de Haze.
- **Característica**: Implementación de badges y notificaciones para las tareas/entregas.
- **Corrección**: Arreglados los colores en el string de los horarios.

## [v0.4.3] - 2026-04-12
- **Característica**: Implementación de apertura directa de archivos adjuntos (PDF, DOCX, imágenes) mediante aplicaciones del sistema.
- **Mejora de UX**: Los archivos ahora se descargan temporalmente en `cacheDir` y se comparten de forma segura usando `FileProvider`, evitando que el usuario deba buscarlos manualmente en la carpeta de descargas.
- **Infraestructura**: Configurado `androidx.core.content.FileProvider` en `AndroidManifest.xml` y `file_paths.xml` para la gestión segura de URIs internos.
- **Refactorización**: Migrada la lógica de descarga de `DownloadManager` (navegador/notificación externa) a un flujo integrado con `Intent.ACTION_VIEW` y selectores de aplicaciones del sistema.
- **Repositorio**: Añadido soporte para descarga directa de binarios (`ByteArray`) desde Supabase Storage en `TareaRepository`.

## [v0.4.2] - 2026-04-12
- **Característica**: Sistema de Snackbar Global centralizado en `AppViewModel` con soporte para acciones de "Deshacer" (Undo) mediante `SharedFlow`.
- **Característica**: Implementación de flujo de borrado seguro con confirmación y "Deshacer" para todas las entidades de Administración (Centros, Cursos, Asignaturas, Horarios).
- **Característica**: Integración del sistema de "Deshacer" en el panel de Profesor para la eliminación de Unidades y Posts.
- **Navegación**: Refactorización de backstacks en `HomeScreen` a `NavKey` (Navigation 3) para una gestión de estado más robusta por pestaña.
- **Navegación**: Bloqueo automático del scroll lateral en el `HorizontalPager` principal al navegar a rutas de detalle o edición.
- **Mejora de UI**: FAB contextual dinámico en `HomeScreen` que ajusta su icono y acción según la profundidad de navegación en el panel de Administración.
- **Refactorización**: Actualización de `DialogState` y diálogos de Horarios para incluir soporte nativo para la acción de eliminar.
- **Corrección de errores**: Corregidos errores de coincidencia de tipos (ID vs Objeto) en las funciones de eliminación del panel de detalle de materia.
- **Limpieza**: Eliminados parámetros obsoletos en casos de uso de notificaciones detectados durante la refactorización.

## [v0.4.1] - 2026-04-12
- **Refinamiento de UI/UX**: Unificada la paleta de colores global (primario, secundario, terciario) en todos los componentes y paneles.
- **Mejora de UI**: Personalizados los selectores de fecha (`CustomDatePickerDialog`) y hora (`CustomTimePickerDialog`) con la estética de la marca.
- **Estandarización de UI**: Refactorizados `CustomTextField`, `CustomSearchBar` y `BottomNavBar` para usar estados visuales consistentes (foco, selección, inactividad).
- **UI/UX**: Mejorada la distinción visual en diálogos de administración (`AsignarAsignaturas`, `AsignarProfesor`) mediante el uso de colores secundarios y terciarios.
- **Consistencia de UI**: Reemplazados múltiples colores "hardcoded" por las variables de tema (`errorColor`, `textColor`, etc.) para asegurar compatibilidad total con modos claro/oscuro.
- **UI/UX**: Refinadas las tarjetas de usuario y contenido (`UsuarioCardAdmin`, `UnidadCard`, `TareaCard`) eliminando bordes innecesarios para un diseño más limpio y moderno.

## [v0.4.0] - 2026-04-12
- **Corrección de errores**: Resuelto error `mime type application/octet-stream is not supported` en Supabase Storage al subir adjuntos.
- **Corrección**: Implementada detección manual de MIME type y nombre de archivo original mediante `ContentResolver` y `OpenableColumns`.
- **Mejora**: Añadido soporte para `contentType` en `TareaRepository`, asegurando que Supabase identifique correctamente los archivos (PDF, DOCX, etc.) con fallback a `OctetStream` en caso de error.
- **UI/UX**: Agregados mensajes de feedback (`Toast`) en el hilo principal para TODAS las acciones críticas (Unidades, Posts, Tareas, Entregas, Calificaciones, Marcado de lectura).
- **UI/UX**: Los adjuntos en `TareaCard` y diálogos ahora son interactivos; permiten abrir el archivo directamente mediante `Intent.ACTION_VIEW`.
- **UI/UX**: Implementada visualización del tamaño de archivo (KB/MB) en los selectores de archivos para mayor claridad del usuario.
- **Corrección**: Resuelto crash en `DialogOrchestrator` al intentar abrir el detalle de tarea desde el perfil de estudiante (error de casting de `DialogState`).
- **Refactorización**: Actualizados los diálogos y paneles de estudiante para propagar correctamente el contexto del alumno (`id`, `nombre`) a las entregas.

## [v0.3.9] - 2026-04-12
- **Refactorización**: Rediseñado el diálogo de creación de tareas (`AddTareaDialog`) separando los campos de fecha y hora en una disposición vertical para mejorar la usabilidad.
- **UI**: Implementados selectores nativos de fecha (`DatePicker`) y hora (`TimePicker`) con corrección de desfase por zona horaria (UTC a Local).
- **Infraestructura**: Corregida la asignación de metadatos críticos (`profesorId`, `centroId`) al crear nuevas tareas desde el panel del profesor.
- **Mejora**: Añadido manejo de errores y cierre seguro de flujos en la selección de archivos adjuntos.
- **Estado**: **PENDIENTE** - La creación de tareas en Firestore aún no es funcional debido a errores de conectividad de red persistentes (`firestore.googleapis.com`) y validaciones de formato de fecha pendientes.

## [v0.3.8] - 2026-04-11
- **Característica**: Añadido un diálogo de "Editar Perfil" en `ProfileScreen` que permite a los usuarios actualizar su información básica (Nombre) directamente.
- **Refactorización**: Eliminado `SettingsScreen` y consolidadas todas las preferencias de usuario y gestión de perfil en `ProfileScreen`.
- **Mejora de UI**: Limpiados `TopBarRow` y `DropDownMenu` eliminando enlaces redundantes a "Configuración".
- **Gestión de diálogos**: Introducido el estado `EditSelfProfile` en `DialogOrchestrator` para actualizaciones de perfil de autoservicio seguras.
- **Infraestructura**: Integradas actualizaciones en tiempo real de Firestore para cambios de perfil con sincronización inmediata de la UI en toda la aplicación.

## [v0.3.7] - 2026-04-11
- **Característica**: Añadido un selector de tema (Claro, Oscuro, Automático) en la pantalla de Perfil para una experiencia visual personalizada.
- **Característica**: Implementado un interruptor local para habilitar/deshabilitar notificaciones push en el perfil de usuario.
- **Infraestructura**: Migradas las configuraciones de usuario (tema y notificaciones) a `DataStore` para una persistencia local eficiente, reduciendo las operaciones de escritura en Firestore.
- **Refactorización**: Eliminado `notificationsEnabled` del modelo `User` para limpiar el esquema de la base de datos.
- **Mejora**: Actualizado `MyFirebaseMessagingService` para respetar la preferencia de notificación local antes de mostrar alertas.

## [v0.3.6] - 2026-04-11
- **Característica**: Implementado completamente el CRUD de Recordatorios en todas las capas (Firestore, Repositorio, ViewModel y UI).
- **Mejora de UI**: Mejorada `CustomNotificationCard` con un adorno visual de "clip" y estandarizado el formato de fecha usando `formatearFechaParaMostrar`.
- **Interacción de UI**: Integrado el ciclo de vida completo de edición/eliminación para recordatorios en `RecordatoriosEstudiantePanel` y `RecordatoriosProfesorPanel`.
- **Gestión de diálogos**: Añadido `EditRecordatorioDialog` a `DialogOrchestrator` para una edición consistente basada en estados.
- **Infraestructura**: Implementado `actualizarRecordatorio` en `RecordatorioRepository` con actualizaciones atómicas respaldadas por Firestore.
- **Infraestructura**: Migrada la lógica de notificaciones push de Cloud Functions a la integración directa de la API de Firebase para mejorar la eficiencia.
- **Refactorización**: Conectadas las acciones de recordatorio de `AppViewModel` a los callbacks de `HomeScreen` para asegurar actualizaciones reactivas de la UI en los diferentes roles de usuario.

## [v0.3.5] - 2026-04-11
- **Característica**: Implementado sistema de notificaciones push usando Firebase Cloud Messaging y Cloud Functions.
- **Navegación**: Soporte de "Deep Linking" interno para abrir detalles de materias desde las notificaciones.
- **Infraestructura**: Suscripción dinámica a temas de Firebase basada en la matriculación del estudiante.
- **UI**: Implementado el manejo de intents en MainActivity y sincronización con el backstack de Navigation 3.

## [v0.3.4] - 2026-04-11
- **Característica**: Implementado el flujo de recuperación de contraseña de Firebase.
- **Mejora de UI**: Añadidos `ForgotPasswordPanel` y `ForgotPasswordSuccessPanel` con navegación automatizada y feedback de estado de éxito.
- **Navegación**: Integrada la recuperación de contraseña en el backstack interno de `AuthScreen` usando Navigation 3, asegurando un retorno limpio al Login.
- **Infraestructura**: Ampliados `AuthRepository` y `AuthViewModel` para soportar el envío de correos de restablecimiento de contraseña.

## [v0.3.3] - 2026-04-11
- **Mejora de UI**: Reemplazados los filtros desplegables por un sistema de `FilterChip` multiseleccionable en `FilterByDialog` para una experiencia más moderna y táctil.
- **Mejora de UI**: Mejorada la selección de turno en `CentrosAdminPanel` con iconos contextuales (Sol para Mañana, Atardecer para Tarde).
- **Mejora de UI**: Estandarizados los encabezados de horarios en el panel de Administración para seguir el formato "Acrónimo + Turno + Ciclo" (ej., "DAM M2").
- **Mejora de UI**: Actualizado el encabezado de `MateriaDetalleEstudiantePanel` con una fila de perfil de profesor interactiva que reemplaza el texto estático.
- **Corrección de errores**: Resueltos cierres por deserialización de Firestore en `Asignatura` (colisión de nombres @DocumentId) y `User` (campos faltantes).
- **Corrección de errores**: Corregida la discrepancia de tipo de datos para `cicloNum` al generar nuevas entradas de horario en `HorariosAdminScreen`.
- **Integridad de datos**: Sincronizados los campos `imgUrl` y `fotoUrl` en los casos de uso de registro y actualizaciones de perfil para asegurar la consistencia del avatar.
- **Infraestructura**: Ampliados `ProfesorRepository` y `ProfesorViewModel` para soportar la obtención de perfiles de profesores para las vistas de detalle del estudiante.

## [v0.3.2] - 2026-04-11
- **Mejoras de UI de Administración**: Iconos estandarizados en `CentrosAdminPanel` para niveles de FP (Especialización: `AutoAwesome`, Superior: `School`, Medio: `MenuBook`, Básico: `ImportContacts`).
- **Mejoras de UI de Administración**: Refactorizada la selección de Curso en la edición de usuario (Diálogo y Pantalla) de texto manual a un desplegable de acrónimos basado en la base de datos (ej., "DAM", "DAW").
- **Mejoras de UI de Administración**: Sincronizados `cursoId` y `cursoOArea` en la edición de usuario; al seleccionar un curso ahora se actualizan automáticamente tanto el ID interno como la etiqueta visual (ej., "DAMV1").
- **Estandarización de componentes**: Unificado el estilo visual de `CustomOptionsTextField` con `CustomTextField` (color de superficie, esquinas de 16dp, sin borde) y corregidos el recorte/colores del menú desplegable.
- **Integridad de datos**: Añadida la carga reactiva de datos de cursos (`cargarCursosPorCentro`) al entrar en las pantallas de gestión o edición de usuarios para asegurar la disponibilidad del desplegable.
- **Corrección de errores**: Corregida la implementación de `DialogState.EditUser` en `HomeScreen.kt` y `UsuariosAdminPanel.kt` para pasar la `List<Curso>` completa en lugar de solo acrónimos.

## [v0.3.1] - 2026-04-11
- **Estandarización de UI**: Reemplazadas todas las instancias de `OutlinedTextField` por un `CustomTextField` unificado con un diseño sin bordes, esquinas redondeadas de 16dp y fondo `surfaceColor`.
- **Gestión de Administración**: Implementado `EditUserDialog` para una edición integral de usuarios, con campos condicionales basados en el rol (Turno, Ciclo, Curso/Área).
- **Gestión de Administración**: Invertido el orden de las pestañas en `UsuariosAdminPanel` para mostrar "Activos" como la pestaña principal.
- **Cuadrícula visual**: Refactorizados los paneles de Materias de `LazyRow` horizontal a una `LazyVerticalGrid` de 3 columnas para mejorar la accesibilidad y la densidad de información.
- **Tipografía y Diseño**: Mejorada la legibilidad de `CardItem` con mejor contraste de texto, alineación centrada y manejo de puntos suspensivos para nombres de materias largos.
- **Refactorización de componentes**: Actualizados `CustomSearchBar` y `CustomPasswordTextField` (migrados a `TextFieldState`) para mantener la consistencia visual y funcional en toda la aplicación.

## [v0.3.0] - 2026-04-11
- **Persistencia de navegación**: Implementado `tabBackStacks` (mapa de `SnapshotStateList`) en `HomeScreen` para mantener estados de navegación independientes para cada pestaña de la barra inferior.
- **Control de desplazamiento**: Deshabilitado el deslizamiento de `HorizontalPager` en rutas de detalle (`isDetailRoute`) para evitar cambios de pestaña accidentales durante la navegación anidada.
- **Gestión centralizada de UI**: Unificada la lógica del Botón de Acción Flotante (FAB) y la gestión del backstack dentro del Scaffold principal de `HomeScreen` para un comportamiento consistente en todos los niveles de Administración.
- **Consistencia de UI**: Estandarizados todos los iconos de navegación a `Icons.AutoMirrored.Filled.ArrowBackIos` e `Icons.AutoMirrored.Filled.ArrowForwardIos` en toda la aplicación.
- **Mejoras visuales**: Actualizada `CursoCard` para que sea totalmente basada en el modelo, usando `iconoName`, `colorFondoHex` y `colorIconoHex` del objeto de datos `Curso`.
- **Refinamiento de UX**: Añadido un relleno de contenido inferior de `80.dp` a todas las listas `LazyColumn` administrativas y de calificación para evitar que el FAB oculte elementos.
- **Corrección de errores**: Corregidos los títulos de encabezado redundantes en `CursosScreen` (ej., "Cursos de Curso de...") comprobando condicionalmente si el nombre del tipo ya contiene la palabra "Curso".

## [v0.2.9] - 2026-04-10
- **Arquitectura de navegación**: Centralizadas todas las subrutas de Administración (Centros, Tipos, Cursos, Asignaturas, Usuarios) en `HomeScreen` para resolver cierres por `IllegalStateException` y "Pantalla desconocida" causados por componentes `NavDisplay` anidados.
- **Arquitectura de navegación**: Actualizados los `NestedNavMappers` para identificar correctamente las rutas de detalle dentro de la jerarquía de Administración ampliada para soportar la prioridad del gesto de retroceso.
- **Gestión de estado**: Definidos `AdminViewModel` y `ProfesorViewModel` a nivel de `HomeScreen` para asegurar la persistencia del estado y los datos compartidos en los niveles de subnavegación.
- **Refactorización**: Desacoplado `CentrosAdminPanel` en componentes de pantalla independientes y reutilizables (`CentrosListScreen`, `TiposCursoScreen`, `CursosScreen`, etc.).
- **Mejora**: Internalizado el estado de búsqueda y de UI dentro de `EstudiantesAsignaturaLista` (Calificaciones) para una mejor encapsulación y una lógica de padre simplificada.
- **Corrección de errores**: Corregidos problemas de importación de iconos y errores de inferencia de tipos en el árbol de navegación de Administración.

## [v0.2.8] - 2026-04-10
- **Corrección de errores**: Corregido el problema de que la insignia de notificación del estudiante no aparecía en los posts nuevos o editados.
- **Característica**: Añadida `fechaActualizacion` a los Posts para rastrear ediciones y activar notificaciones.
- **Mejora**: El recuento de notificaciones ahora solo considera los posts visibles.
- **Infraestructura**: Actualizados `EstudianteRepository` y `ProfesorRepository` para manejar las marcas de tiempo de actualización de los posts.

## [v0.2.7] - 2026-04-10
- **Arquitectura**: Completada la refactorización total a Clean Architecture + MVVM.
- **Arquitectura**: Implementado el Patrón Repositorio para todas las fuentes de datos (Auth, User, Course, Admin, Estudiante, Profesor, Recordatorio).
- **Arquitectura**: Introducida la capa de Dominio con Casos de Uso para la lógica de negocio compleja (Registro, Semillado de base de datos, Notificaciones, etc.).
- **Inyección de dependencias**: Migrados todos los ViewModels a Hilt (`@HiltViewModel`).
- **Inyección de dependencias**: Creados `FirebaseModule` y `RepositoryModule` para la gestión de proveedores/enlaces de Hilt.
- **Mejora**: Los ViewModels ahora usan `StateFlow` para la gestión del estado y están desacoplados de los SDK de Firebase.
- **Mejora**: Eliminado el manejo manual de `ListenerRegistration` en los ViewModels mediante el uso de `callbackFlow` en los Repositorios.
- **Refactorización**: Estandarizada la inyección de ViewModels de Hilt usando `hiltViewModel()` en todas las pantallas, eliminando el paso manual de ViewModels en `AppNavigation`.
- **Corrección de errores**: Corregida la discrepancia de parámetros y el nombre de los campos en el flujo de registro de Google Setup.
- **Corrección**: Resueltos errores de compilación relacionados con AGP 9.1 y las configuraciones de sourceSet de Kotlin.

## [v0.2.6] - 2026-04-10
- **Característica**: Integrado Hilt para la Inyección de Dependencias.
- **Infraestructura**: Configurado KSP (Kotlin Symbol Processing) para la generación de código de Hilt.
- **Infraestructura**: Añadidos `GestorEstudiantilApp` y `@AndroidEntryPoint` en `MainActivity`.
- **Infraestructura**: Añadido `hilt-navigation-compose` para la integración de ViewModels en Compose.

## [v0.2.5] - 2026-04-10
- **Característica**: El horario ahora se basa completamente en datos de Firestore.
- **Característica**: Sincronización del horario entre las vistas de estudiante y profesor.
- **Característica**: Implementado `HorariosProfesorPanel` para los horarios de los profesores.
- **Característica**: Añadidos nombres completos de asignaturas y códigos de grupo (ej., "DAM V1") a las vistas de horario para ambos roles.
- **Mejora**: Estandarizado "turno" a minúsculas en todo el sistema (Auth, Admin, Estudiante) para resolver problemas de sensibilidad a mayúsculas en Firestore.
- **Mejora**: Mejorada la sincronización del horario en `HomeScreen` para cargar datos de forma reactiva cuando se actualizan los campos del perfil de usuario (turno, cicloNum).
- **Mejora**: Refactorizado `HorariosEstudiantePanel` con mejores estados de carga, estados vacíos y una coincidencia de franjas horarias más flexible.
- **Corrección de errores**: Corregido un problema crítico donde los horarios no se mostraban debido a discrepancias en las mayúsculas y perfiles de usuario incompletos durante la carga inicial de datos.
- **Mejora**: Logotipo y marca actualizados.

## [v0.2.4] - 2026-04-08
- **Característica**: Unificados "Centros" y "Asignación" en un único flujo administrativo.
- **Mejora**: Convertidos todos los diálogos de edición administrativa (Centros, Cursos, Asignaturas, Usuarios) en vistas Scaffold de pantalla completa.
- **Mejora**: Rediseñadas las pantallas de edición con barras inferiores fijas para acciones primarias (Guardar/Cancelar).
- **Mejora**: Rutas y mappers de Navigation 3 actualizados para soportar la edición de pantalla completa de tipo seguro.
- **Mejora**: Extraídos los componentes de selección de UI (ColorPicker, IconPicker) a una librería compartida.
- **Mejora**: Logotipo y marca actualizados.
- **Mejora**: Rediseñada la UI para las pantallas de Autenticación.
- **Cambio**: Eliminado el archivo obsoleto `AdminEditDialogs.kt`.

## [v0.2.3] - 2026-04-07
- **Característica**: Implementada la capacidad de añadir nuevos Cursos/Asignaturas.
- **Característica**: Implementada la capacidad de crear Unidades/Posts dentro de las Asignaturas.
- **Característica**: Implementada la capacidad de calificar a los estudiantes.
- **Característica**: Implementada la capacidad de añadir Recordatorios.
- **Corrección de errores**: Corregida la matriculación de estudiantes.
- **Cambio**: Los IDs de Cursos/Asignaturas ahora son deterministas.
- **Cambio**: cursoOArea ahora es una concatenación del curso + turno + ciclo.

## [v0.2.2] - 2026-04-07
- **Corrección de errores**: Corregidos los datos brutos de la DB y .jsonl.
- **Cambio**: Modificados AdminViewModel y Navegación.
- **Característica**: Chips de filtro para la SearchBar.
- **Característica**: Creación de paneles de Profesor.
- **Característica**: Implementada la capacidad de modificar los datos de la DB desde el AdminPanel, incluyendo iconos y colores de Cursos/Asignaturas.
- **Cambio**: Cursos/Asignaturas ahora tienen sus propios colores.

## [v0.2.1] - 2026-03-15
- **Mejora**: Reestructurada la NavigationBar para que sea una navegación anidada con backstacks.

## [v0.2.0] - 2026-03-15
- **Corrección**: Corregido un error en el inicio de sesión de Google OAuth cuando el rol del usuario era Estudiante.
- **Mejora**: Reestructurados NavDisplay y Rutas.

## [v0.1.9] - 2026-03-09
- **Mejora**: Añadido un nuevo proveedor de datos para IES Comercio.
- **Mejora**: Añadido CentrosAdminPanel.

## [v0.1.8] - 2026-03-09
- **Mejora**: Los diálogos ahora se gestionan mediante DialogState y DialogOrchestrator.
- **Mejora**: `CustomOptionsTextField` ahora usa la librería ExposedDropdownMenu.

## [v0.1.7] - 2026-03-08
- **Mejora**: NavHost ahora es NavDisplay usando Navigation 3.

## [v0.1.6] - 2026-03-04
- **Mejora**: Implementado diálogo de filtro para la barra de búsqueda.

## [v0.1.5] - 2026-03-04
- **Corrección de errores**: Corregido el inicio de sesión de Google OAuth.
- **Mejora**: Encapsulado el inicio de sesión de Google OAuth.
- **Mejora**: La pantalla de Google ahora también solicita datos del centro escolar.
- **Cambio**: Actualizadas las dependencias de terceros.
- **Cambio**: Las pantallas de formulario ahora son ConstraintLayout.

## [v0.1.3] - 2026-03-01
- **Característica**: Añadida autenticación de usuarios.
- **Corrección de errores**: Corregidos problemas de diseño en el dashboard.

## [v0.1.2] - 2026-02-15
- **Mejora**: Mejorado el rendimiento de la obtención de datos.
- **Cambio**: Actualizadas las dependencias de terceros.

## [v0.1.1] - 2026-02-01
- **Característica**: Implementado un nuevo diseño de UI para el perfil de usuario.

## [v0.0.1] - 2026-01-01
- Lanzamiento inicial con características principales.
