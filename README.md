# 🎓 Gestor Estudiantil

Gestor Estudiantil es una aplicación móvil nativa para Android diseñada para ayudar a los estudiantes a organizar su vida académica. Permite llevar un control detallado de asignaturas, calificaciones, horarios y recordatorios, además de contar con un panel de administración para la gestión de usuarios.

## 🚀 Tecnologías y Herramientas

El proyecto está desarrollado con las últimas tecnologías y prácticas de desarrollo en Android:

* **Lenguaje:** Kotlin.
* **Interfaz de Usuario (UI):** Jetpack Compose (Moderno toolkit declarativo para UI nativa).
* **Arquitectura:** MVVM (Model-View-ViewModel).
* **Servicios en la nube:** Firebase (Integración mediante `google-services.json`).
* **Sistema de compilación:** Gradle (Kotlin DSL).

## 📱 Funcionalidades Principales

### 👨‍🎓 Para el Estudiante
* **Autenticación:** Inicio de sesión y registro de usuarios (`AuthScreen`). Manejo de estados de cuenta pendiente de aprobación (`PendingApprovalScreen`).
* **Panel Principal (Home):** Vista general con acceso rápido a las distintas herramientas (`EstudianteHomeScreen`).
* **Gestión de Asignaturas:** Visualización y control de las materias cursadas (`AsignaturasEstudiantePanel`).
* **Calificaciones y Evaluaciones:** Registro de notas obtenidas y seguimiento del rendimiento académico por materia (`CalificacionesEstudiantePanel`, `CalificacionesAsignaturaPanel`).
* **Horarios:** Organización de clases y rutinas semanales (`HorariosEstudiantePanel`).
* **Recordatorios:** Creación y visualización de alertas sobre tareas, exámenes o entregas (`RecordatoriosEstudiantePanel`, `AddRecordatorioDialog`).
* **Perfil y Ajustes:** Personalización y configuración de la cuenta (`ProfileScreen`, `SettingsScreen`).

### 🛠️ Para el Administrador
* **Gestión de Usuarios:** Panel exclusivo para administrar las cuentas de los estudiantes que utilizan la aplicación (`UsuariosAdminPanel`).

## 📂 Estructura del Proyecto

El código fuente principal se encuentra dentro de `app/src/main/java/samf/gestorestudiantil/` y está organizado de la siguiente manera:

* `data/`: Contiene la capa de datos.
    * `models/`: Entidades principales del dominio (`Asignatura`, `Centro`, `Curso`, `Evaluacion`, `Horario`, `Profesor`, `Recordatorio`, `User`, `Post`).
    * `enums/`: Enumeraciones para tipificar datos (`tipoEvaluacion`, `tipoRecordatorio`).
* `ui/`: Contiene toda la capa de presentación (Jetpack Compose).
    * `components/`: Componentes UI reutilizables (`CardItem`, `DropDownMenu`, etc.).
    * `dialogs/`: Ventanas emergentes (`ConfirmDialog`, `AddRecordatorioDialog`).
    * `navigation/`: Lógica de enrutamiento y navegación entre pantallas (`AppNavigation`, `Routes`).
    * `panels/`: Vistas específicas divididas por roles (`admin` y `estudiante`).
    * `screens/`: Pantallas completas de la aplicación.
    * `theme/`: Configuración visual, colores, tipografía y formas (`Theme.kt`, `Color.kt`, `Type.kt`).
    * `viewmodels/`: Gestores de estado y lógica de negocio (`AuthViewModel`, `AdminViewModel`).
* `domain/`: Casos de uso y mapeadores (`MappersUI.kt`).

## ⚙️ Instalación y Configuración

1. Clona este repositorio en tu máquina local.
2. Abre el proyecto utilizando **Android Studio**.
3. Asegúrate de tener configurado un emulador o dispositivo físico con Android.
4. (Opcional) Verifica la configuración de Firebase en la consola asegurándote de que el archivo `google-services.json` contenga las credenciales correctas de tu proyecto de Firebase.
5. Haz clic en **Run** (o presiona `Shift + F10`) para compilar y ejecutar la aplicación.