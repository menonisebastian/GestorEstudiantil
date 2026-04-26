package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import samf.gestorestudiantil.data.enums.tipoRecordatorio
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.ui.components.CustomDateField
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.CustomTimeField
import samf.gestorestudiantil.ui.theme.*
import java.util.UUID

@Composable
fun RecordatorioDialog(
    state: DialogState.Recordatorio,
    onShowDialog: (DialogState) -> Unit,
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        RecordatorioContent(
            state = state,
            onShowDialog = onShowDialog,
            onDismissRequest = onDismissRequest,
            modifier = Modifier
                .background(backgroundColor, RoundedCornerShape(16.dp))
                .padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordatorioBottomSheet(
    state: DialogState.Recordatorio,
    onShowDialog: (DialogState) -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = backgroundColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (state.recordatorioExistente == null) "Añadir Recordatorio" else "Editar Recordatorio",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            RecordatorioContent(
                state = state,
                onShowDialog = onShowDialog,
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@Composable
fun RecordatorioContent(
    state: DialogState.Recordatorio,
    onShowDialog: (DialogState) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordatorio = state.recordatorioExistente
    var titulo by remember { mutableStateOf(recordatorio?.titulo ?: "") }
    var descripcion by remember { mutableStateOf(recordatorio?.descripcion ?: "") }
    var fecha by remember { mutableStateOf(recordatorio?.fecha ?: state.initialDate) }
    var hora by remember { mutableStateOf(recordatorio?.hora ?: "") }
    var tipo by remember { mutableStateOf(recordatorio?.tipo ?: tipoRecordatorio.EXAMEN) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (modifier != Modifier) {
            Text(
                text = if (recordatorio == null) "Añadir Recordatorio" else "Editar Recordatorio",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
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
                onShowDialog(DialogState.TimePicker(hora) { 
                    val formattedTime = if (it.length == 4 && !it.contains(":")) {
                        "${it.substring(0, 2)}:${it.substring(2)}"
                    } else it
                    hora = formattedTime 
                })
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
                    val finalRecordatorio = recordatorio?.copy(
                        titulo = titulo,
                        descripcion = descripcion,
                        fecha = fecha,
                        hora = hora,
                        tipo = tipo
                    ) ?: Recordatorio(
                        id = UUID.randomUUID().toString(),
                        usuarioId = "", // Se asigna en el ViewModel
                        titulo = titulo,
                        descripcion = descripcion,
                        fecha = fecha,
                        hora = hora,
                        tipo = tipo
                    )
                    state.onSave(finalRecordatorio)
                    onDismissRequest()
                },
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = primaryColor),
                enabled = titulo.isNotBlank() && fecha.isNotBlank() && hora.isNotBlank()
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
