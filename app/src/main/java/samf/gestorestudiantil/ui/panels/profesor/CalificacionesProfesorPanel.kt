package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.User
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import samf.gestorestudiantil.ui.components.AsignaturaCard
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.components.CustomTextField
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalificacionesProfesorPanel(
    profesor: User,
    paddingValues: PaddingValues,
    onOpenDialog: (DialogState) -> Unit,
    onAsignaturaClick: (Asignatura) -> Unit,
    onEstudianteClick: (User, Asignatura) -> Unit
) {
    val viewModel: ProfesorViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    var searchText by remember { mutableStateOf("") }
    var filtroCurso by remember { mutableStateOf("") }
    var filtroAsignatura by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Estudiantes", "Asignaturas")

    LaunchedEffect(profesor.id) {
        viewModel.cargarAsignaturas(profesor.id)
    }

    // Listas dinámicas para el filtro del profesor
    val cursosOpciones = remember(state.todosMisEstudiantes) {
        state.todosMisEstudiantes.map { it.cursoOArea }.distinct().sorted()
    }
    val asignaturasOpciones = remember(state.asignaturas) {
        state.asignaturas.map { it.acronimo }.distinct().sorted()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "Gestión de Calificaciones",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        CustomSearchBar(
            textoBusqueda = searchText,
            onValueChange = { searchText = it },
            onFilterClick = {
                onOpenDialog(
                    DialogState.Filter(
                        tipo = "Calificaciones",
                        currentFilters = buildMap {
                            if (filtroCurso.isNotEmpty()) put("curso", filtroCurso)
                            if (filtroAsignatura.isNotEmpty()) put("asignatura", filtroAsignatura)
                        },
                        opcionesPersonalizadas = mapOf(
                            "cursos" to cursosOpciones,
                            "asignaturas" to asignaturasOpciones
                        ),
                        onApply = { seleccion ->
                            filtroCurso = seleccion["curso"] ?: ""
                            filtroAsignatura = seleccion["asignatura"] ?: ""
                        }
                    )
                )
            },
            filters = buildMap {
                if (filtroCurso.isNotEmpty()) put("curso", filtroCurso)
                if (filtroAsignatura.isNotEmpty()) put("asignatura", filtroAsignatura)
            },
            onRemoveFilter = { key ->
                if (key == "curso") filtroCurso = ""
                if (key == "asignatura") filtroAsignatura = ""
            }
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = primaryColor,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = primaryColor
                )
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) primaryColor else surfaceDimColor
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (selectedTab) {
            0 -> { // Pestaña Estudiantes
                val filteredEstudiantes = state.todosMisEstudiantes.filter {
                    val coincideBusqueda = it.nombre.contains(searchText, ignoreCase = true)
                    val coincideCurso = filtroCurso.isEmpty() || it.cursoOArea == filtroCurso

                    // Si hay filtro de asignatura, el estudiante debe pertenecer al curso de esa asignatura
                    val coincideAsignatura = if (filtroAsignatura.isEmpty()) true else {
                        state.asignaturas.any { asig -> asig.acronimo == filtroAsignatura && asig.cursoId == it.cursoId }
                    }

                    coincideBusqueda && coincideCurso && coincideAsignatura
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredEstudiantes) { estudiante ->
                        // En esta pestaña, necesitamos saber qué asignatura del profesor dar a este alumno
                        // Si hay filtro de asignatura, usamos esa. Si no, la primera que coincida con el curso del alumno.
                        val asigDelProfesorParaEsteAlumno = if (filtroAsignatura.isNotEmpty()) {
                            state.asignaturas.find { it.acronimo == filtroAsignatura && it.cursoId == estudiante.cursoId }
                        } else {
                            state.asignaturas.find { it.cursoId == estudiante.cursoId }
                        }

                        EstudianteCard(
                            estudiante = estudiante,
                            materia = asigDelProfesorParaEsteAlumno?.nombre ?: "Sin asignatura común",
                            onImageClick = { onOpenDialog(DialogState.UserProfile(estudiante)) },
                            onClick = {
                                if (asigDelProfesorParaEsteAlumno != null) {
                                    onEstudianteClick(estudiante, asigDelProfesorParaEsteAlumno)
                                }
                            }
                        )
                    }
                }
            }
            1 -> { // Pestaña Asignaturas
                val filteredAsignaturas = state.asignaturas.filter {
                    val coincideBusqueda = it.nombre.contains(searchText, ignoreCase = true) || it.acronimo.contains(searchText, ignoreCase = true)
                    val coincideAsignatura = filtroAsignatura.isEmpty() || it.acronimo == filtroAsignatura

                    // En la pestaña de asignaturas, el filtro de curso se aplica al cursoId/acrónimo del curso
                    // Nota: state.asignaturas ya está filtrado por profesorId en el ViewModel
                    val coincideCurso = filtroCurso.isEmpty() || it.cursoId.contains(filtroCurso) // O una comparación más exacta si se tiene el acrónimo del curso en Asignatura

                    coincideBusqueda && coincideAsignatura && coincideCurso
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredAsignaturas) { asignatura ->
                        AsignaturaCard(asignatura, onClick = {
                            onAsignaturaClick(asignatura)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun EstudiantesAsignaturaLista(
    asignatura: Asignatura,
    estudiantes: List<User>,
    onEstudianteClick: (User) -> Unit,
    onOpenDialog: (DialogState) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfesorViewModel
) {
    var searchText by remember { mutableStateOf("") }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Volver", tint = textColor)
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = asignatura.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                val turnoLetra = if (asignatura.turno.lowercase() == "matutino") "M" else "V"
                val cursoAcronimo = try { asignatura.idFirestore.substringAfter("_").substringBefore("_").uppercase() } catch (e: Exception) { "" }
                Text(
                    text = "$cursoAcronimo - [$cursoAcronimo$turnoLetra${asignatura.cicloNum}]",
                    fontSize = 12.sp,
                    color = surfaceDimColor
                )
            }
        }

        CustomSearchBar(
            textoBusqueda = searchText,
            onValueChange = { searchText = it },
            onFilterClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        val filteredEstudiantes = if (searchText.isEmpty()) {
            estudiantes
        } else {
            estudiantes.filter {
                it.nombre.contains(searchText, ignoreCase = true)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredEstudiantes) { estudiante ->
                EstudianteCard(
                    estudiante = estudiante,
                    materia = asignatura.nombre,
                    onImageClick = { onOpenDialog(DialogState.UserProfile(estudiante)) },
                    onClick = onEstudianteClick
                )
            }
        }
    }
}

@Composable
fun EstudianteCard(
    estudiante: User,
    materia: String,
    onImageClick: () -> Unit = {},
    onClick: (User) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(estudiante) },
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = estudiante.imgUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
                    .clickable { onImageClick() },
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(estudiante.nombre, fontWeight = FontWeight.Bold, color = textColor)
                Text("${estudiante.cursoOArea} • $materia", fontSize = 11.sp, color = surfaceDimColor)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = surfaceDimColor)
        }
    }
}

