package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.secondaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.theme.whiteColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePickerDialog(
    state: DialogState.TimePicker,
    onDismissRequest: () -> Unit
) {
    val (initialHour, initialMinute) = remember(state.initialTime) {
        if (state.initialTime.isNotBlank() && state.initialTime.contains(":")) {
            try {
                val parts = state.initialTime.split(":")
                parts[0].toInt() to parts[1].toInt()
            } catch (e: Exception) {
                val cal = Calendar.getInstance()
                cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
            }
        } else {
            val cal = Calendar.getInstance()
            cal.get(Calendar.HOUR_OF_DAY) to cal.get(Calendar.MINUTE)
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        confirmButton = {
            IconButton(
                onClick = {
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute)
                    state.onTimeSelected(formattedTime)
                    onDismissRequest()
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = primaryColor)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Seleccionar", tint = whiteColor)
            }
        },
        dismissButton = {
            IconButton(
                onClick = onDismissRequest,
                colors = IconButtonDefaults.iconButtonColors(containerColor = secondaryColor.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = secondaryColor)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Seleccionar hora",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        // Este es el atributo para el fondo del recuadro (hora/minuto) seleccionado
                        timeSelectorSelectedContainerColor = tertiaryColor.copy(alpha = 0.1f),
                        timeSelectorUnselectedContainerColor = surfaceColor,
                        clockDialColor = surfaceColor,
                        timeSelectorSelectedContentColor = primaryColor
                    )
                )
            }
        }
    )
}
