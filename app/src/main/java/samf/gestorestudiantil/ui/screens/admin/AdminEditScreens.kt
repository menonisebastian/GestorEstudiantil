package samf.gestorestudiantil.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.ColorPickerField
import samf.gestorestudiantil.ui.components.IconPickerField
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.textColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCentroScreen(
    state: DialogState.EditCentro,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.centro?.nombre ?: "") }
    var direccion by remember { mutableStateOf(state.centro?.direccion ?: "") }
    var tipo by remember { mutableStateOf(state.centro?.tipo ?: "") }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(if (state.centro == null) "Añadir Centro" else "Editar ${state.centro.nombre}", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
                windowInsets = WindowInsets(top = 0.dp)
            )
        },
        bottomBar = {
            Surface(color = backgroundColor, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier.weight(1f)
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = direccion,
                onValueChange = { direccion = it },
                label = { Text("Dirección") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = tipo,
                onValueChange = { tipo = it },
                label = { Text("Tipo (ej. Instituto de Educación Secundaria)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(if (state.curso == null) "Añadir Curso" else "Editar ${state.curso.acronimo}", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
                windowInsets = WindowInsets(top = 0.dp)
            )
        },
        bottomBar = {
            Surface(color = backgroundColor, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Cancelar") }
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
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar") }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = acronimo, onValueChange = { acronimo = it }, label = { Text("Acrónimo (ej. DAM)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = tipo, onValueChange = { tipo = it }, label = { Text("Tipo (ej. FP Grado Superior)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = modalidad, onValueChange = { modalidad = it }, label = { Text("Modalidad (presencial/dual)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = urlInfo, onValueChange = { urlInfo = it }, label = { Text("URL Info") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = horasTotalesCurso, onValueChange = { if (it.all { c -> c.isDigit() }) horasTotalesCurso = it }, label = { Text("Horas Totales") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = turnosStr, onValueChange = { turnosStr = it }, label = { Text("Turnos (separados por coma)") }, modifier = Modifier.fillMaxWidth())

            IconPickerField(value = iconoName, onValueChange = { iconoName = it })
            ColorPickerField(label = "Color Fondo Hex", value = colorFondoHex, onValueChange = { colorFondoHex = it })
            ColorPickerField(label = "Color Icono Hex", value = colorIconoHex, onValueChange = { colorIconoHex = it })
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAsignaturaScreen(
    state: DialogState.EditAsignatura,
    onBack: () -> Unit
) {
    var acronimo by remember { mutableStateOf(state.asignatura?.acronimo ?: "") }
    var nombre by remember { mutableStateOf(state.asignatura?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(state.asignatura?.descripcion ?: "") }
    var profesorId by remember { mutableStateOf(state.asignatura?.profesorId ?: "") }
    var ciclo by remember { mutableStateOf(state.asignatura?.ciclo ?: "1") }
    var cicloNum by remember { mutableStateOf(state.asignatura?.cicloNum?.toString() ?: "1") }
    var horasTotales by remember { mutableStateOf(state.asignatura?.horasTotales?.toString() ?: "0") }
    var horasSemanales by remember { mutableStateOf(state.asignatura?.horasSemanales?.toString() ?: "0") }
    var iconoName by remember { mutableStateOf(state.asignatura?.iconoName ?: "Class") }
    var colorFondoHex by remember { mutableStateOf(state.asignatura?.colorFondoHex ?: "#E8E8E8") }
    var colorIconoHex by remember { mutableStateOf(state.asignatura?.colorIconoHex ?: "#6B7280") }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(if (state.asignatura == null) "Añadir Asignatura" else "Editar ${state.asignatura.acronimo}", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
                windowInsets = WindowInsets(top = 0.dp)
            )
        },
        bottomBar = {
            Surface(color = backgroundColor, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(
                        onClick = {
                            val asignatura = state.asignatura?.copy(
                                acronimo = acronimo, nombre = nombre, descripcion = descripcion, profesorId = profesorId,
                                cursoId = state.cursoId, centroId = state.centroId,
                                ciclo = ciclo, cicloNum = cicloNum.toIntOrNull() ?: 1, horasTotales = horasTotales.toIntOrNull() ?: 0,
                                horasSemanales = horasSemanales.toIntOrNull() ?: 0, iconoName = iconoName,
                                colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex
                            ) ?: Asignatura(
                                cursoId = state.cursoId, centroId = state.centroId, acronimo = acronimo, nombre = nombre,
                                descripcion = descripcion, profesorId = profesorId, ciclo = ciclo, cicloNum = cicloNum.toIntOrNull() ?: 1,
                                horasTotales = horasTotales.toIntOrNull() ?: 0, horasSemanales = horasSemanales.toIntOrNull() ?: 0,
                                iconoName = iconoName, colorFondoHex = colorFondoHex, colorIconoHex = colorIconoHex
                            )
                            state.onSave(asignatura)
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar") }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = acronimo, onValueChange = { acronimo = it }, label = { Text("Acrónimo (ej. AD)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = descripcion, onValueChange = { descripcion = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = profesorId, onValueChange = { profesorId = it }, label = { Text("ID Profesor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = ciclo, onValueChange = { ciclo = it }, label = { Text("Ciclo (ej. 1, 2, único)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = cicloNum, onValueChange = { if (it.all { c -> c.isDigit() }) cicloNum = it }, label = { Text("Número de Ciclo (1 o 2)") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = horasTotales, onValueChange = { if (it.all { c -> c.isDigit() }) horasTotales = it }, label = { Text("Horas Totales") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = horasSemanales, onValueChange = { if (it.all { c -> c.isDigit() }) horasSemanales = it }, label = { Text("Horas Semanales") }, modifier = Modifier.fillMaxWidth())

            IconPickerField(value = iconoName, onValueChange = { iconoName = it })
            ColorPickerField(label = "Color Fondo Hex", value = colorFondoHex, onValueChange = { colorFondoHex = it })
            ColorPickerField(label = "Color Icono Hex", value = colorIconoHex, onValueChange = { colorIconoHex = it })
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUserScreen(
    state: DialogState.EditUser,
    onBack: () -> Unit
) {
    var nombre by remember { mutableStateOf(state.user.nombre) }
    var rol by remember { mutableStateOf(state.user.rol) }
    var cursoOArea by remember { mutableStateOf(state.user.cursoOArea) }
    var turno by remember { mutableStateOf(state.user.turno) }
    var cicloNum by remember { mutableStateOf(state.user.cicloNum.toString()) }

    val roles = listOf("ESTUDIANTE", "PROFESOR", "ADMIN")
    val turnos = listOf("matutino", "vespertino", "")

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Editar ${state.user.nombre}", color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor),
                windowInsets = WindowInsets(top = 0.dp)
            )
        },
        bottomBar = {
            Surface(color = backgroundColor, shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                    Button(
                        onClick = {
                            val updatedUser = state.user.copy(
                                nombre = nombre,
                                rol = rol,
                                cursoOArea = cursoOArea,
                                turno = turno,
                                cicloNum = cicloNum.toIntOrNull() ?: 1
                            )
                            state.onSave(updatedUser)
                            onBack()
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Guardar") }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())

            Text("Rol", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                roles.forEach { r ->
                    FilterChip(
                        selected = rol == r,
                        onClick = { rol = r },
                        label = { Text(r) }
                    )
                }
            }

            OutlinedTextField(value = cursoOArea, onValueChange = { cursoOArea = it }, label = { Text("Curso o Área") }, modifier = Modifier.fillMaxWidth())

            Text("Turno", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                turnos.forEach { t ->
                    FilterChip(
                        selected = turno == t,
                        onClick = { turno = t },
                        label = { Text(if(t.isEmpty()) "N/A" else t) }
                    )
                }
            }

            OutlinedTextField(
                value = cicloNum,
                onValueChange = { if (it.all { c -> c.isDigit() }) cicloNum = it },
                label = { Text("Ciclo (1 o 2)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
