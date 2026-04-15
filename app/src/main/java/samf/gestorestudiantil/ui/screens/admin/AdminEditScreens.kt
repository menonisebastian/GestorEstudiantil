package samf.gestorestudiantil.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.ColorPickerField
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.components.IconPickerField
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun EditCentroScreen(
    state: DialogState.EditCentro,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.centro?.nombre ?: "") }
    var direccion by remember { mutableStateOf(state.centro?.direccion ?: "") }
    var tipo by remember { mutableStateOf(state.centro?.tipo ?: "") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (state.centro == null) "Añadir Centro" else "Editar Centro",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                state.centro?.let {
                    Text(
                        text = it.nombre,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            if (state.centro != null) {
                OutlinedButton(
                    onClick = {
                        state.onDelete?.invoke(state.centro)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar Centro")
                }
            }
            CustomTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = "Nombre"
            )
            CustomTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = "Dirección"
            )
            CustomOptionsTextField(
                texto = tipo,
                onValueChange = { tipo = it },
                opciones = listOf("IES", "Centro Privado", "Centro Concertado", "Universidad"),
                label = "Tipo"
            )
            Spacer(modifier = Modifier.height(180.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val centro = state.centro?.copy(nombre = nombre, direccion = direccion, tipo = tipo)
                            ?: Centro(nombre = nombre, direccion = direccion, tipo = tipo)
                        state.onSave(centro)
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Guardar", color = textColor)
                }
            }
        }
    }
}

