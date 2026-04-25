package samf.gestorestudiantil.ui.dialogs

import androidx.compose.runtime.Composable

@Composable
fun DialogOrchestrator(
    states: List<DialogState>,
    onShowDialog: (DialogState) -> Unit,
    onDismiss: (DialogState) -> Unit
) {
    states.lastOrNull()?.let { state ->
        val dismissAction = { onDismiss(state) }
        when (state) {
            is DialogState.None -> Unit

            is DialogState.Confirmation -> {
                ConfirmDialog(
                    state = state,
                    onDismissRequest = dismissAction
                )
            }

            is DialogState.AddRecordatorio -> {
                AddRecordatorioDialog(
                    state = state,
                    onShowDialog = onShowDialog,
                    onDismissRequest = dismissAction
                )
            }

            is DialogState.EditRecordatorio -> {
                EditRecordatorioDialog(
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
                AddUnidadDialog(
                    state = state,
                    onDismissRequest = dismissAction
                )
            }

            is DialogState.AddPost -> {
                AddPostDialog(
                    state = state,
                    onDismissRequest = dismissAction
                )
            }

            is DialogState.EditHorario -> {
                EditHorarioDialog(
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
                AddTareaDialog(
                    state = state,
                    onShowDialog = onShowDialog,
                    onDismissRequest = dismissAction
                )
            }

            is DialogState.TareaDetalleEstudiante -> {
                TareaDetalleEstudianteDialog(
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
                VerDetalleEvaluacionDialog(
                    evaluacion = state.evaluacion,
                    onDismiss = dismissAction
                )
            }

            is DialogState.AddEditCalificacion -> {
                AddEditCalificacionDialog(
                    evaluacion = state.evaluacion,
                    onDismiss = dismissAction,
                    onSave = state.onSave
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
