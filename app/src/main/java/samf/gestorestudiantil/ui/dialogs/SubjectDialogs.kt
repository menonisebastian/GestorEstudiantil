package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre de la unidad"
                )
                CustomTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = "Descripción",
                    singleLine = false,
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

                CustomOptionsTextField(
                    texto = selectedAsignatura?.let { "${it.acronimo} - ${it.nombre}" } ?: "Ninguna / Vacío",
                    onValueChange = { seleccion ->
                        selectedAsignaturaId = if (seleccion == "Ninguna / Vacío") "" 
                        else state.asignaturasDisponibles.find { "${it.acronimo} - ${it.nombre}" == seleccion }?.idFirestore ?: ""
                    },
                    opciones = listOf("Ninguna / Vacío") + state.asignaturasDisponibles.map { "${it.acronimo} - ${it.nombre}" },
                    label = "Asignatura"
                )

                CustomTextField(
                    value = aula,
                    onValueChange = { aula = it },
                    label = "Aula"
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = "Título"
                )
                CustomTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = "Contenido / Mensaje",
                    singleLine = false,
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
