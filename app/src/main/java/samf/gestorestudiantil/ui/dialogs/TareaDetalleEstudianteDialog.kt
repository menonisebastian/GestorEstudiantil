package samf.gestorestudiantil.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.data.models.AdjuntoInfo
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TareaDetalleEstudianteDialog(
    state: DialogState.TareaDetalleEstudiante,
    estudianteId: String,
    estudianteNombre: String,
    onDismissRequest: () -> Unit
) {
    val viewModel: EstudianteViewModel = viewModel()
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val tarea = state.tarea

    LaunchedEffect(tarea.id, estudianteId) {
        viewModel.cargarMiEntrega(tarea.id, estudianteId)
    }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            selectedFileName = it.lastPathSegment ?: "archivo"
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        title = { Text(tarea.titulo) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Instrucciones y Fecha Límite
                Text(tarea.descripcion, style = MaterialTheme.typography.bodyMedium, color = textColor)
                
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val esVencida = tarea.fechaLimiteEntrega.toDate().before(Date())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarToday, 
                        contentDescription = null, 
                        tint = if (esVencida) MaterialTheme.colorScheme.error else primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Fecha límite: ${dateFormat.format(tarea.fechaLimiteEntrega.toDate())}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (esVencida) MaterialTheme.colorScheme.error else surfaceDimColor
                    )
                }

                // 2. Adjunto del profesor (si existe)
                tarea.adjunto?.let { adjunto ->
                    OutlinedCard(
                        onClick = { /* TODO: Descargar/Ver archivo del profesor */ },
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

                // 3. Estado de la entrega
                val entrega = uiState.miEntrega
                if (entrega != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("TU ENTREGA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = primaryColor)
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = primaryColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(entrega.adjunto.nombreArchivo, color = textColor, maxLines = 1)
                                    Text("Entregada el: ${dateFormat.format(entrega.fechaEntrega.toDate())}", style = MaterialTheme.typography.labelSmall, color = surfaceDimColor)
                                }
                                if (!esVencida && (entrega.calificacion == null)) {
                                    IconButton(onClick = { viewModel.eliminarEntrega(entrega) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Anular entrega", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        if (entrega.calificacion != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Nota: ${entrega.calificacion}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    entrega.comentarioProfesor?.let {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(it, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                } else if (esVencida) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Plazo finalizado. No puedes realizar entregas.", color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    // Sección para realizar entrega
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("REALIZAR ENTREGA", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        
                        OutlinedCard(
                            onClick = { filePickerLauncher.launch("*/*") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AttachFile, contentDescription = null, tint = surfaceDimColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    selectedFileName.ifEmpty { "Seleccionar archivo para entregar..." },
                                    color = if (selectedFileName.isEmpty()) surfaceDimColor else textColor,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (uiState.miEntrega == null && !tarea.fechaLimiteEntrega.toDate().before(Date())) {
                Button(
                    onClick = {
                        selectedFileUri?.let { uri ->
                            val fileData = context.contentResolver.openInputStream(uri)?.readBytes()
                            if (fileData != null) {
                                val entrega = Entrega(
                                    tareaId = tarea.id,
                                    estudianteId = estudianteId,
                                    estudianteNombre = estudianteNombre,
                                    profesorId = tarea.profesorId,
                                    asignaturaId = tarea.asignaturaId
                                )
                                state.onEntregar(fileData, selectedFileName)
                                onDismissRequest()
                            }
                        }
                    },
                    enabled = selectedFileUri != null && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = textColor)
                    else Text("Entregar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cerrar")
            }
        }
    )
}
