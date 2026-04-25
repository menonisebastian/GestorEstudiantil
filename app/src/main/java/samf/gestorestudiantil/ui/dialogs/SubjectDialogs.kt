package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.theme.whiteColor

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
        containerColor = backgroundColor,
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
                    Switch(
                        checked = visible,
                        onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = primaryColor,
                            checkedTrackColor = primaryColor.copy(alpha = 0.5f),
                            uncheckedThumbColor = surfaceDimColor,
                            uncheckedTrackColor = surfaceDimColor.copy(alpha = 0.5f)
                        )
                    )
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

    val selectedAsignatura = state.asignaturasDisponibles.find { it.id == selectedAsignaturaId }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Asignar Materia") },
        containerColor = backgroundColor,
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
                        else state.asignaturasDisponibles.find { "${it.acronimo} - ${it.nombre}" == seleccion }?.id ?: ""
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (state.horario.id.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            state.onDelete(state.horario)
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Eliminar")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        val asig = state.asignaturasDisponibles.find { it.id == selectedAsignaturaId }
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
            }
        },
        dismissButton = {
            if (state.horario.id.isEmpty()) {
                TextButton(onClick = onDismissRequest) {
                    Text("Cancelar")
                }
            }
        }
    )
}

@Composable
fun EditSelfProfileDialog(
    state: DialogState.EditSelfProfile,
    onDismissRequest: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.user.nombre) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Editar Perfil", color = textColor, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
        containerColor = backgroundColor,
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre"
                )
                Text(
                    "El email y el rol no se pueden cambiar desde aquí.",
                    fontSize = 12.sp,
                    color = surfaceDimColor
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    state.onSave(nombre)
                    onDismissRequest()
                },
                enabled = nombre.isNotBlank() && nombre != state.user.nombre,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Guardar", color = textColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", color = textColor)
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
        containerColor = backgroundColor,
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
                    Switch(checked = visible, onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                        checkedThumbColor = whiteColor,
                        checkedTrackColor = primaryColor,
                        uncheckedThumbColor = surfaceDimColor,
                        uncheckedTrackColor = surfaceDimColor.copy(alpha = 0.5f)
                    ))
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
