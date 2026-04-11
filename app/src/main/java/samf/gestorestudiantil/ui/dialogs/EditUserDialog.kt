package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun EditUserDialog(
    state: DialogState.EditUser,
    onDismissRequest: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.user.nombre) }
    var email by remember { mutableStateOf(state.user.email) }
    var rol by remember { mutableStateOf(state.user.rol) }
    var turno by remember { mutableStateOf(state.user.turno) }
    var ciclo by remember { mutableIntStateOf(state.user.cicloNum) }
    var cursoId by remember { mutableStateOf(state.user.cursoId) }
    var cursoOArea by remember { mutableStateOf(state.user.cursoOArea) }

    val roles = listOf("ESTUDIANTE", "PROFESOR", "ADMIN")
    val turnos = listOf("matutino", "vespertino")
    val ciclos = listOf("1", "2")

    val acronimosCursos = remember(state.cursos) { state.cursos.map { it.acronimo } }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = backgroundColor,
        title = {
            Text(
                text = "Editar Usuario",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CustomTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre Completo",
                    icon = Icons.Outlined.Person
                )

                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    icon = Icons.Outlined.Email,
                    readOnly = true // El email suele ser el identificador único/login
                )

                CustomOptionsTextField(
                    texto = rol,
                    onValueChange = { rol = it },
                    opciones = roles,
                    label = "Rol",
                    icon = Icons.Outlined.Badge
                )

                if (rol == "ESTUDIANTE" || rol == "PROFESOR") {
                    CustomOptionsTextField(
                        texto = turno,
                        onValueChange = { 
                            turno = it 
                            // Actualizar cursoOArea cuando cambia el turno
                            if (rol == "ESTUDIANTE") {
                                val acronimo = state.cursos.find { it.id == cursoId }?.acronimo ?: cursoOArea.takeWhile { !it.isDigit() && it != 'M' && it != 'V' }
                                val letraTurno = if (it.lowercase().contains("matutino")) "M" else "V"
                                cursoOArea = "${acronimo}${letraTurno}${ciclo}"
                            }
                        },
                        opciones = turnos,
                        label = "Turno",
                        icon = Icons.Outlined.Schedule
                    )

                    if (rol == "ESTUDIANTE" && state.cursos.isNotEmpty()) {
                        CustomOptionsTextField(
                            texto = state.cursos.find { it.id == cursoId }?.acronimo ?: cursoOArea,
                            onValueChange = { acronimo ->
                                val cursoSeleccionado = state.cursos.find { it.acronimo == acronimo }
                                cursoSeleccionado?.let {
                                    cursoId = it.id
                                    val letraTurno = if (turno.lowercase().contains("matutino")) "M" else "V"
                                    cursoOArea = "${it.acronimo}${letraTurno}${ciclo}"
                                }
                            },
                            opciones = acronimosCursos,
                            label = "Curso",
                            icon = Icons.Outlined.School
                        )
                    } else {
                        CustomTextField(
                            value = cursoOArea,
                            onValueChange = { cursoOArea = it },
                            label = if (rol == "ESTUDIANTE") "Curso" else "Departamento / Área",
                            icon = Icons.Outlined.School
                        )
                    }
                }

                if (rol == "ESTUDIANTE") {
                    CustomOptionsTextField(
                        texto = ciclo.toString(),
                        onValueChange = { 
                            ciclo = it.toInt() 
                            // Actualizar cursoOArea cuando cambia el ciclo
                            val acronimo = state.cursos.find { it.id == cursoId }?.acronimo ?: cursoOArea.takeWhile { !it.isDigit() && it != 'M' && it != 'V' }
                            val letraTurno = if (turno.lowercase().contains("matutino")) "M" else "V"
                            cursoOArea = "${acronimo}${letraTurno}${ciclo}"
                        },
                        opciones = ciclos,
                        label = "Ciclo",
                        icon = Icons.Outlined.Groups
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedUser = state.user.copy(
                        nombre = nombre,
                        rol = rol,
                        turno = turno,
                        cicloNum = ciclo,
                        cursoId = cursoId,
                        cursoOArea = cursoOArea
                    )
                    state.onSave(updatedUser)
                    onDismissRequest()
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancelar", color = textColor)
            }
        }
    )
}
