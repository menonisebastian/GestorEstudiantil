package samf.gestorestudiantil.ui.dialogs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember

@Composable
fun DialogOrchestrator(
    states: List<DialogState>,
    onShowDialog: (DialogState) -> Unit,
    onDismiss: (DialogState) -> Unit
) {
    fun DialogState.isOverlay(): Boolean = 
        this is DialogState.DatePicker || 
        this is DialogState.TimePicker || 
        this is DialogState.Confirmation ||
        this is DialogState.NotificationPermissionRationale
    
    val visibleStates = remember(states.size, states.lastOrNull()) {
        if (states.isEmpty()) return@remember emptyList()
        
        val result = mutableListOf<DialogState>()
        
        for (i in states.indices.reversed()) {
            val s = states[i]
            result.add(0, s)
 
            if (!s.isOverlay()) break
        }
        result
    }

    visibleStates.forEach { state ->
        val dismissAction = { onDismiss(state) }
        key(state) {
            when (state) {
                is DialogState.None -> Unit

                is DialogState.Confirmation -> {
                    ConfirmDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.Recordatorio -> {
                    RecordatorioBottomSheet(
                        state = state,
                        onShowDialog = onShowDialog,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.Filter -> {
                    FilterByBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.UserProfile -> {
                    UserProfileDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AsignarAsignaturas -> {
                    AsignarAsignaturasBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AsignarProfesor -> {
                    AsignarProfesorBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AsignarTutor -> {
                    AsignarTutorBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AddUnidad -> {
                    AddUnidadBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AddPost -> {
                    AddPostBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.EditHorario -> {
                    EditHorarioBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.EditUser -> {
                    EditUserBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.EditSelfProfile -> {
                    EditSelfProfileDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.EditCentro,
                is DialogState.EditCurso,
                is DialogState.EditAsignatura -> Unit

                is DialogState.DatePicker -> {
                    CustomDatePickerDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.TimePicker -> {
                    CustomTimePickerDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AddTarea -> {
                    AddTareaBottomSheet(
                        state = state,
                        onShowDialog = onShowDialog,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.TareaDetalleEstudiante -> {
                    TareaDetalleEstudianteBottomSheet(
                        state = state,
                        estudianteId = state.estudianteId,
                        estudianteNombre = state.estudianteNombre,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.VerEntregasProfesor -> {
                    VerEntregasProfesorBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction,
                        onOpenDialog = onShowDialog
                    )
                }

                is DialogState.VerDetalleEvaluacion -> {
                    VerDetalleEvaluacionBottomSheet(
                        state = state,
                        onDismiss = dismissAction
                    )
                }

                is DialogState.AddEditCalificacion -> {
                    AddEditCalificacionBottomSheet(
                        state = state,
                        onDismiss = dismissAction,
                    )
                }

                is DialogState.AttachmentOptions -> {
                    AttachmentOptionsBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.NotificationPermissionRationale -> {
                    NotificationPermissionDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }
            }
        }
    }
}
