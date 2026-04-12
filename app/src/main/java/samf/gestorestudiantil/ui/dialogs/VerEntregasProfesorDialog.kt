package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.data.models.Entrega
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VerEntregasProfesorDialog(
    state: DialogState.VerEntregasProfesor,
    onDismissRequest: () -> Unit
) {
    val viewModel: ProfesorViewModel = viewModel()
    val entregas by viewModel.entregas.collectAsState()
    var entregaParaCalificar by remember { mutableStateOf<Entrega?>(null) }

    LaunchedEffect(state.tarea.id) {
        viewModel.cargarEntregas(state.tarea.id)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        title = { Text("Entregas: ${state.tarea.titulo}") },
        text = {
            Box(modifier = Modifier.heightIn(max = 400.dp)) {
                if (entregas.isEmpty()) {
                    Text("No hay entregas todavía.", modifier = Modifier.padding(16.dp), color = surfaceDimColor)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(entregas) { entrega ->
                            EntregaItem(
                                entrega = entrega,
                                onCalificar = { entregaParaCalificar = entrega },
                                onDescargar = {
                                    entrega.adjunto.let { 
                                        viewModel.descargarArchivo(it.supabasePath, it.nombreArchivo)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Cerrar") }
        }
    )

    if (entregaParaCalificar != null) {
        CalificarEntregaDialog(
            entrega = entregaParaCalificar!!,
            onDismiss = { entregaParaCalificar = null },
            onSave = { nota, comentario ->
                state.onCalificar(entregaParaCalificar!!.id, nota, comentario)
                entregaParaCalificar = null
            }
        )
    }
}

@Composable
fun EntregaItem(
    entrega: Entrega,
    onCalificar: () -> Unit,
    onDescargar: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Card(
        colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entrega.estudianteNombre, fontWeight = FontWeight.Bold)
                Text("Entregado: ${dateFormat.format(entrega.fechaEntrega.toDate())}", style = MaterialTheme.typography.labelSmall, color = surfaceDimColor)
                if (entrega.calificacion != null) {
                    Text("Nota: ${entrega.calificacion}", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
            IconButton(onClick = onDescargar) {
                Icon(Icons.Default.Download, contentDescription = "Descargar", tint = primaryColor)
            }
            IconButton(onClick = onCalificar) {
                Icon(Icons.Default.Edit, contentDescription = "Calificar", tint = surfaceDimColor)
            }
        }
    }
}

@Composable
fun CalificarEntregaDialog(
    entrega: Entrega,
    onDismiss: () -> Unit,
    onSave: (Float, String?) -> Unit
) {
    var nota by remember { mutableStateOf(entrega.calificacion?.toString() ?: "") }
    var comentario by remember { mutableStateOf(entrega.comentarioProfesor ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar a ${entrega.estudianteNombre}") },
        containerColor = backgroundColor,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomTextField(
                    value = nota,
                    onValueChange = { if (it.isEmpty() || it.toFloatOrNull() != null) nota = it },
                    label = "Calificación (0-10)"
                )
                CustomTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = "Comentario (opcional)",
                    singleLine = false,
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nota.toFloat(), comentario.ifEmpty { null }) },
                enabled = nota.isNotEmpty() && nota.toFloatOrNull() != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