@Composable
fun EditCursoScreen(
    state: DialogState.EditCurso,
    onBack: () -> Unit
) {
    var acronimo by remember { mutableStateOf(state.curso?.acronimo ?: "") }
    var nombre by remember { mutableStateOf(state.curso?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(state.curso?.descripcion ?: "") }
    var tipo by remember { mutableStateOf(state.curso?.tipo ?: "") }
    var modalidad by remember { mutableStateOf(state.curso?.modalidad ?: "presencial") }
    var urlInfo by remember { mutableStateOf(state.curso?.urlInfo ?: "") }
    var horasTotalesCurso by remember { mutableStateOf(state.curso?.horasTotalesCurso?.toString() ?: "0") }
    var iconoName by remember { mutableStateOf(state.curso?.iconoName ?: "School") }
    var colorFondoHex by remember { mutableStateOf(state.curso?.colorFondoHex ?: "#D0E1FF") }
    var colorIconoHex by remember { mutableStateOf(state.curso?.colorIconoHex ?: "#2563EB") }
    var turnosStr by remember { mutableStateOf(state.curso?.turnosDisponibles?.joinToString(", ") ?: "") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (state.curso == null) "Añadir Curso" else "Editar Curso",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                state.curso?.let {
                    Text(
                        text = it.acronimo,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            if (state.curso != null) {
                OutlinedButton(
                    onClick = {
                        state.onDelete?.invoke(state.curso)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor)
                ) {
                    Text("Eliminar Curso")
                }
            }
            CustomTextField(value = acronimo, onValueChange = { acronimo = it }, label = "Acrónimo (ej. DAM)")
            CustomTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre Completo")
            CustomTextField(value = descripcion, onValueChange = { descripcion = it }, label = "Descripción", singleLine = false, minLines = 2)
            CustomOptionsTextField(
                texto = tipo,
                onValueChange = { tipo = it },
                opciones = listOf("FP Grado Medio", "FP Grado Superior", "Bachillerato", "ESO"),
                label = "Tipo"
            )
            CustomOptionsTextField(
                texto = modalidad,
                onValueChange = { modalidad = it },
                opciones = listOf("presencial", "dual", "online"),
                label = "Modalidad"
            )
            CustomTextField(value = urlInfo, onValueChange = { urlInfo = it }, label = "URL Info")
            CustomTextField(value = horasTotalesCurso, onValueChange = { if (it.all { c -> c.isDigit() }) horasTotalesCurso = it }, label = "Horas Totales")
            CustomTextField(value = turnosStr, onValueChange = { turnosStr = it }, label = "Turnos (separados por coma)")

            IconPickerField(value = iconoName, onValueChange = { iconoName = it })
            ColorPickerField(label = "Color Fondo Hex", value = colorFondoHex, onValueChange = { colorFondoHex = it })
            ColorPickerField(label = "Color Icono Hex", value = colorIconoHex, onValueChange = { colorIconoHex = it })
            Spacer(modifier = Modifier.height(180.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val turnosList = turnosStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val curso = state.curso?.copy(
                            acronimo = acronimo, nombre = nombre, descripcion = descripcion, centroId = state.centroId, tipo = tipo,
                            modalidad = modalidad, urlInfo = urlInfo, horasTotalesCurso = horasTotalesCurso.toIntOrNull() ?: 0,
                            iconoName = iconoName, colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex,
                            turnosDisponibles = turnosList
                        ) ?: Curso(
                            centroId = state.centroId, acronimo = acronimo, nombre = nombre, descripcion = descripcion, tipo = tipo,
                            modalidad = modalidad, urlInfo = urlInfo, horasTotalesCurso = horasTotalesCurso.toIntOrNull() ?: 0,
                            iconoName = iconoName, colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex,
                            turnosDisponibles = turnosList
                        )
                        state.onSave(curso)
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Guardar", color = textColor)
                }
            }
        }
    }
}

@Composable
fun EditAsignaturaScreen(
    state: DialogState.EditAsignatura,
    onBack: () -> Unit
) {
    var acronimo by remember { mutableStateOf(state.asignatura?.acronimo ?: "") }
    var nombre by remember { mutableStateOf(state.asignatura?.nombre ?: "") }
    var departamento by remember { mutableStateOf(state.asignatura?.departamento ?: "") }
    var descripcion by remember { mutableStateOf(state.asignatura?.descripcion ?: "") }
    var profesorId by remember { mutableStateOf(state.asignatura?.profesorId ?: "") }
    var ciclo by remember { mutableStateOf(state.asignatura?.ciclo ?: "1") }
    var cicloNum by remember { mutableStateOf(state.asignatura?.cicloNum?.toString() ?: "1") }
    var horasTotales by remember { mutableStateOf(state.asignatura?.horasTotales?.toString() ?: "0") }
    var horasSemanales by remember { mutableStateOf(state.asignatura?.horasSemanales?.toString() ?: "0") }
    var iconoName by remember { mutableStateOf(state.asignatura?.iconoName ?: "Class") }
    var colorFondoHex by remember { mutableStateOf(state.asignatura?.colorFondoHex ?: "#E8E8E8") }
    var colorIconoHex by remember { mutableStateOf(state.asignatura?.colorIconoHex ?: "#6B7280") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (state.asignatura == null) "Añadir Asignatura" else "Editar Asignatura",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                state.asignatura?.let {
                    Text(
                        text = it.acronimo,
                        fontSize = 12.sp,
                        color = textColor.copy(alpha = 0.7f)
                    )
                }
            }

            if (state.asignatura != null) {
                OutlinedButton(
                    onClick = {
                        state.onDelete?.invoke(state.asignatura)
                        onBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor)
                ) {
                    Text("Eliminar Asignatura")
                }
            }
            CustomTextField(value = acronimo, onValueChange = { acronimo = it }, label = "Acrónimo (ej. AD)")
            CustomTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre Completo")
            CustomOptionsTextField(
                texto = departamento,
                onValueChange = { departamento = it },
                opciones = listOf(
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
                ),
                label = "Departamento"
            )
            CustomTextField(value = descripcion, onValueChange = { descripcion = it }, label = "Descripción", singleLine = false, minLines = 2)
            CustomTextField(value = profesorId, onValueChange = { profesorId = it }, label = "ID Profesor")
            CustomOptionsTextField(
                texto = ciclo,
                onValueChange = { ciclo = it },
                opciones = listOf("1", "2", "único"),
                label = "Ciclo"
            )
            CustomOptionsTextField(
                texto = cicloNum,
                onValueChange = { cicloNum = it },
                opciones = listOf("1", "2"),
                label = "Número de Ciclo"
            )
            CustomTextField(value = horasTotales, onValueChange = { if (it.all { c -> c.isDigit() }) horasTotales = it }, label = "Horas Totales")
            CustomTextField(value = horasSemanales, onValueChange = { if (it.all { c -> c.isDigit() }) horasSemanales = it }, label = "Horas Semanales")

            IconPickerField(value = iconoName, onValueChange = { iconoName = it })
            ColorPickerField(label = "Color Fondo Hex", value = colorFondoHex, onValueChange = { colorFondoHex = it })
            ColorPickerField(label = "Color Icono Hex", value = colorIconoHex, onValueChange = { colorIconoHex = it })
            Spacer(modifier = Modifier.height(180.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val asignatura = state.asignatura?.copy(
                            acronimo = acronimo, nombre = nombre, departamento = departamento, descripcion = descripcion, profesorId = profesorId,
                            cursoId = state.cursoId, centroId = state.centroId,
                            ciclo = ciclo, cicloNum = cicloNum.toIntOrNull() ?: 1, horasTotales = horasTotales.toIntOrNull() ?: 0,
                            horasSemanales = horasSemanales.toIntOrNull() ?: 0, iconoName = iconoName,
                            colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex
                        ) ?: Asignatura(
                            cursoId = state.cursoId, centroId = state.centroId, acronimo = acronimo, nombre = nombre,
                            departamento = departamento,
                            descripcion = descripcion, profesorId = profesorId, ciclo = ciclo, cicloNum = cicloNum.toIntOrNull() ?: 1,
                            horasTotales = horasTotales.toIntOrNull() ?: 0, horasSemanales = horasSemanales.toIntOrNull() ?: 0,
                            iconoName = iconoName, colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex
                        )
                        state.onSave(asignatura)
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Guardar", color = textColor)
                }
            }
        }
    }
}

