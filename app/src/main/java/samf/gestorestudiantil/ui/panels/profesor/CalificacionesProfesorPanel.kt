package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.components.CustomFAB
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.AsignaturaCard
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.dialogs.EvaluacionProfesorItem
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

    val cursosOpciones = remember(state.todosMisEstudiantes) {
        state.todosMisEstudiantes.filterIsInstance<User.Estudiante>().map { it.curso }.distinct().sorted()
    }
    val asignaturasOpciones = remember(state.asignaturas) {
        state.asignaturas.map { it.acronimo }.distinct().sorted()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Contenido Principal
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> { // Pestaña Estudiantes
                    val filteredEstudiantes = state.todosMisEstudiantes.filter { it ->
                        val coincideBusqueda = it.nombre.contains(searchText, ignoreCase = true)
                        val cursosSeleccionados = filtroCurso.split(",").filter { it.isNotEmpty() }
                        val coincideCurso = if (cursosSeleccionados.isEmpty()) true else {
                            it is User.Estudiante && cursosSeleccionados.contains(it.curso)
                        }
                        val asignaturasSeleccionadas = filtroAsignatura.split(",").filter { it.isNotEmpty() }
                        val coincideAsignatura = if (asignaturasSeleccionadas.isEmpty()) true else {
                            it is User.Estudiante && state.asignaturas.any { asig -> 
                                asignaturasSeleccionadas.contains(asig.acronimo) && asig.cursoId == it.cursoId 
                            }
                        }
                        coincideBusqueda && coincideCurso && coincideAsignatura
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 220.dp, bottom = 120.dp, start = 20.dp, end = 20.dp)
                    ) {
                        items(filteredEstudiantes) { estudiante ->
                            val asignaturasSeleccionadas = filtroAsignatura.split(",").filter { it.isNotEmpty() }
                            val estudianteCursoId = (estudiante as? User.Estudiante)?.cursoId ?: ""
                            val asigDelProfesorParaEsteAlumno = if (asignaturasSeleccionadas.isNotEmpty()) {
                                state.asignaturas.find { asignaturasSeleccionadas.contains(it.acronimo) && it.cursoId == estudianteCursoId }
                            } else {
                                state.asignaturas.find { it.cursoId == estudianteCursoId }
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
                        val asignaturasSeleccionadas = filtroAsignatura.split(",").filter { it.isNotEmpty() }
                        val coincideAsignatura = if (asignaturasSeleccionadas.isEmpty()) true else {
                            asignaturasSeleccionadas.contains(it.acronimo)
                        }
                        val cursosSeleccionados = filtroCurso.split(",").filter { it.isNotEmpty() }
                        val coincideCurso = if (cursosSeleccionados.isEmpty()) true else {
                            cursosSeleccionados.any { curso -> it.cursoId.contains(curso, ignoreCase = true) }
                        }
                        coincideBusqueda && coincideAsignatura && coincideCurso
                    }
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(top = 220.dp, bottom = 120.dp, start = 20.dp, end = 20.dp)
                    ) {
                        items(filteredAsignaturas) { asignatura ->
                            AsignaturaCard(asignatura, userRole = "PROFESOR", onClick = {
                                onAsignaturaClick(asignatura)
                            })
                        }
                    }
                }
            }
        }

        // Cabezal Flotante (Título + Barra de Búsqueda + Tabs)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor.copy(alpha = 0.95f))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Gestión de Calificaciones",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor
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
                        onRemoveFilter = { keyPlusValue ->
                            val (key, newValue) = if (keyPlusValue.contains(":")) {
                                val parts = keyPlusValue.split(":")
                                parts[0] to parts[1]
                            } else {
                                keyPlusValue to ""
                            }
                            if (key == "curso") filtroCurso = newValue
                            if (key == "asignatura") filtroAsignatura = newValue
                        }
                    )
                }
            }

            // Pestañas (TabRow) - Se mantienen fuera de la Card pero dentro del área flotante
            SecondaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = textColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    val count = if (index == 0) state.todosMisEstudiantes.size else state.asignaturas.size
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) textColor else surfaceDimColor
                                )
                                if (count > 0) {
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 6.dp)
                                            .background(
                                                color = if (selectedTab == index) primaryColor.copy(alpha = 0.15f) else surfaceDimColor.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedTab == index) primaryColor else surfaceDimColor
                                        )
                                    }
                                }
                            }
                        }
                    )
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
) {
    var searchText by remember { mutableStateOf("") }
    Box(modifier = Modifier.fillMaxSize()) {
        val filteredEstudiantes = if (searchText.isEmpty()) {
            estudiantes
        } else {
            estudiantes.filter {
                it.nombre.contains(searchText, ignoreCase = true)
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 190.dp, bottom = 120.dp, start = 20.dp, end = 20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredEstudiantes) { estudiante ->
                EstudianteCard(
                    estudiante = estudiante,
                    materia = asignatura.nombre,
                    onImageClick = { onOpenDialog(DialogState.UserProfile(estudiante)) },
                    onClick = onEstudianteClick
                )
            }
        }

        // Header Flotante (Unificado con el resto de la app)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = asignatura.nombre,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = textColor
                        )
                        val turnoLetra = if (asignatura.turno.lowercase() == "matutino") "M" else "V"
                        val cursoAcronimo = asignatura.cursoId.substringAfterLast("_").uppercase()
                        Text(
                            text = "${asignatura.acronimo} $cursoAcronimo$turnoLetra${asignatura.cicloNum}",
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
            AccImg(
                userName = estudiante.nombre,
                imgUrl = estudiante.imgUrl,
                size = 50.dp,
                onClick = { onImageClick() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(estudiante.nombre, fontWeight = FontWeight.Bold, color = textColor)
                val infoAdicional = if (estudiante is User.Estudiante) estudiante.curso else ""
                Text("$infoAdicional • $materia", fontSize = 11.sp, color = surfaceDimColor)
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
    viewModel: ProfesorViewModel
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(estudiante.id, asignatura.id) {
        viewModel.cargarEvaluacionesEstudiante(estudiante.id, asignatura.id)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Contenido
        if (state.evaluaciones.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay calificaciones registradas", color = surfaceDimColor)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 112.dp, bottom = 120.dp, start = 20.dp, end = 20.dp)
            ) {
                items(state.evaluaciones) { eval ->
                    EvaluacionProfesorItem(
                        evaluacion = eval,
                        onEdit = {
                            onOpenDialog(
                                DialogState.AddEditCalificacion(
                                    evaluacion = eval,
                                    onSave = { viewModel.guardarEvaluacion(it) }
                                )
                            )
                        },
                        onDelete = { viewModel.eliminarEvaluacion(eval) },
                        onToggleVisibility = {
                            viewModel.guardarEvaluacion(eval.copy(visible = !eval.visible))
                        },
                        onDownload = eval.adjunto?.let { adjunto ->
                            { viewModel.descargarArchivo(adjunto.supabasePath, adjunto.nombreArchivo) }
                        }
                    )
                }
            }
        }

        // Header Flotante (Alumno + Media)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                AccImg(
                    userName = estudiante.nombre,
                    imgUrl = estudiante.imgUrl,
                    size = 48.dp,
                    onClick = { onOpenDialog(DialogState.UserProfile(estudiante)) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = estudiante.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    val turnoLetra = if (asignatura.turno.lowercase() == "matutino") "M" else "V"
                    val cursoAcronimo = asignatura.cursoId.substringAfterLast("_").uppercase()
                    Text(
                        text = "${asignatura.acronimo} $cursoAcronimo$turnoLetra${asignatura.cicloNum}",
                        fontSize = 11.sp,
                        color = surfaceDimColor
                    )
                }
                
                val notaMedia = if (state.evaluaciones.isNotEmpty()) state.evaluaciones.map { it.nota }.average() else 0.0
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "%.2f", notaMedia),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (notaMedia >= 5) Color(0xFF73BE77) else Color(0xFFD55047)
                    )
                    Text("MEDIA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = surfaceDimColor)
                }
            }
        }

        CustomFAB(
            onClick = {
                onOpenDialog(
                    DialogState.AddEditCalificacion(
                        evaluacion = Evaluacion(estudianteId = estudiante.id, asignaturaId = asignatura.id),
                        onSave = { viewModel.guardarEvaluacion(it) }
                    )
                )
            },
            text = "Añadir",
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 100.dp, end = 20.dp)
        )
    }
}
