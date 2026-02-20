package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role.Companion.RadioButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import samf.gestorestudiantil.models.tipoRecordatorio
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun AddRecordatorioDialog(
    onDismissRequest: () -> Unit,
    onAddRecordatorio: (String, String, String, String, String) -> Unit,
)
{
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var hora by remember { mutableStateOf("") }
    val radioOptions = remember { tipoRecordatorio.entries.map { it -> it.name.lowercase().replaceFirstChar { it.titlecaseChar() } } }

    // CORRECCIÓN 2: Sintaxis correcta de estado (var ... by ...)
    var tipo by remember { mutableStateOf(radioOptions[0]) }


    Dialog(onDismissRequest = onDismissRequest)
    {
        Column(
            modifier = Modifier.background(backgroundColor, RoundedCornerShape(16.dp)).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        )
        {
            CustomTextField(
                texto = titulo,
                label = "Titulo",
                onValueChange = { titulo = it },
                icon = Icons.AutoMirrored.Filled.Label
            )
            CustomTextField(
                texto = descripcion,
                label = "Descripcion",
                onValueChange = { descripcion = it },
                icon = Icons.Filled.Description
            )
            CustomTextField(
                texto = fecha,
                label = "Fecha",
                onValueChange = { fecha = it },
                icon = Icons.Filled.DateRange
            )
            CustomTextField(
                texto = hora,
                label = "Hora",
                onValueChange = { hora = it },
                icon = Icons.Filled.AccessTime
            )

            Text("Tipo:", style = MaterialTheme.typography.titleMedium, color = textColor)

            Column(modifier = Modifier.selectableGroup()) {
                radioOptions.forEach { text ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (text == tipo),
                                onClick = { tipo = text },
                                role = RadioButton
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == tipo),
                            onClick = null, // null recommended for accessibility with screen readers
                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor, unselectedColor = surfaceDimColor)
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                            color = textColor
                        )
                    }
                }
            }

            // Botones de confirmar/cancelar
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
                    onAddRecordatorio(titulo, descripcion, fecha, hora, tipo)
                    onDismissRequest()
                }) { Text("Guardar", color = textColor) }
            }
        }
    }
}