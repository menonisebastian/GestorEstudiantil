package samf.gestorestudiantil.ui.dialogs

import samf.gestorestudiantil.data.enums.tipoRecordatorio
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
        val onApply: (String) -> Unit // Retorna el valor seleccionado
    ) : DialogState()
}