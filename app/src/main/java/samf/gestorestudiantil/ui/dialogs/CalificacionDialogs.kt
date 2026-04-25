package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.data.enums.tipoEvaluacion
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.ui.components.CustomDropDownMenu
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.MenuItem
import samf.gestorestudiantil.ui.components.TypeChip
import samf.gestorestudiantil.ui.theme.*
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel
import java.util.Locale

@Composable
fun EvaluacionProfesorItem(
    evaluacion: Evaluacion,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDownload: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            if (!evaluacion.visible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(surfaceDimColor.copy(alpha = 0.5f))
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .graphicsLayer(alpha = if (evaluacion.visible) 1f else 0.6f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(evaluacion.nombre, fontWeight = FontWeight.Bold, color = textColor)
                        if (!evaluacion.visible) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "OCULTO",
                                    fontSize = 10.sp,
                                    color = surfaceDimColor,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    if (evaluacion.adjunto != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Tiene entrega",
                                tint = primaryColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Archivo adjunto", fontSize = 11.sp, color = primaryColor)
                        }
                    }

                    if (!evaluacion.comentario.isNullOrBlank()) {
                        Text(
                            text = evaluacion.comentario!!,
                            fontSize = 11.sp,
                            color = surfaceDimColor,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                if (evaluacion.adjunto != null && onDownload != null) {
                    IconButton(onClick = onDownload) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Descargar entrega",
                            tint = primaryColor
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TypeChip(option = evaluacion.tipoEvaluacion)
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f", evaluacion.nota),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = if (evaluacion.nota >= 5) Color(0xFF73BE77) else Color(0xFFD55047)
                    )
                }

                CustomDropDownMenu(
                    items = listOf(
                        MenuItem(
                            text = "Editar",
                            icon = Icons.Default.Edit,
                            onClick = onEdit
                        ),
                        MenuItem(
                            text = if (evaluacion.visible) "Ocultar" else "Mostrar",
                            icon = if (evaluacion.visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            onClick = onToggleVisibility
                        ),
                        MenuItem(
                            text = "Eliminar",
                            icon = Icons.Default.Delete,
                            onClick = onDelete,
                            isDestructive = true
                        )
                    )
                )
            }
        }
    }
}

@Composable
fun VerDetalleEvaluacionDialog(
    evaluacion: Evaluacion,
    onDismiss: () -> Unit
) {
    val viewModel: ProfesorViewModel = hiltViewModel()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = backgroundColor,
        title = {
            Column {
                TypeChip(option = evaluacion.tipoEvaluacion)
                Spacer(modifier = Modifier.height(8.dp))
                Text(evaluacion.nombre, fontWeight = FontWeight.Bold, color = textColor)
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Calificación", color = surfaceDimColor, fontSize = 14.sp)
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f", evaluacion.nota),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (evaluacion.nota >= 5) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }

                if (!evaluacion.comentario.isNullOrBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(surfaceColor)
                            .padding(12.dp)
                    ) {
                        Text("Comentario del profesor", style = MaterialTheme.typography.labelSmall, color = primaryColor, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(evaluacion.comentario!!, color = textColor, fontSize = 14.sp)
                    }
                }

                evaluacion.adjunto?.let { adjunto ->
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Archivo adjunto", style = MaterialTheme.typography.labelSmall, color = surfaceDimColor)
                        OutlinedCard(
                            onClick = { viewModel.descargarArchivo(adjunto.supabasePath, adjunto.nombreArchivo) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.outlinedCardColors(containerColor = surfaceColor),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = primaryColor)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(adjunto.nombreArchivo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor, maxLines = 1)
                                }
                                Icon(Icons.Default.Download, contentDescription = "Descargar", tint = primaryColor)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Cerrar", color = textColor)
            }
        }
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditCalificacionDialog(
    evaluacion: Evaluacion,
    onDismiss: () -> Unit,
    onSave: (Evaluacion) -> Unit
) {
    var nombre by remember { mutableStateOf(evaluacion.nombre) }
    var nota by remember { mutableStateOf(evaluacion.nota.let { if (it == 0.0 && evaluacion.id.isEmpty()) "" else String.format(Locale.US, "%.2f", it) }) }
    var tipoSeleccionado by remember { mutableStateOf(evaluacion.tipoEvaluacion) }
    var comentario by remember { mutableStateOf(evaluacion.comentario ?: "") }
    var visible by remember { mutableStateOf(evaluacion.visible) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = backgroundColor,
        title = { Text(if (evaluacion.id.isEmpty()) "Nueva Calificación" else "Editar Calificación", fontWeight = FontWeight.Bold, color = textColor) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CustomTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre del trabajo/examen",
                    modifier = Modifier.fillMaxWidth()
                )
                
                Column {
                    Text("Tipo de evaluación", fontSize = 12.sp, color = surfaceDimColor, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tipoEvaluacion.entries.forEach { tipo ->
                            val isSelected = tipoSeleccionado == tipo
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) tipo.color.copy(alpha = 0.2f) else surfaceColor)
                                    .clickable { tipoSeleccionado = tipo }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tipo.label,
                                    color = if (isSelected) tipo.color else surfaceDimColor,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                CustomTextField(
                    value = nota,
                    onValueChange = { if (it.isEmpty() || it.replace(",", ".").toDoubleOrNull() != null) nota = it },
                    label = "Nota (0.0 - 10.0)",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                CustomTextField(
                    value = comentario,
                    onValueChange = { comentario = it },
                    label = "Comentario (opcional)",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    minLines = 3
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Visibilidad para el estudiante", fontWeight = FontWeight.Bold, color = textColor, fontSize = 14.sp)
                        Text(if (visible) "Visible" else "Oculto", color = surfaceDimColor, fontSize = 12.sp)
                    }
                    Switch(
                        checked = visible,
                        onCheckedChange = { visible = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = whiteColor,
                            checkedTrackColor = primaryColor,
                            uncheckedThumbColor = surfaceDimColor,
                            uncheckedTrackColor = surfaceDimColor.copy(alpha = 0.5f)
                        )
                    )
                }

                evaluacion.adjunto?.let { adjunto ->
                    val viewModel: ProfesorViewModel = hiltViewModel()
                    OutlinedCard(
                        onClick = { viewModel.descargarArchivo(adjunto.supabasePath, adjunto.nombreArchivo) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = surfaceColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = primaryColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Archivo entregado", style = MaterialTheme.typography.labelSmall, color = surfaceDimColor)
                                Text(adjunto.nombreArchivo, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor, maxLines = 1)
                            }
                            Icon(Icons.Default.Download, contentDescription = "Descargar", tint = primaryColor)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(evaluacion.copy(
                        nombre = nombre,
                        nota = nota.replace(",", ".").toDoubleOrNull() ?: 0.0,
                        tipoEvaluacion = tipoSeleccionado,
                        comentario = comentario.ifBlank { null },
                        visible = visible
                    ))
                    onDismiss()
                },
                shape = RoundedCornerShape(12.dp),
                enabled = nombre.isNotBlank() && nota.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Guardar", color = textColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = textColor.copy(alpha = 0.7f)) }
        }
    )
}
