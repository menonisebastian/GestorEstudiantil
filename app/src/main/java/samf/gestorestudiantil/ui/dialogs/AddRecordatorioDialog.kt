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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun AddRecordatorioDialog(
    state: DialogState.AddRecordatorio,
    onShowDialog: (DialogState) -> Unit, // Nuevo parámetro
    onDismissRequest: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(state.initialDate) }
    var hora by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf(tipoRecordatorio.EXAMEN) }

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
                label = "Fecha",
                onShowDatePicker = {
                    onShowDialog(DialogState.DatePicker(fecha) { fecha = it })
                }
            )
            CustomTimeField(
                value = hora,
                label = "Hora",
                onShowTimePicker = {
                    onShowDialog(DialogState.TimePicker(hora) { hora = it })
                }
            )
            Column {
                Text(
                    "Tipo",
                    fontSize = 12.sp,
                    color = surfaceDimColor,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tipoRecordatorio.entries.forEach { entry ->
                        val isSelected = tipo == entry
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) entry.color.copy(alpha = 0.2f) else surfaceColor)
                                .clickable { tipo = entry }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = entry.label,
                                color = if (isSelected) entry.color else surfaceDimColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancelar",
                        tint = surfaceDimColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        state.onSave(titulo, descripcion, fecha, hora, tipo)
                        onDismissRequest()
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = primaryColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Guardar",
                        tint = textColor
                    )
                }
            }
        }
    }
}