@Composable
fun CalificacionesDetalleEstudiante(
    estudiante: User,
    asignatura: Asignatura,
    onOpenDialog: (DialogState) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfesorViewModel
) {
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var evaluacionAEditar by remember { mutableStateOf<Evaluacion?>(null) }

    LaunchedEffect(estudiante.id, asignatura.idFirestore) {
        viewModel.cargarEvaluacionesEstudiante(estudiante.id, asignatura.idFirestore)
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    evaluacionAEditar = Evaluacion(estudianteId = estudiante.id, asignaturaId = asignatura.idFirestore)
                    showDialog = true
                },
                containerColor = primaryColor
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir", tint = textColor)
            }
        }
    ) { _ -> // Ignoramos paddingValues del Scaffold interno para reducir el espacio superior
        Column(modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Volver", tint = textColor)
                }
                AsyncImage(
                    model = estudiante.imgUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable { onOpenDialog(DialogState.UserProfile(estudiante)) },
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(8.dp))
                Spacer(modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = estudiante.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    val turnoLetra = if (asignatura.turno.lowercase() == "matutino") "M" else "V"
                    val cursoAcronimo = asignatura.cursoId.substringAfterLast("_").uppercase()
                    Text(
                        text = "${asignatura.nombre} - [$cursoAcronimo$turnoLetra${asignatura.cicloNum}]",
                        fontSize = 12.sp,
                        color = surfaceDimColor
                    )
                }
            }

            if (state.evaluaciones.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay calificaciones registradas", color = surfaceDimColor)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(state.evaluaciones) { eval ->
                        EvaluacionProfesorItem(
                            evaluacion = eval,
                            onEdit = {
                                evaluacionAEditar = eval
                                showDialog = true
                            },
                            onDelete = { viewModel.eliminarEvaluacion(eval) },
                            onToggleVisibility = {
                                viewModel.guardarEvaluacion(eval.copy(visible = !eval.visible))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AddEditCalificacionDialog(
            evaluacion = evaluacionAEditar!!,
            onDismiss = { showDialog = false },
            onSave = {
                viewModel.guardarEvaluacion(it)
                showDialog = false
            }
        )
    }
}

@Composable
fun EvaluacionProfesorItem(
    evaluacion: Evaluacion,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleVisibility: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(evaluacion.nombre, fontWeight = FontWeight.Bold, color = textColor)
                Text(evaluacion.tipoEvaluacion.label, fontSize = 11.sp, color = surfaceDimColor)
            }
            Text(
                text = evaluacion.nota.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (evaluacion.nota >= 5) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            var expanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = surfaceDimColor)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = { onEdit(); expanded = false },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text(if (evaluacion.visible) "Ocultar" else "Mostrar") },
                        onClick = { onToggleVisibility(); expanded = false },
                        leadingIcon = { Icon(if (evaluacion.visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color.Red) },
                        onClick = { onDelete(); expanded = false },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

@Composable
fun AddEditCalificacionDialog(
    evaluacion: Evaluacion,
    onDismiss: () -> Unit,
    onSave: (Evaluacion) -> Unit
) {
    var nombre by remember { mutableStateOf(evaluacion.nombre) }
    var nota by remember { mutableStateOf(evaluacion.nota.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (evaluacion.id.isEmpty()) "Nueva Calificación" else "Editar Calificación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CustomTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = "Nombre del trabajo/examen",
                    modifier = Modifier.fillMaxWidth()
                )
                CustomTextField(
                    value = nota,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) nota = it },
                    label = "Nota (0.0 - 10.0)",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(evaluacion.copy(nombre = nombre, nota = nota.toDoubleOrNull() ?: 0.0))
            }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
