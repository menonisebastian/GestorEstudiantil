package samf.gestorestudiantil.ui.dialogs

import androidx.compose.runtime.Composable

@Composable
fun DialogOrchestrator(
    state: DialogState,
    onDismiss: () -> Unit
) {
    when (state) {
        is DialogState.None -> Unit

        is DialogState.Confirmation -> {
            ConfirmDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.AddRecordatorio -> {
            AddRecordatorioDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.Filter -> {
            FilterByDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.UserProfile -> {
            UserProfileDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.AsignarAsignaturas -> {
            AsignarAsignaturasDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.AsignarProfesor -> {
            AsignarProfesorDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.AddUnidad -> {
            AddUnidadDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.AddPost -> {
            AddPostDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.EditHorario -> {
            EditHorarioDialog(
                state = state,
                onDismissRequest = onDismiss
            )
        }

        is DialogState.EditCentro,
        is DialogState.EditCurso,
        is DialogState.EditAsignatura,
        is DialogState.EditUser -> Unit
    }
}
