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
*   **Autenticación:** Integración con **Credential Manager** para un flujo seguro y moderno (Google / Github / Email).
*   **Servicios en la Nube:**
    *   **[Firebase](https://firebase.google.com/):** Autenticación, Firestore (DB tiempo real), Cloud Messaging (Notificaciones Push).
    *   **[Supabase](https://supabase.com/):** Gestión avanzada de archivos (Storage) para tareas y entregas.
    *   **[Cloudinary](https://cloudinary.com/):** Optimización y almacenamiento dinámico de imágenes de perfil.
*   **Persistencia Local:** DataStore (Preferencias de usuario como temas y notificaciones).
*   **Testing:** Entorno de pruebas unitarias (MockK, Turbine) y de integración para validación de lógica de negocio y ViewModels.

## 📱 Experiencia de Usuario (UI/UX)

*   **Modal Bottom Sheets:** Paneles inferiores para una mejor usabilidad táctil sin pérdida de contenido.
*   **Selective Stacking:** Sistema inteligente de apilamiento que permite abrir selectores de fecha/hora sobre paneles inferiores sin perder el estado.
*   **Floating Pills:** Elementos flotantes dinámicos con transparencia en la navegación.
*   **Búsqueda Optimizada:** Animaciones fluidas en todos los filtros globales.
*   **Sistema de Deshacer Acciones:** Capacidad de revertir eliminaciones críticas con feedback visual mediante Snackbars con barra de progreso.
*   **Calendario Dinámico:** Integración con [Kizitonwose Calendar](https://github.com/kizitonwose/Calendar) para gestión visual de eventos y tareas.
*   **Splash Screen API:** Experiencia de inicio nativa y fluida.

## 📱 Funcionalidades por Rol

### 👨‍🎓 Para el Estudiante
*   **Próximas Entregas:** Dashboard expandible con las tareas que vencen en los próximos 7 días.
*   **Calificaciones Globales:** Resumen detallado de notas medias y estado académico por asignatura.
*   **Muro de Asignaturas:** Acceso a unidades, publicaciones y tareas con soporte para archivos adjuntos.
*   **Gestión de Entregas:** Subida y descarga de archivos con feedback en tiempo real.
*   **Asistencia:** Historial cronológico de asistencia con filtrado avanzado.
*   **Organización:** Sistema de **Recordatorios** personales con notificaciones locales programadas.

### 👨‍🏫 Para el Profesor
*   **Control de Asistencia:** Marcado rápido (Presente, Ausente, Tarde, Justificado) con guardado automático (*live-save*).
*   **Evaluación 360°:** Calificación de entregas y feedback personalizado con soporte para edición dinámica.
*   **Borrado Lógico:** Sistema de *Soft Delete* para contenidos, permitiendo la recuperación de elementos eliminados accidentalmente.
*   **Gestión de Contenidos:** Creación y edición de unidades didácticas, anuncios y tareas académicas.
*   **Horarios:** Visualización y edición de su jornada laboral sincronizada.

### ️🧑🏻‍💻 Para el Administrador
*   **Cálculo Dinámico:** Estadísticas y contadores de estudiantes procesados en tiempo real sin necesidad de sincronización manual.
*   **Limpieza Profunda:** Herramientas de mantenimiento para el vaciado definitivo de papelera y optimización de almacenamiento (Firestore/Supabase).
*   **Gestión Institucional:** Configuración de Centros, Cursos, Ciclos y Asignaturas.
*   **Control de Usuarios:** Aprobación de cuentas pendientes, edición de perfiles y asignación de roles.
*   **Seeding:** Carga masiva de datos base mediante archivos JSONL.

## 📂 Estructura del Proyecto (Clean Architecture)

```
app/src/main/java/samf/gestorestudiantil/
├── data/              # Implementación de datos y persistencia
│   ├── enums/         # Enumeraciones globales (Roles, Estados)
│   ├── interfaces/    # Interfaces de modelos UI
│   ├── models/        # Modelos de datos (POJOs/Data Classes) para Firebase/Supabase
│   └── repositories/  # Implementaciones concretas de los repositorios
├── domain/            # Lógica de negocio pura (independiente de la UI)
│   ├── notifications/ # Gestión de notificaciones locales y push (FCM)
│   ├── repositories/  # Definición de interfaces de repositorios
│   ├── serializers/   # Serializadores personalizados para Firestore
│   ├── usecases/      # Casos de uso (Lógica de negocio unitaria)
│   └── utils/         # Utilidades, Mappers y validadores
├── ui/                # Capa de presentación (Jetpack Compose)
│   ├── components/    # Componentes atómicos y efectos visuales
│   ├── dialogs/       # Sistema de diálogos reactivos
│   ├── navigation/    # Rutas y grafos (Navigation 3)
│   ├── panels/        # Dashboards por rol (Admin, Profesor, Estudiante)
│   ├── screens/       # Pantallas principales y contenido de navegación
│   ├── theme/         # Diseño de sistema (Material 3)
│   └── viewmodels/    # Gestión de estado (StateFlow) y lógica UI
├── di/                # Inyección de dependencias (Hilt)
└── MainActivity.kt    # Punto de entrada y host de navegación
```

## 🛠️ Instalación y Configuración

1.  **Firebase:** Incluir el archivo `google-services.json` en el directorio `app/`.
2.  **FCM:** El envío de notificaciones requiere el archivo `service-account.json` en `app/src/main/assets/`.
3.  **Supabase:** Configura tus credenciales (URL/Key) en los recursos de strings.
4.  **Entorno:** Recomendado Android Studio **Ladybug** o superior y dispositivos con **API 34-36** para soporte total.

---
*Nexo+: Elevando la experiencia académica al siguiente nivel.*
