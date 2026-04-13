package samf.gestorestudiantil.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import samf.gestorestudiantil.ui.theme.backgroundColor

@Composable
fun ConfirmDialog(
    state: DialogState.Confirmation, // <-- Recibe el estado
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        title = { Text(text = state.title) },
        text = { Text(text = state.content) },
        confirmButton = {
            Button(onClick = {
                state.onConfirm()
                onDismissRequest()
            }) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
