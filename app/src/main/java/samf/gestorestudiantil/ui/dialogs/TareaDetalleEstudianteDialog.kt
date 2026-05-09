package samf.gestorestudiantil.ui.dialogs

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.ui.theme.*
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareaDetalleEstudianteBottomSheet(
    state: DialogState.TareaDetalleEstudiante,
    estudianteId: String,
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
                text = state.tarea.titulo,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            TareaDetalleEstudianteContent(
                state = state,
                estudianteId = estudianteId,
                onDismissRequest = onDismissRequest
            )
        }
    }
}

@Composable
fun TareaDetalleEstudianteContent(
    state: DialogState.TareaDetalleEstudiante,
    estudianteId: String,
    onDismissRequest: () -> Unit
) {
    val viewModel: EstudianteViewModel = viewModel()
    val uiState by viewModel.state.collectAsState()
    val tarea = state.tarea

    LaunchedEffect(tarea.id, estudianteId) {
        viewModel.cargarMiEntrega(tarea.id, estudianteId)
    }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium, color = textColor)
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val esVencida = tarea.fechaLimiteEntrega.toDate().before(Date())

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.CalendarToday, 
                contentDescription = null, 
                tint = if (esVencida) errorColor else primaryColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Fecha límite: ${dateFormat.format(tarea.fechaLimiteEntrega.toDate())}",
                style = MaterialTheme.typography.bodySmall,
                color = if (esVencida) errorColor else surfaceDimColor
            )
        }

        tarea.adjunto?.let { adjunto ->
            OutlinedCard(
                onClick = { viewModel.descargarArchivo(adjunto.supabasePath, adjunto.nombreArchivo) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(containerColor = backgroundColor)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = primaryColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Material de apoyo", style = MaterialTheme.typography.labelSmall, color = surfaceDimColor)
                        Text(adjunto.nombreArchivo, color = textColor, maxLines = 1)
                    }
                }
            }
        }

        HorizontalDivider(color = surfaceDimColor.copy(alpha = 0.2f))

        val entrega = uiState.miEntrega
        if (entrega != null) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TU ENTREGA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = primaryColor)
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = primaryColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entrega.adjunto.nombreArchivo, color = textColor, maxLines = 1)
                            Text("Entregada el: ${dateFormat.format(entrega.fechaEntrega.toDate())}", style = MaterialTheme.typography.labelSmall, color = surfaceDimColor)
                        }
                        if (!esVencida && (entrega.calificacion == null)) {
                            IconButton(onClick = { state.onEliminarEntrega() }, colors = IconButtonDefaults.iconButtonColors(containerColor = errorColor.copy(alpha = 0.1f))) {
                                Icon(Icons.Default.Delete, contentDescription = "Anular entrega", tint = errorColor, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }

                if (entrega.calificacion != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = secondaryColor.copy(alpha = 0.1f)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Nota: ${entrega.calificacion}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = secondaryColor)
                            entrega.comentarioProfesor?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(it, style = MaterialTheme.typography.bodySmall, color = textColor)
                            }
                        }
                    }
                }
            }
        } else if (esVencida) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = errorColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Plazo finalizado. No puedes realizar entregas.", color = errorColor)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("REALIZAR ENTREGA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = tertiaryColor)
                
                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri -> viewModel.onFileSelected(uri) }

                OutlinedCard(
                    onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = surfaceDimColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = if (uiState.selectedFileName.isEmpty()) "Seleccionar archivo para entregar..." 
                                   else if (uiState.selectedFileSize > 0) {
                                       val kb = uiState.selectedFileSize / 1024.0
                                       val sizeStr = if (kb > 1024) String.format(Locale.getDefault(), "%.2f MB", kb / 1024.0)
                                       else String.format(Locale.getDefault(), "%.2f KB", kb)
                                       "${uiState.selectedFileName} ($sizeStr)"
                                   } else uiState.selectedFileName,
                            color = if (uiState.selectedFileName.isEmpty()) surfaceDimColor else textColor,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cerrar", color = secondaryColor)
            }

            if (uiState.miEntrega == null && !tarea.fechaLimiteEntrega.toDate().before(Date())) {
                Button(
                    onClick = {
                        val data = uiState.selectedFileData
                        if (data != null) {
                            state.onEntregar(data, uiState.selectedFileName, uiState.selectedMimeType)
                            onDismissRequest()
                        }
                    },
                    enabled = uiState.selectedFileData != null && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = whiteColor),
                    modifier = Modifier.weight(1f)
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = whiteColor)
                    else Text("Entregar")
                }
            }
        }
    }
}
