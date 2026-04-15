package samf.gestorestudiantil.ui.dialogs

import samf.gestorestudiantil.data.enums.tipoRecordatorio
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.data.models.Tarea

sealed class DialogState {
    data object None : DialogState()

    // 1. Diálogo de Confirmación (Usado en AdminPanel)
    data class Confirmation(
        val title: String,
        val content: String,
        val onConfirm: () -> Unit
    ) : DialogState()

    // 2. Diálogos de Recordatorios (Usado en HomeScreen y RecordatoriosPanel)
    data class AddRecordatorio(
        val onSave: (String, String, String, String, tipoRecordatorio) -> Unit
    ) : DialogState()

    data class EditRecordatorio(
        val recordatorio: samf.gestorestudiantil.data.models.Recordatorio,
        val onSave: (samf.gestorestudiantil.data.models.Recordatorio) -> Unit
    ) : DialogState()

    // 3. Diálogo de Filtros (Usado en Recordatorios y Admin)
    data class Filter(
        val tipo: String, // "Usuario", "Asignatura", "Recordatorio", etc.
        val currentFilters: Map<String, String> = emptyMap(),
        val opcionesPersonalizadas: Map<String, List<String>> = emptyMap(), // Opciones dinámicas
        val onApply: (Map<String, String>) -> Unit // Retorna el mapa de filtros actualizados
    ) : DialogState()

    // 4. Diálogo de Perfil de Usuario
    data class UserProfile(
        val user: User
    ) : DialogState()

    data class EditSelfProfile(
        val user: User,
        val onSave: (String) -> Unit // Solo nombre por ahora, se puede expandir
    ) : DialogState()

    // 5. Diálogos de Edición/Creación para Admin
    data class EditCentro(
        val centro: Centro? = null,
        val onSave: (Centro) -> Unit,
        val onDelete: ((Centro) -> Unit)? = null
    ) : DialogState()

    data class EditCurso(
        val curso: Curso? = null,
        val centroId: String,
        val onSave: (Curso) -> Unit,
        val onDelete: ((Curso) -> Unit)? = null
    ) : DialogState()

    data class EditAsignatura(
        val asignatura: Asignatura? = null,
        val cursoId: String,
        val centroId: String,
        val onSave: (Asignatura) -> Unit,
        val onDelete: ((Asignatura) -> Unit)? = null
    ) : DialogState()

    data class EditUser(
        val user: User,
        val cursos: List<Curso> = emptyList(),
        val onSave: (User) -> Unit
    ) : DialogState()

    data class AsignarAsignaturas(
        val profesor: User,
        val onAssign: (String) -> Unit, // id de la asignatura
        val onUnassign: (String) -> Unit // id de la asignatura
    ) : DialogState()

    data class AsignarProfesor(
        val asignatura: Asignatura
    ) : DialogState()

    // 6. Diálogos para Asignaturas (Profesores)
    data class AddUnidad(
        val asignaturaId: String,
        val unidadId: String? = null, // null si es nueva, id si es editar
        val nombreInicial: String = "",
        val descripcionInicial: String = "",
        val visibleInicial: Boolean = false,
        val onSave: (String, String, Boolean) -> Unit // nombre, descripcion, visible
    ) : DialogState()

    data class AddPost(
        val asignaturaId: String,
        val unidadId: String,
        val postId: String? = null, // null si es nuevo
        val tituloInicial: String = "",
        val contenidoInicial: String = "",
        val visibleInicial: Boolean = false,
        val onSave: (String, String, Boolean) -> Unit // titulo, contenido, visible
    ) : DialogState()

    data class EditHorario(
        val horario: Horario,
        val asignaturasDisponibles: List<Asignatura>,
        val onSave: (Horario) -> Unit,
        val onDelete: (Horario) -> Unit = {}
    ) : DialogState()

    // 7. Selectores de Fecha y Hora
    data class DatePicker(
        val initialDate: String,
        val onDateSelected: (String) -> Unit
    ) : DialogState()

    data class TimePicker(
        val initialTime: String,
        val onTimeSelected: (String) -> Unit
    ) : DialogState()

    // 8. Diálogos para Tareas (Hybrid)
    data class AddTarea(
        val asignaturaId: String,
        val unidadId: String,
        val tareaExistente: Tarea? = null,
        val onSave: (Tarea, ByteArray?, String?, String?) -> Unit // Tarea, fileData, fileName, mimeType
    ) : DialogState()

    data class TareaDetalleEstudiante(
        val tarea: Tarea,
        val estudianteId: String,
        val estudianteNombre: String,
        val onEntregar: (ByteArray, String, String?) -> Unit,
        val onEliminarEntrega: () -> Unit
    ) : DialogState()

    data class VerEntregasProfesor(
        val tarea: Tarea,
        val onCalificar: (Evaluacion) -> Unit
    ) : DialogState()

    data class VerDetalleEvaluacion(
        val evaluacion: Evaluacion
    ) : DialogState()

    // 9. Permisos
    data class NotificationPermissionRationale(
        val onConfirm: () -> Unit
    ) : DialogState()
}
