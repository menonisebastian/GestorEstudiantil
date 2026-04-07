package samf.gestorestudiantil.ui.panels.profesor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
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
import samf.gestorestudiantil.ui.components.AsignaturaCard
import samf.gestorestudiantil.ui.components.CustomSearchBar
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
    paddingValues: PaddingValues
) {
    val viewModel: ProfesorViewModel = viewModel()
    val state by viewModel.state.collectAsState()

    var selectedAsignatura by remember { mutableStateOf<Asignatura?>(null) }
    var selectedEstudiante by remember { mutableStateOf<User?>(null) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(profesor.id) {
        viewModel.cargarAsignaturas(profesor.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 20.dp)
    ) {
        if (selectedEstudiante != null && selectedAsignatura != null) {
            // Detalle de calificaciones del estudiante en la asignatura
            CalificacionesDetalleEstudiante(
                estudiante = selectedEstudiante!!,
                asignatura = selectedAsignatura!!,
                onBack = { selectedEstudiante = null },
                viewModel = viewModel
            )
        } else if (selectedAsignatura != null) {
            // Lista de estudiantes de la asignatura
            EstudiantesAsignaturaLista(
                asignatura = selectedAsignatura!!,
                estudiantes = state.estudiantes,
                searchText = searchText,
                onSearchChange = { searchText = it },
                onEstudianteClick = { selectedEstudiante = it },
                onBack = { selectedAsignatura = null },
                viewModel = viewModel
            )
        } else {
            // Selección de asignatura
            Text(
                text = "Selecciona una Asignatura",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.asignaturas) { asignatura ->
                    AsignaturaCard(asignatura, onClick = {
                        selectedAsignatura = asignatura
                        viewModel.cargarEstudiantesPorCurso(asignatura.cursoId)
                    })
                }
            }
        }
    }
}

// Eliminamos AsignaturaItem ya que usaremos AsignaturaCard de Composables.kt

@Composable
fun EstudiantesAsignaturaLista(
    asignatura: Asignatura,
    estudiantes: List<User>,
    searchText: String,
    onSearchChange: (String) -> Unit,
    onEstudianteClick: (User) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfesorViewModel
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = textColor)
            }
            Text(
                text = asignatura.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        CustomSearchBar(
            textoBusqueda = searchText,
            onValueChange = onSearchChange,
            onFilterClick = {}
        )

        Spacer(modifier = Modifier.height(16.dp))

        val filteredEstudiantes = estudiantes.filter {
            it.nombre.contains(searchText, ignoreCase = true)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredEstudiantes) { estudiante ->
                EstudianteCard(estudiante, asignatura.nombre, onEstudianteClick)
            }
        }
    }
}

@Composable
fun EstudianteCard(estudiante: User, materia: String, onClick: (User) -> Unit) {
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
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(estudiante.nombre, fontWeight = FontWeight.Bold, color = textColor)
                Text("${estudiante.cursoOArea} • $materia", fontSize = 11.sp, color = surfaceDimColor)
            }
            // Aquí se podría calcular el promedio real si se pasara
            Text(
                text = "8.5", // Placeholder promedio
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = primaryColor,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun CalificacionesDetalleEstudiante(
    estudiante: User,
    asignatura: Asignatura,
    onBack: () -> Unit,
    viewModel: ProfesorViewModel
) {
    val state by viewModel.state.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var evaluacionAEditar by remember { mutableStateOf<Evaluacion?>(null) }

    LaunchedEffect(estudiante.id, asignatura.id) {
        viewModel.cargarEvaluacionesEstudiante(estudiante.id, asignatura.id)
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = textColor)
            }
            Text(
                text = "Calificaciones: ${estudiante.nombre}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                evaluacionAEditar = Evaluacion(estudianteId = estudiante.id, asignaturaId = asignatura.id)
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir", tint = primaryColor)
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                fontWeight = FontWeight.Bold,
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
                        onClick = { onEdit(); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text(if (evaluacion.visible) "Ocultar" else "Mostrar") },
                        onClick = { onToggleVisibility(); expanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color.Red) },
                        onClick = { onDelete(); expanded = false }
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
        title = { Text(if (evaluacion.id.isEmpty()) "Añadir Calificación" else "Editar Calificación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre (e.g. Examen UD1)") }
                )
                OutlinedTextField(
                    value = nota,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) nota = it },
                    label = { Text("Nota") }
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
