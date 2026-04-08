package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Horario

@Composable
fun AddUnidadDialog(
    state: DialogState.AddUnidad,
    onDismissRequest: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.nombreInicial) }
    var descripcion by remember { mutableStateOf(state.descripcionInicial) }
    var visible by remember { mutableStateOf(state.visibleInicial) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (state.unidadId == null) "Nueva Unidad" else "Editar Unidad") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la unidad") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { visible = !visible },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Visible para estudiantes")
                    Switch(checked = visible, onCheckedChange = null)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    state.onSave(nombre, descripcion, visible)
                    onDismissRequest()
                },
                enabled = nombre.isNotBlank()
            ) {
                Text(if (state.unidadId == null) "Crear" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditHorarioDialog(
    state: DialogState.EditHorario,
    onDismissRequest: () -> Unit
) {
    var selectedAsignaturaId by remember { mutableStateOf(state.horario.asignaturaId) }
    var aula by remember { mutableStateOf(state.horario.aula) }
    var expanded by remember { mutableStateOf(false) }

    val selectedAsignatura = state.asignaturasDisponibles.find { it.idFirestore == selectedAsignaturaId }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Asignar Materia") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(text = "${state.horario.dia} (${state.horario.horaInicio} - ${state.horario.horaFin})", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)

                Box {
                    OutlinedTextField(
                        value = selectedAsignatura?.nombre ?: "Ninguna / Vacío",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asignatura") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguna / Vacío") },
                            onClick = {
                                selectedAsignaturaId = ""
                                expanded = false
                            }
                        )
                        state.asignaturasDisponibles.forEach { asig ->
                            DropdownMenuItem(
                                text = { Text("${asig.acronimo} - ${asig.nombre}") },
                                onClick = {
                                    selectedAsignaturaId = asig.idFirestore
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = aula,
                    onValueChange = { aula = it },
                    label = { Text("Aula") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val asig = state.asignaturasDisponibles.find { it.idFirestore == selectedAsignaturaId }
                    val updated = state.horario.copy(
                        asignaturaId = selectedAsignaturaId,
                        asignaturaAcronimo = asig?.acronimo ?: "",
                        profesorId = asig?.profesorId ?: "",
                        profesorNombre = asig?.profesorNombre ?: "",
                        aula = aula
                    )
                    state.onSave(updated)
                    onDismissRequest()
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun AddPostDialog(
    state: DialogState.AddPost,
    onDismissRequest: () -> Unit
) {
    var titulo by remember { mutableStateOf(state.tituloInicial) }
    var contenido by remember { mutableStateOf(state.contenidoInicial) }
    var visible by remember { mutableStateOf(state.visibleInicial) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (state.postId == null) "Nueva Publicación" else "Editar Publicación") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido / Mensaje") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 5
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { visible = !visible },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Visible para estudiantes")
                    Switch(checked = visible, onCheckedChange = null)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    state.onSave(titulo, contenido, visible)
                    onDismissRequest()
                },
                enabled = titulo.isNotBlank() && contenido.isNotBlank()
            ) {
                Text(if (state.postId == null) "Publicar" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar")
            }
        }
    )
}
