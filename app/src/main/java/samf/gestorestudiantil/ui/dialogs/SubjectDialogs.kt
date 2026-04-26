package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.theme.*

@Composable
fun AddUnidadDialog(
    state: DialogState.AddUnidad,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (state.unidadId == null) "Nueva Unidad" else "Editar Unidad") },
        containerColor = backgroundColor,
        text = {
            AddUnidadContent(state = state, onDismissRequest = onDismissRequest)
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUnidadBottomSheet(
    state: DialogState.AddUnidad,
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
                text = if (state.unidadId == null) "Nueva Unidad" else "Editar Unidad",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            AddUnidadContent(state = state, onDismissRequest = onDismissRequest)
        }
    }
}

@Composable
fun AddUnidadContent(
    state: DialogState.AddUnidad,
    onDismissRequest: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.nombreInicial) }
    var descripcion by remember { mutableStateOf(state.descripcionInicial) }
    var visible by remember { mutableStateOf(state.visibleInicial) }

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
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar", color = textColor)
            }
            Button(
                onClick = {
                    state.onSave(nombre, descripcion, visible)
                    onDismissRequest()
                },
                enabled = nombre.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (state.unidadId == null) "Crear" else "Guardar")
            }
        }
    }
}

@Composable
fun EditHorarioDialog(
    state: DialogState.EditHorario,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Asignar Materia") },
        containerColor = backgroundColor,
        text = {
            EditHorarioContent(state = state, onDismissRequest = onDismissRequest)
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHorarioBottomSheet(
    state: DialogState.EditHorario,
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
                text = "Asignar Materia",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            EditHorarioContent(state = state, onDismissRequest = onDismissRequest)
        }
    }
}

@Composable
fun EditHorarioContent(
    state: DialogState.EditHorario,
    onDismissRequest: () -> Unit
) {
    var selectedAsignaturaId by remember { mutableStateOf(state.horario.asignaturaId) }
    var aula by remember { mutableStateOf(state.horario.aula) }

    val selectedAsignatura = state.asignaturasDisponibles.find { it.id == selectedAsignaturaId }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "${state.horario.dia} (${state.horario.horaInicio} - ${state.horario.horaFin})", fontWeight = FontWeight.Bold)

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

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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
                TextButton(onClick = onDismissRequest) {
                    Text("Cancelar", color = textColor)
                }
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
    }
}

@Composable
fun EditSelfProfileDialog(
    state: DialogState.EditSelfProfile,
    onDismissRequest: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.user.nombre) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Editar Perfil", color = textColor, fontWeight = FontWeight.Bold) },
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
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
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
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(if (state.postId == null) "Nueva Publicación" else "Editar Publicación") },
        containerColor = backgroundColor,
        text = {
            AddPostContent(state = state, onDismissRequest = onDismissRequest)
        },
        confirmButton = {},
        dismissButton = {}
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostBottomSheet(
    state: DialogState.AddPost,
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
                text = if (state.postId == null) "Nueva Publicación" else "Editar Publicación",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            AddPostContent(state = state, onDismissRequest = onDismissRequest)
        }
    }
}

@Composable
fun AddPostContent(
    state: DialogState.AddPost,
    onDismissRequest: () -> Unit
) {
    var titulo by remember { mutableStateOf(state.tituloInicial) }
    var contenido by remember { mutableStateOf(state.contenidoInicial) }
    var visible by remember { mutableStateOf(state.visibleInicial) }

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

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismissRequest,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancelar", color = textColor)
            }
            Button(
                onClick = {
                    state.onSave(titulo, contenido, visible)
                    onDismissRequest()
                },
                enabled = titulo.isNotBlank() && contenido.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) {
                Text(if (state.postId == null) "Publicar" else "Guardar")
            }
        }
    }
}
