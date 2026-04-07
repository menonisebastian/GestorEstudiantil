package samf.gestorestudiantil.ui.dialogs

import samf.gestorestudiantil.data.enums.tipoRecordatorio
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.User
import java.util.UUID

sealed class DialogState {
    data object None : DialogState()

    // 1. Diálogo de Confirmación (Usado en AdminPanel)
    data class Confirmation(
        val title: String,
        val content: String,
        val onConfirm: () -> Unit
    ) : DialogState()

    // 2. Diálogo de Añadir Recordatorio (Usado en HomeScreen)
    data class AddRecordatorio(
        val onSave: (String, String, String, String, tipoRecordatorio) -> Unit
    ) : DialogState()

    // 3. Diálogo de Filtros (Usado en Recordatorios y Admin)
    data class Filter(
        val tipo: String, // "Usuario", "Asignatura", "Recordatorio", etc.
        val currentFilters: Map<String, String> = emptyMap(),
        val onApply: (Map<String, String>) -> Unit // Retorna el mapa de filtros actualizados
    ) : DialogState()

    // 4. Diálogo de Perfil de Usuario
    data class UserProfile(
        val user: User
    ) : DialogState()

    // 5. Diálogos de Edición/Creación para Admin
    data class EditCentro(
        val centro: Centro? = null,
        val onSave: (Centro) -> Unit
    ) : DialogState()

    data class EditCurso(
        val curso: Curso? = null,
        val centroId: String,
        val onSave: (Curso) -> Unit
    ) : DialogState()

    data class EditAsignatura(
        val asignatura: Asignatura? = null,
        val cursoId: String,
        val centroId: String,
        val onSave: (Asignatura) -> Unit
    ) : DialogState()

    data class AsignarAsignaturas(
        val profesor: User,
        val onAssign: (String) -> Unit, // id de la asignatura
        val onUnassign: (String) -> Unit // id de la asignatura
    ) : DialogState()

    data class AsignarProfesor(
        val asignatura: Asignatura
    ) : DialogState()
}
