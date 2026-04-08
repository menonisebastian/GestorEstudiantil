# 🎓 Gestor Estudiantil

Gestor Estudiantil es una aplicación móvil nativa para Android diseñada para ayudar a los estudiantes a organizar su vida académica. Permite llevar un control detallado de asignaturas, calificaciones, horarios y recordatorios, además de contar con potentes paneles para profesores y administradores.

## 🚀 Tecnologías y Herramientas

El proyecto está desarrollado con las últimas tecnologías y prácticas de desarrollo en Android:

* **Lenguaje:** Kotlin.
* **Interfaz de Usuario (UI):** Jetpack Compose (Moderno toolkit declarativo para UI nativa).
* **Navegación:** Navigation 3 (Enfoque moderno y type-safe).
* **Arquitectura:** MVVM (Model-View-ViewModel).
* **Servicios en la nube:** Firebase (Firestore, Authentication, Storage).
* **Inyección de Dependencias:** Hilt (en progreso/implementado en componentes).
* **Sistema de compilación:** Gradle (Kotlin DSL).

## 📱 Funcionalidades Principales

### 👨‍🎓 Para el Estudiante
* **Autenticación:** Inicio de sesión y registro de usuarios (`AuthScreen`). Manejo de estados de cuenta pendiente de aprobación.
* **Panel Principal (Home):** Vista general con acceso rápido a las distintas herramientas.
* **Gestión de Asignaturas:** Visualización del contenido compartido por los profesores, unidades y publicaciones.
* **Calificaciones:** Consulta de notas obtenidas y seguimiento del rendimiento académico.
* **Horarios:** Organización de clases y rutinas semanales.
* **Recordatorios:** Creación y visualización de alertas sobre tareas o exámenes con notificaciones.
* **Perfil:** Personalización de la cuenta y ajustes de la aplicación.

### 👨‍🏫 Para el Profesor
* **Gestión de Asignaturas:** Control total sobre las materias asignadas.
* **Planificación Didáctica:** Creación y organización de Unidades y Temas dentro de cada asignatura.
* **Publicaciones (Posts):** Compartir materiales, anuncios y recursos con los estudiantes en tiempo real.
* **Control de Calificaciones:** Registro, edición y visibilidad de evaluaciones para los alumnos.

### 🛠️ Para el Administrador
* **Gestión de Usuarios:** Aprobación de nuevas cuentas, edición de perfiles y control de roles (Admin, Profesor, Estudiante).
* **Estructura Académica:** Gestión jerárquica de Centros, Tipos de Curso, Cursos y Ciclos.
* **Gestión de Asignaturas:** Creación de materias y asignación de profesores responsables.
* **Importación de Datos:** Herramientas para la carga masiva de datos y limpieza de registros desde fuentes externas (JSONL).

## 📂 Estructura del Proyecto

El código fuente principal se encuentra dentro de `app/src/main/java/samf/gestorestudiantil//` y está organizado de la siguiente manera:

* `data/`: Contiene la capa de datos.
    * `models/`: Entidades del dominio (`Asignatura`, `Centro`, `Curso`, `Evaluacion`, `User`, `Post`, `Unidad`, etc.).
    * `enums/`: Enumeraciones para tipificar datos.
* `ui/`: Contiene toda la capa de presentación (Jetpack Compose).
    * `components/`: Componentes UI reutilizables y atómicos.
    * `dialogs/`: Orquestador de diálogos y ventanas emergentes.
    * `navigation/`: Lógica de enrutamiento, mappers de navegación y definición de rutas.
    * `panels/`: Vistas específicas divididas por roles (`admin`, `profesor` y `estudiante`).
    * `screens/`: Pantallas completas y flujos de edición (full-screen Scaffolds).
    * `theme/`: Configuración visual del sistema de diseño (Colores, Tipografía, Formas).
    * `viewmodels/`: Gestores de estado y lógica de negocio mediante `StateFlow`.
* `domain/`: Casos de uso, validadores y mapeadores de UI.

## ⚙️ Instalación y Configuración

1. Clona este repositorio en tu máquina local.
2. Abre el proyecto utilizando **Android Studio**.
3. Asegúrate de tener configurado un emulador o dispositivo físico con Android (API 24+ recomendado).
4. Verifica la configuración de Firebase: el archivo `google-services.json` debe estar presente en el módulo `app`.
5. Haz clic en **Run** (o presiona `Shift + F10`) para compilar y ejecutar la aplicación.
