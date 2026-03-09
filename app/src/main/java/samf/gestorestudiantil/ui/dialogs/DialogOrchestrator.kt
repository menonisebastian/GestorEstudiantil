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
    }
}