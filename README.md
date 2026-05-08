<p style="text-align: center;">
  <img src="https://res.cloudinary.com/dywawleqm/image/upload/v1777109979/newlogoplus_a2mzt0.png" alt="Nexo+ Logo" width="400">
</p>

Nexo+ es una plataforma académica integral diseñada para optimizar la comunicación y gestión entre estudiantes, profesores y administradores. Construida con un enfoque moderno y reactivo, la aplicación ofrece un ecosistema fluido para el control de asignaturas, evaluaciones, horarios y materiales didácticos, todo bajo una interfaz elegante con efectos visuales avanzados.

## ⚙️️️ Tecnologías y Arquitectura

El proyecto implementa los estándares más exigentes del desarrollo Android actual:

*   **Lenguaje:** [Kotlin](https://kotlinlang.org/) (100% nativo).
*   **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) con Material Design 3.
*   **Navegación:** [Navigation 3](https://developer.android.com/guide/navigation/navigation-3) con soporte para *backstacks* independientes por pestaña y rutas *type-safe*.
*   **Arquitectura:** Clean Architecture + MVVM (Model-View-ViewModel).
*   **Inyección de Dependencias:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) para una gestión de dependencias modular y testeable.
*   **Servicios en la Nube:**
    *   **[Firebase](https://firebase.google.com/):** Autenticación (Google/Github/Email), Firestore (DB tiempo real), Cloud Messaging (Notificaciones Push).
    *   **[Supabase](https://supabase.com/):** Gestión avanzada de archivos (Storage) para tareas y entregas.
    *   **[Cloudinary](https://cloudinary.com/):** Optimización y almacenamiento dinámico de imágenes de perfil.
*   **Persistencia Local:** DataStore (Preferencias de usuario como temas y notificaciones).
*   **UI Avanzada:** 
    *   **Floating Pills:** Elementos flotantes dinámicos con transparencia (*glassmorphism*) en la navegación.
    *   **Calendario Dinámico:** Integración con [Kizitonwose Calendar](https://github.com/kizitonwose/Calendar) para gestión visual de eventos.
    *   **Coil:** Carga eficiente de imágenes asíncronas.
    *   **AnimatedContent/Visibility:** Transiciones fluidas en toda la aplicación.
    *   **Splash Screen API:** Experiencia de inicio nativa y fluida.

## 📱 Funcionalidades por Rol

### 👨‍🎓 Para el Estudiante
*   **Muro de Asignaturas:** Acceso a unidades didácticas, publicaciones del profesor y tareas académicas.
*   **Gestión de Entregas:** Subida de archivos directamente a la nube con feedback inmediato.
*   **Seguimiento de Calificaciones:** Consulta de notas detalladas con comentarios y archivos de feedback.
*   **Organización Personal:** Calendario interactivo y sistema de **Recordatorios** para tareas y notas propias.
*   **Asistencia:** Consulta del historial de asistencia por asignatura y global.
*   **Horarios:** Visualización de clases sincronizada en tiempo real.

### 👨‍🏫 Para el Profesor
*   **Control de Contenidos:** Creación y edición de unidades, anuncios y materiales de apoyo.
*   **Evaluación 360°:** Calificación de entregas, asignación de notas y envío de feedback personalizado.
*   **Gestión de Tareas:** Publicación de evaluaciones y corrección de entregas con feedback personalizado.
*   **Control de Asistencia:** Sistema digital para pasar lista y registrar ausencias en tiempo real.
*   **Horarios:** Gestión visual de su propia jornada laboral.
*   **Notificaciones:** Envío de avisos directos a los alumnos mediante integración con FCM.

### ️🧑🏻‍💻 Para el Administrador
*   **Control Institucional:** Configuración jerárquica de Centros, Cursos, Ciclos y Asignaturas.
*   **Gestión de Usuarios:** Aprobación de cuentas, edición de perfiles y asignación de roles.
*   **Mantenimiento Avanzado:** 
    *   Semillado masivo de la base de datos mediante archivos JSONL.
    *   Generación atómica de horarios y gestión de calendarios escolares.
    *   Limpieza y optimización de datos del sistema.

## 📂 Estructura del Proyecto (Clean Architecture)

```
app/src/main/java/samf/gestorestudiantil/
├── data/              # Modelos, Repositorios (Impl) y Servicios (Firebase/Supabase)
├── domain/            # Casos de Uso, Interfaces de Repositorios y Mappers
├── ui/
│   ├── components/    # Librería de componentes UI reutilizables
│   ├── dialogs/       # Orquestador de diálogos basado en estados
│   ├── navigation/    # Configuración de Navigation 3 y HomeState
│   ├── panels/        # Interfaces especializadas por rol (Admin/Profe/Estudiante)
│   ├── screens/       # Pantallas de flujo principal (Auth, Home, Perfil)
│   ├── theme/         # Sistema de diseño, colores y tipografía
│   └── viewmodels/    # Lógica de presentación y gestión de estados (StateFlow)
├── di/                # Módulos de Hilt (FirebaseModule, RepositoryModule, NetworkModule)
└── utils/             # Utilidades, ErrorMapper y formateadores
```

## 🛠️ Instalación y Configuración

1.  **Firebase:** Es imprescindible incluir el archivo `google-services.json` en el directorio `app/`.
2.  **FCM (Notificaciones):** El envío de notificaciones administrativas y académicas requiere el archivo `service-account.json` en `app/src/main/assets/` (no incluido por seguridad).
3.  **Supabase:** Configura tus credenciales (URL/Key) en los recursos de strings o mediante variables de entorno.
4.  **Entorno:** Recomendado Android Studio **Ladybug** o superior y dispositivos con **API 34+** para soporte total de notificaciones y temas dinámicos.

---
*Nexo+: Elevando la experiencia académica al siguiente nivel.*
