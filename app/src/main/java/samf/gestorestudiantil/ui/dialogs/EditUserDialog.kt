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
import androidx.compose.runtime.LaunchedEffect
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

    // Campos de Estudiante
    var turno by remember {
        mutableStateOf(
            when(state.user) {
                is User.Estudiante -> state.user.turno
                is User.Profesor -> state.user.turno
                else -> "matutino"
            }
        )
    }
    var ciclo by remember { 
        mutableIntStateOf(if (state.user is User.Estudiante) state.user.cicloNum else 1) 
    }
    var cursoId by remember { 
        mutableStateOf(if (state.user is User.Estudiante) state.user.cursoId else "") 
    }
    var cursoInput by remember { 
        mutableStateOf(if (state.user is User.Estudiante) state.user.curso else "") 
    }

    // Campo de Profesor
    var departamento by remember { 
        mutableStateOf(if (state.user is User.Profesor) state.user.departamento else "") 
    }

    val roles = listOf("ESTUDIANTE", "PROFESOR", "ADMIN")
    val turnos = listOf("matutino", "vespertino")
    val ciclos = listOf("1", "2")
    val departamentos = listOf(
        "Actividades complementarias y extraescolares",
        "Administración y Gestión",
        "Artes plásticas",
        "Biología y geología",
        "Clásicas",
        "Comercio y marketing",
        "Economía",
        "Educación física",
        "Filosofía",
        "Física y química",
        "Formación y orientación laboral",
        "Francés",
        "Geografía e historia",
        "Informática",
        "Inglés",
        "Lengua y literatura",
        "Matemáticas",
        "Música",
        "Orientación",
        "Tecnología"
    )

    // Lógica para autogenerar el acrónimo del curso
    LaunchedEffect(cursoId, turno, ciclo, rol) {
        if (rol == "ESTUDIANTE") {
            val cursoObj = state.cursos.find { it.id == cursoId }
            if (cursoObj != null) {
                val letraTurno = if (turno.lowercase().contains("matutino")) "M" else "V"
                // Solo añadir ciclo si el curso tiene más de uno (o siempre, según prefieras)
                // Aquí asumo que si existe ciclo, se añade
                cursoInput = "${cursoObj.acronimo}${letraTurno}${ciclo}"
            }
        }
    }

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
                    readOnly = true
                )

                CustomOptionsTextField(
                    texto = rol,
                    onValueChange = { rol = it },
                    opciones = roles,
                    label = "Rol",
                    icon = Icons.Outlined.Badge
                )

                if (rol == "ESTUDIANTE") {

                    val acronimosCursos = state.cursos.map { it.acronimo }
                    CustomOptionsTextField(
                        texto = state.cursos.find { it.id == cursoId }?.acronimo ?: "",
                        onValueChange = { acro ->
                            cursoId = state.cursos.find { it.acronimo == acro }?.id ?: ""
                        },
                        opciones = acronimosCursos,
                        label = "Curso Base",
                        icon = Icons.Outlined.School
                    )

                    CustomOptionsTextField(
                        texto = ciclo.toString(),
                        onValueChange = { ciclo = it.toInt() },
                        opciones = ciclos,
                        label = "Ciclo",
                        icon = Icons.Outlined.Groups
                    )

                    CustomTextField(
                        value = cursoInput,
                        onValueChange = { cursoInput = it },
                        label = "Acrónimo Final (Curso)",
                        icon = Icons.Outlined.School,
                        readOnly = true // Se autogenera
                    )
                }

                if (rol == "ESTUDIANTE" || rol == "PROFESOR") {
                    CustomOptionsTextField(
                        texto = turno,
                        onValueChange = { turno = it },
                        opciones = turnos,
                        label = "Turno",
                        icon = Icons.Outlined.Schedule
                    )
                }

                if (rol == "PROFESOR") {
                    CustomOptionsTextField(
                        texto = departamento,
                        onValueChange = { departamento = it },
                        opciones = departamentos,
                        label = "Departamento",
                        icon = Icons.Outlined.School
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedUser = when {
                        rol == "ESTUDIANTE" -> {
                            User.Estudiante(
                                id = state.user.id,
                                nombre = nombre,
                                email = email,
                                centroId = state.user.centroId,
                                estado = state.user.estado,
                                imgUrl = state.user.imgUrl,
                                fcmToken = state.user.fcmToken,
                                rol = "ESTUDIANTE",
                                turno = turno,
                                cicloNum = ciclo,
                                cursoId = cursoId,
                                curso = cursoInput
                            )
                        }
                        rol == "PROFESOR" -> {
                            User.Profesor(
                                id = state.user.id,
                                nombre = nombre,
                                email = email,
                                centroId = state.user.centroId,
                                estado = state.user.estado,
                                imgUrl = state.user.imgUrl,
                                fcmToken = state.user.fcmToken,
                                rol = "PROFESOR",
                                departamento = departamento,
                                turno = turno,
                                asignaturasImpartidas = if (state.user is User.Profesor) state.user.asignaturasImpartidas else emptyList(),
                                ultimaVezAsignaturas = if (state.user is User.Profesor) state.user.ultimaVezAsignaturas else emptyMap()
                            )
                        }
                        else -> {
                            User.Admin(
                                id = state.user.id,
                                nombre = nombre,
                                email = email,
                                centroId = state.user.centroId,
                                estado = state.user.estado,
                                imgUrl = state.user.imgUrl,
                                fcmToken = state.user.fcmToken,
                                rol = "ADMIN"
                            )
                        }
                    }
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