@Composable
fun EditUserScreen(
    state: DialogState.EditUser,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.user.nombre) }
    var rol by remember { mutableStateOf(state.user.rol) }
    var cursoId by remember { mutableStateOf(if (state.user is User.Estudiante) state.user.cursoId else "") }
    var cursoInput by remember { mutableStateOf(if (state.user is User.Estudiante) state.user.curso else "") }
    var turno by remember { mutableStateOf(if (state.user is User.Estudiante) state.user.turno else "") }
    var cicloNum by remember { mutableStateOf(if (state.user is User.Estudiante) state.user.cicloNum.toString() else "1") }
    var departamento by remember { mutableStateOf(if (state.user is User.Profesor) state.user.departamento else "") }

    val roles = listOf("ESTUDIANTE", "PROFESOR", "ADMIN")
    val turnos = listOf("matutino", "vespertino")
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

    val acronimosCursos = remember(state.cursos) { state.cursos.map { it.acronimo } }

    // Lógica para autogenerar el acrónimo del curso
    LaunchedEffect(cursoId, turno, cicloNum, rol) {
        if (rol == "ESTUDIANTE") {
            val cursoObj = state.cursos.find { it.id == cursoId }
            if (cursoObj != null) {
                val letraTurno = if (turno.lowercase().contains("matutino")) "M" else "V"
                cursoInput = "${cursoObj.acronimo}${letraTurno}${cicloNum}"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Editar Usuario",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = state.user.nombre,
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }

            CustomTextField(value = nombre, onValueChange = { nombre = it }, label = "Nombre")

            Text("Rol", style = MaterialTheme.typography.labelLarge)
            CustomOptionsTextField(
                texto = rol,
                onValueChange = { rol = it },
                opciones = roles,
                label = "Rol"
            )

            if (rol == "ESTUDIANTE") {
                Text("Turno", style = MaterialTheme.typography.labelLarge)
                CustomOptionsTextField(
                    texto = turno,
                    onValueChange = { turno = it },
                    opciones = turnos,
                    label = "Turno"
                )

                Text("Curso Base", style = MaterialTheme.typography.labelLarge)
                CustomOptionsTextField(
                    texto = state.cursos.find { it.id == cursoId }?.acronimo ?: "",
                    onValueChange = { acro ->
                        cursoId = state.cursos.find { it.acronimo == acro }?.id ?: ""
                    },
                    opciones = acronimosCursos,
                    label = "Curso Base"
                )

                Text("Ciclo", style = MaterialTheme.typography.labelLarge)
                CustomOptionsTextField(
                    texto = cicloNum,
                    onValueChange = { cicloNum = it },
                    opciones = listOf("1", "2"),
                    label = "Ciclo (1 o 2)"
                )

                CustomTextField(
                    value = cursoInput,
                    onValueChange = { cursoInput = it },
                    label = "Acrónimo Final (Curso)",
                    readOnly = true
                )
            }

            if (rol == "PROFESOR") {
                Text("Turno", style = MaterialTheme.typography.labelLarge)
                CustomOptionsTextField(
                    texto = turno,
                    onValueChange = { turno = it },
                    opciones = turnos,
                    label = "Turno"
                )

                Text("Departamento", style = MaterialTheme.typography.labelLarge)
                CustomOptionsTextField(
                    texto = departamento,
                    onValueChange = { departamento = it },
                    opciones = departamentos,
                    label = "Departamento"
                )
            }

            Spacer(modifier = Modifier.height(180.dp))
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val updatedUser = when (state.user) {
                            is User.Estudiante -> state.user.copy(
                                nombre = nombre,
                                rol = rol,
                                turno = turno,
                                cicloNum = cicloNum.toIntOrNull() ?: 1,
                                cursoId = cursoId,
                                curso = cursoInput
                            )
                            is User.Profesor -> state.user.copy(
                                nombre = nombre,
                                rol = rol,
                                departamento = departamento,
                                turno = turno
                            )
                            is User.Admin -> state.user.copy(
                                nombre = nombre,
                                rol = rol
                            )
                            is User.Incompleto -> state.user.copy(
                                nombre = nombre,
                                rol = rol
                            )
                        }
                        state.onSave(updatedUser)
                        onBack()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text("Guardar", color = textColor)
                }
            }
        }
    }
}
