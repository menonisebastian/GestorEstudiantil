package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.errorColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.domain.capitalize
import samf.gestorestudiantil.ui.theme.surfaceDimColor

@Composable
fun AsignarAsignaturasDialog(
    state: DialogState.AsignarAsignaturas,
    onDismissRequest: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val adminState by adminViewModel.adminState.collectAsState()

    // Filtros
    var cursoFiltro by remember { mutableStateOf("Todos los cursos") }
    var turnoFiltro by remember { mutableStateOf("Todos los turnos") }
    var cicloFiltro by remember { mutableStateOf("Todos los ciclos") }

    val cursosDisponibles = remember(adminState.cursos) {
        listOf("Todos los cursos") + adminState.cursos.map { it.nombre }
    }
    val turnosOpciones = listOf("Todos los turnos") + listOf("matutino", "vespertino").map { it.capitalize() }
    val ciclosDisponibles = listOf("Todos los ciclos", "Ciclo 1", "Ciclo 2")

    // Al abrir, cargamos las asignaturas del profesor y las que no tienen profesor
    LaunchedEffect(state.profesor.id) {
        adminViewModel.cargarAsignaturasSinProfesor("")
        // Aseguramos que tenemos cursos cargados para el filtro
        adminViewModel.cargarCursosPorCentro(state.profesor.centroId)
    }

    var asignaturasProfesor by remember { mutableStateOf<List<samf.gestorestudiantil.data.models.Asignatura>>(emptyList()) }

    // Obtenemos las asignaturas que ya tiene el profesor
    LaunchedEffect(adminState.usuarios) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("asignaturas")
            .whereEqualTo("profesorId", state.profesor.id)
            .get()
            .addOnSuccessListener { snapshot ->
                asignaturasProfesor = snapshot.toObjects(samf.gestorestudiantil.data.models.Asignatura::class.java)
            }
    }

    val asignaturasDisponiblesFiltradas = remember(adminState.asignaturasDisponibles, cursoFiltro, turnoFiltro, cicloFiltro) {
        adminState.asignaturasDisponibles.filter { asig ->
            val matchCurso = if (cursoFiltro == "Todos los cursos") true else {
                val cursoObj = adminState.cursos.find { it.nombre == cursoFiltro }
                asig.cursoId == cursoObj?.id
            }
            val matchTurno = if (turnoFiltro == "Todos los turnos") true else asig.turno.lowercase() == turnoFiltro.lowercase()
            val matchCiclo = if (cicloFiltro == "Todos los ciclos") true else {
                val cicloNum = if (cicloFiltro == "Ciclo 1") 1 else 2
                asig.cicloNum == cicloNum
            }
            matchCurso && matchTurno && matchCiclo
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Cerrar") }
        },
        containerColor = backgroundColor,
        title = {
            Text(text = "Asignaturas de ${state.profesor.nombre}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.height(550.dp)) {
                // Sección de Filtros
                Text(
                    "Filtrar disponibles",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = surfaceDimColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        CustomOptionsTextField(
                            texto = cursoFiltro,
                            onValueChange = { cursoFiltro = it },
                            opciones = cursosDisponibles,
                            label = "Curso"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        CustomOptionsTextField(
                            texto = turnoFiltro,
                            onValueChange = { turnoFiltro = it },
                            opciones = turnosOpciones,
                            label = "Turno"
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        CustomOptionsTextField(
                            texto = cicloFiltro,
                            onValueChange = { cicloFiltro = it },
                            opciones = ciclosDisponibles,
                            label = "Ciclo"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (asignaturasProfesor.isNotEmpty()) {
                    Text("Asignadas", fontWeight = FontWeight.Bold, color = primaryColor, modifier = Modifier.padding(vertical = 8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(asignaturasProfesor) { asig ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val turnoLetra = if (asig.turno.lowercase() == "matutino") "M" else "V"
                                        val cursoAcronimo = asig.cursoId.substringAfterLast("_").uppercase()
                                        Text("${asig.acronimo} $cursoAcronimo$turnoLetra${asig.cicloNum}", fontWeight = FontWeight.Bold, color = primaryColor)
                                        Text(asig.nombre, fontSize = 10.sp, color = textColor.copy(alpha = 0.7f))
                                    }
                                    IconButton(onClick = { 
                                        adminViewModel.desasignarAsignatura(asig.id, state.profesor.id)
                                        // Actualizar lista local para feedback inmediato
                                        asignaturasProfesor = asignaturasProfesor.filter { it.id != asig.id }
                                    }, colors = IconButtonDefaults.iconButtonColors(containerColor = errorColor.copy(alpha = 0.1f))) {
                                        Icon(Icons.Default.Remove, contentDescription = "Desasignar", tint = errorColor)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Disponibles (${asignaturasDisponiblesFiltradas.size})", fontWeight = FontWeight.Bold, color = tertiaryColor, modifier = Modifier.padding(bottom = 8.dp))
                
                if (adminState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = primaryColor)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(asignaturasDisponiblesFiltradas) { asig ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = tertiaryColor.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        val turnoLetra = if (asig.turno.lowercase() == "matutino") "M" else "V"
                                        val cursoAcronimo = asig.cursoId.substringAfterLast("_").uppercase()
                                        Text("${asig.acronimo} $cursoAcronimo$turnoLetra${asig.cicloNum}", fontWeight = FontWeight.Bold, color = tertiaryColor)
                                        Text(asig.nombre, fontSize = 10.sp, color = textColor.copy(alpha = 0.7f))
                                    }
                                    IconButton(onClick = { 
                                        adminViewModel.asignarAsignaturaAProfesor(asig.id, state.profesor.id)
                                        // Actualizar lista local
                                        asignaturasProfesor = asignaturasProfesor + asig
                                    }, colors = IconButtonDefaults.iconButtonColors(containerColor = primaryColor.copy(alpha = 0.1f))) {
                                        Icon(Icons.Default.Add, contentDescription = "Asignar", tint = primaryColor)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
