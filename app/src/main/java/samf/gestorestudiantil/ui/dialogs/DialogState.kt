package samf.gestorestudiantil.ui.dialogs

import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.AsistenciaEstado
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.data.models.Tarea
import samf.gestorestudiantil.domain.utils.UiText

sealed class DialogState {
    data object None : DialogState()

    // 1. Diálogo de Confirmación
    data class Confirmation(
        val title: UiText,
        val content: UiText,
        val onConfirm: () -> Unit
    ) : DialogState()

    // 2. Diálogo de Recordatorios
    data class Recordatorio(
        val initialDate: String = "",
        val recordatorioExistente: samf.gestorestudiantil.data.models.Recordatorio? = null,
        val onSave: (samf.gestorestudiantil.data.models.Recordatorio) -> Unit
    ) : DialogState()

    // 3. Diálogo de Filtros
    data class Filter(
        val tipo: String,
        val currentFilters: Map<String, String> = emptyMap(),
        val opcionesPersonalizadas: Map<String, List<String>> = emptyMap(),
        val onApply: (Map<String, String>) -> Unit
    ) : DialogState()

    // 4. Diálogo de Perfil de Usuario
    data class UserProfile(
        val user: User
    ) : DialogState()

    data class EditSelfProfile(
        val user: User,
        val onSave: (String) -> Unit
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
        val onAssign: (String) -> Unit,
        val onUnassign: (String) -> Unit
    ) : DialogState()

    data class AsignarProfesor(
        val asignatura: Asignatura
    ) : DialogState()

    data class AsignarTutor(
        val claseId: String,
        val centroId: String
    ) : DialogState()

    // 6. Diálogos para Asignaturas
    data class AddUnidad(
        val asignaturaId: String,
        val unidadId: String? = null,
        val nombreInicial: String = "",
        val descripcionInicial: String = "",
        val visibleInicial: Boolean = false,
        val ordenInicial: Int = 1,
        val onSave: (String, String, Boolean, Int) -> Unit
    ) : DialogState()

    data class AddPost(
        val asignaturaId: String,
        val unidadId: String,
        val postId: String? = null,
        val tituloInicial: String = "",
        val contenidoInicial: String = "",
        val visibleInicial: Boolean = false,
        val onSave: (String, String, Boolean) -> Unit
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
        val allowPastDates: Boolean = false,
        val onDateSelected: (String) -> Unit
    ) : DialogState()

    data class TimePicker(
        val initialTime: String,
        val onTimeSelected: (String) -> Unit
    ) : DialogState()

    // 8. Diálogos para Tareas
    data class AddTarea(
        val asignaturaId: String,
        val unidadId: String,
        val centroId: String = "",
        val acronimoAsignatura: String = "",
        val profesorId: String = "",
        val profesorNombre: String = "",
        val tareaExistente: Tarea? = null,
        val onSave: () -> Unit
    ) : DialogState()

    data class TareaDetalleEstudiante(
        val tarea: Tarea,
        val estudianteId: String,
        val estudianteNombre: String,
        val onEntregar: (ByteArray, String, String?) -> Unit,
        val onEliminarEntrega: () -> Unit,
        val onAttachmentClick: ((String, String) -> Unit)? = null
    ) : DialogState()

    data class VerEntregasProfesor(
        val tarea: Tarea,
        val onCalificar: (Evaluacion) -> Unit
    ) : DialogState()

    data class VerDetalleEvaluacion(
        val evaluacion: Evaluacion,
        val onAttachmentClick: ((String, String) -> Unit)? = null
    ) : DialogState()

    data class AddEditCalificacion(
        val evaluacion: Evaluacion,
        val onSave: (Evaluacion) -> Unit,
        val onAttachmentClick: ((String, String) -> Unit)? = null
    ) : DialogState()

    // 10. Archivos Adjuntos
    data class AttachmentOptions(
        val supabasePath: String,
        val fileName: String,
        val onOpen: (String, String) -> Unit,
        val onDownload: (String, String) -> Unit
    ) : DialogState()

    // 11. Permisos
    data class NotificationPermissionRationale(
        val onConfirm: () -> Unit
    ) : DialogState()

    data class SelectAsistencia(
        val estudianteNombre: String,
        val estadoActual: AsistenciaEstado?,
        val onEstadoSelected: (AsistenciaEstado) -> Unit
    ) : DialogState()

    data object Help : DialogState()
}
