# 🎓 Nexo+

Nexo+ es una aplicación móvil nativa para Android diseñada para transformar la gestión académica y la comunicación entre estudiantes, profesores y administradores. Esta plataforma integral permite un control detallado de la vida académica, facilitando la organización de asignaturas, calificaciones, horarios y recordatorios en un entorno moderno y eficiente.

## 🚀 Tecnologías y Herramientas

El proyecto implementa las tecnologías más avanzadas del ecosistema Android:

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/) (100% nativo).
*   **Interfaz de Usuario (UI):** [Jetpack Compose](https://developer.android.com/jetpack/compose) con Material Design 3.
*   **Arquitectura:** MVVM (Model-View-ViewModel) con Clean Architecture.
*   **Inyección de Dependencias:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android).
*   **Navegación:** [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) (Enfoque moderno, dinámico y type-safe).
*   **Servicios en la Nube:**
    *   **[Firebase](https://firebase.google.com/):** Authentication, Firestore (Base de datos NoSQL), Cloud Messaging (Notificaciones Push).
    *   **[Supabase](https://supabase.com/):** Postgrest y Storage para gestión avanzada de datos y archivos.
    *   **[Cloudinary](https://cloudinary.com/):** Optimización y almacenamiento de imágenes.
*   **Gestión de Datos:** DataStore (Preferencias) y Kotlinx Serialization.
*   **UI Avanzada:** Haze (Efectos de desenfoque/glassmorphism), Coil (Carga de imágenes), ColorPicker.
*   **Autenticación:** Google Sign-In y Credential Manager API.

## 📱 Funcionalidades Principales

### 👨‍🎓 Para el Estudiante
*   **Centro de Control:** Dashboard con acceso rápido a las herramientas principales.
*   **Gestión Académica:** Visualización de asignaturas, unidades temáticas y material de apoyo.
*   **Seguimiento de Rendimiento:** Consulta detallada de calificaciones y evaluaciones en tiempo real.
*   **Organización Personal:** Horarios dinámicos y sistema de recordatorios con notificaciones push.
*   **Perfil Personalizado:** Gestión de cuenta y preferencias visuales.

### 👨‍🏫 Para el Profesor
*   **Gestión de Contenidos:** Creación y organización de unidades didácticas y temas.
*   **Comunicación Directa:** Publicación de anuncios, recursos y materiales en el muro de las asignaturas.
*   **Control Evaluativo:** Registro y edición de calificaciones, con visibilidad inmediata para los alumnos.
*   **Notificaciones:** Envío de avisos importantes mediante FCM.

### 🛠️ Para el Administrador
*   **Gestión de Usuarios:** Sistema de aprobación de cuentas, edición de perfiles y asignación de roles (Admin, Profesor, Estudiante).
*   **Estructura Institucional:** Configuración jerárquica de Centros, Cursos, Ciclos y Asignaturas.
*   **Asignación Docente:** Vinculación de profesores a sus respectivas materias.
*   **Herramientas de Datos:** Carga masiva de información mediante archivos JSONL y limpieza de registros.

### ⚙️ Funcionalidades Base (Transversales)
*   **Autenticación Multimodal:** Inicio de sesión y registro mediante Google Auth o credenciales tradicionales con soporte para Credential Manager.
*   **Recuperación de Acceso:** Sistema de restablecimiento de contraseña mediante correo electrónico.
*   **Gestión de Perfil:** Personalización de datos de usuario, incluyendo foto de perfil almacenada en la nube.
*   **Seguridad:** Control de acceso basado en estados de cuenta (Pendiente, Aprobado, Rechazado).
*   **Notificaciones:** Recepción de avisos importantes y recordatorios en tiempo real.
*   **Experiencia de Usuario:** Interfaz adaptativa con soporte para temas, animaciones fluidas y efectos visuales modernos.

## 📂 Estructura del Proyecto

El código fuente sigue una organización modular basada en Clean Architecture:

### 1. Capa de Datos (`data/`)
*   **`models/`**: POJOs y Data Classes que representan las entidades del sistema (`User`, `Curso`, `Tarea`, `Asignatura`, `Evaluacion`, etc.).
*   **`repositories/`**: Implementaciones concretas de los repositorios (`AuthRepositoryImpl`, `UserRepositoryImpl`, `AdminRepositoryImpl`) que gestionan el flujo de datos desde Firebase y Supabase.
*   **`interfaces/`**: Definición de contratos para las fuentes de datos.
*   **`enums/`**: Enumeraciones para roles de usuario, estados de cuenta y tipos de evaluación.

### 2. Capa de Dominio (`domain/`)
*   **`usecases/`**: Lógica de negocio pura e independiente de la UI (`RegisterUserUseCase`, `SeedDatabaseUseCase`, `AssignSubjectToProfessorUseCase`).
*   **`repositories/`**: Interfaces de los repositorios (Definición del contrato).
*   **`serializers/`**: Lógica para la serialización de datos complejos.
*   **`MappersUI.kt`**: Conversores de modelos de datos a modelos específicos para la vista.

### 3. Capa de Presentación (`ui/`)
*   **`viewmodels/`**: Mediadores entre la capa de dominio y la UI, manteniendo el estado de forma reactiva.
*   **`navigation/`**: Configuración de Navigation 3, incluyendo `AppNavigation` y mappers de rutas.
*   **`panels/`**: Interfaces divididas por rol:
    *   `admin/`: Gestión de centros, ciclos, profesores y aprobación de usuarios.
    *   `profesor/`: Gestión de contenidos, tareas y calificaciones.
    *   `estudiante/`: Visualización de cursos, progreso y recordatorios.
*   **`screens/`**: Pantallas de flujo principal (Login, Registro, Perfil, Home).
*   **`components/`**: Librería de componentes personalizados (Cards, Buttons, TextFields) bajo Material 3.
*   **`dialogs/`**: Orquestador de diálogos para feedback y entrada de datos.
*   **`theme/`**: Definición de la identidad visual (Color, Type, Shape, Haze configuration).

### 4. Inyección de Dependencias (`di/`)
*   Módulos de Hilt para la provisión de instancias únicas de Clientes de Firebase, Supabase, Storage y Repositorios.

## ⚙️ Instalación y Configuración

1.  Clona este repositorio.
2.  Abre el proyecto en **Android Studio (Ladybug o superior)**.
3.  Configura Firebase:
    *   Añade tu archivo `google-services.json` en la carpeta `app/`.
    *   **Archivo de Credenciales (FCM):** Para el envío de notificaciones desde el panel de profesor, es necesario incluir el archivo `service-account.json` (obtenido desde la Consola de Firebase -> Cuentas de servicio) en la ruta `app/src/main/resources/`. *Nota: Este archivo contiene claves privadas y no debe subirse al repositorio.*
    *   Habilita Google Sign-In en la consola de Firebase.
4.  Configura Supabase:
    *   Asegúrate de configurar las keys en `res/values/strings.xml` (o vía BuildConfig).
5.  Sincroniza con Gradle y ejecuta en un dispositivo con **API 34+**.
