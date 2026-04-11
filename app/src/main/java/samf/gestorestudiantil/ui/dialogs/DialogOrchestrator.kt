package samf.gestorestudiantil.ui.dialogs

import androidx.compose.runtime.Composable

@Composable
fun DialogOrchestrator(
    states: List<DialogState>,
    onShowDialog: (DialogState) -> Unit,
    onDismiss: (DialogState) -> Unit
) {
    states.forEach { state ->
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
                FilterByDialog(
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
                AsignarAsignaturasDialog(
                    state = state,
                    onDismissRequest = dismissAction
                )
            }

            is DialogState.AsignarProfesor -> {
                AsignarProfesorDialog(
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
                EditUserDialog(
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
        }
    }
}
