package samf.gestorestudiantil.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.CustomDateField
import samf.gestorestudiantil.ui.components.CustomTimeField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.TareaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTareaDialog(
    state: DialogState.AddTarea,
    onShowDialog: (DialogState) -> Unit,
    onDismissRequest: () -> Unit,
    viewModel: TareaViewModel = hiltViewModel()
) {
    
    LaunchedEffect(state.tareaExistente) {
        viewModel.inicializarCon(state.tareaExistente)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.onFileSelected(uri)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (state.tareaExistente == null) "Nueva Tarea" else "Editar Tarea") },
        containerColor = backgroundColor,
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomTextField(value = viewModel.titulo, onValueChange = { viewModel.titulo = it }, label = "Título")
                CustomTextField(
                    value = viewModel.descripcion,
                    onValueChange = { viewModel.descripcion = it },
                    label = "Instrucciones",
                    singleLine = false,
                    minLines = 3
                )

                // Selectores de Fecha y Hora
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomDateField(
                        value = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(viewModel.fechaLimite),
                        label = "Fecha Límite",
                        onShowDatePicker = {
                            val dateString = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(viewModel.fechaLimite)
                            onShowDialog(DialogState.DatePicker(dateString) { newDateString ->
                                val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                                sdf.parse(newDateString)?.let { newDate ->
                                    viewModel.updateFecha(newDate)
                                }
                            })
                        }
                    )
                    CustomTimeField(
                        value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(viewModel.fechaLimite),
                        label = "Hora Límite",
                        onShowTimePicker = {
                            val timeString = SimpleDateFormat("HH:mm", Locale.getDefault()).format(viewModel.fechaLimite)
                            onShowDialog(DialogState.TimePicker(timeString) { newTimeString ->
                                val parts = newTimeString.split(":")
                                if (parts.size == 2) {
                                    viewModel.updateHora(parts[0].toInt(), parts[1].toInt())
                                }
                            })
                        }
                    )
                }

                // Selector de Archivo
                OutlinedCard(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = backgroundColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Documento Adjunto (opcional)", style = MaterialTheme.typography.labelSmall, color = surfaceDimColor)
                            val displaySize = if (viewModel.selectedFileSize > 0) {
                                val kb = viewModel.selectedFileSize / 1024.0
                                if (kb > 1024) String.format(Locale.getDefault(), "%.2f MB", kb / 1024.0)
                                else String.format(Locale.getDefault(), "%.2f KB", kb)
                            } else ""
                            
                            Text(
                                text = if (viewModel.selectedFileName.isEmpty()) "Seleccionar archivo..." 
                                       else if (displaySize.isNotEmpty()) "${viewModel.selectedFileName} ($displaySize)"
                                       else viewModel.selectedFileName,
                                color = if (viewModel.selectedFileName.isEmpty()) surfaceDimColor else textColor,
                                maxLines = 1
                            )
                        }
                        if (viewModel.selectedFileName.isNotEmpty() && viewModel.selectedFileUri != null) {
                            IconButton(onClick = { viewModel.removeFile() }) {
                                Icon(Icons.Default.Close, contentDescription = "Quitar", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.visible = !viewModel.visible },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Visible para estudiantes")
                    Switch(checked = viewModel.visible, onCheckedChange = { viewModel.visible = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.save(state.asignaturaId, state.unidadId) { tarea, data, name, mime ->
                        state.onSave(tarea, data, name, mime)
                        onDismissRequest()
                    }
                },
                enabled = viewModel.titulo.isNotBlank()
            ) {
                Text(if (state.tareaExistente == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
