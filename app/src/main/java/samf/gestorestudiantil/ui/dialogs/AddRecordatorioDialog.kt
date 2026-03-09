package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import samf.gestorestudiantil.data.enums.tipoRecordatorio
import samf.gestorestudiantil.ui.components.CustomDateField
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.CustomTimeField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun AddRecordatorioDialog(
    state: DialogState.AddRecordatorio, // <-- CAMBIO AQUÍ
    onDismissRequest: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }

    val radioOptions = remember {
        tipoRecordatorio.entries.map { it.name.lowercase().replaceFirstChar { c -> c.titlecaseChar() } }
    }
    var tipo by remember { mutableStateOf(radioOptions[0]) }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Añadir Recordatorio",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            CustomTextField(
                value = titulo,
                onValueChange = { titulo = it },
                icon = Icons.AutoMirrored.Filled.Label,
                label = "Titulo",
                readOnly = false,
                isClickable = false
            )
            CustomTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                icon = Icons.Filled.Description,
                label = "Descripcion",
                readOnly = false,
                isClickable = false
            )
            CustomDateField(
                value = fecha,
                onValueChange = { fecha = it },
                label = "Fecha",
            )
            CustomTimeField(
                value = hora,
                onValueChange = { hora = it },
                label = "Hora",
            )
            CustomOptionsTextField(
                texto = tipo,
                label = "Tipo",
                onValueChange = { tipo = it },
                opciones = radioOptions,
                icon = Icons.Default.FormatListNumbered
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) { Text("Cancelar") }
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    onClick = {
                        val selectedEnum = tipoRecordatorio.entries.firstOrNull { enumEntry ->
                            val formattedName = enumEntry.name.lowercase()
                                .replaceFirstChar { char -> char.titlecaseChar() }
                            formattedName == tipo
                        } ?: tipoRecordatorio.entries.first()

                        // Usamos el callback del estado
                        state.onSave(titulo, descripcion, fecha, hora, selectedEnum)
                        onDismissRequest()
                    }
                ) { Text("Guardar", color = textColor) }
            }
        }
    }
}