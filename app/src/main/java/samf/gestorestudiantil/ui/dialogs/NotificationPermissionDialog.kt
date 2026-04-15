package samf.gestorestudiantil.ui.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.theme.whiteColor

@Composable
fun NotificationPermissionDialog(
    state: DialogState.NotificationPermissionRationale,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        title = { Text("Permiso de Notificaciones", color = textColor, fontWeight = FontWeight.Bold)},
        text = { Text("Necesitamos este permiso para avisarte sobre nuevas tareas, calificaciones y mensajes importantes de tus profesores.", color = textColor) },
        confirmButton = {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                onClick = {
                    state.onConfirm()
                    onDismissRequest()
                }
            ) {
                Text("Aceptar", color = whiteColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", color = surfaceDimColor)
            }
        }
    )
}
