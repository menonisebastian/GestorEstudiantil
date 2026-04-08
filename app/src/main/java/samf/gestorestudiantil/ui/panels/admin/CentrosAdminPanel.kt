package samf.gestorestudiantil.ui.panels.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.ui.components.AsignaturaCard
import androidx.compose.material.icons.filled.Edit
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel
import samf.gestorestudiantil.ui.navigation.Routes

enum class AdminView {
    CENTROS, TIPOS_CURSO, CURSOS, TURNOS, CICLOS, ASIGNATURAS, HORARIOS
}

@Composable
fun CentrosAdminPanel(
    paddingValues: PaddingValues,
    adminViewModel: AdminViewModel = viewModel(),
    onOpenDialog: (DialogState) -> Unit,
    onNavigate: (Routes.HomeRoutes) -> Unit
) {
    val adminState by adminViewModel.adminState.collectAsState()
    val context = LocalContext.current

    var currentView by remember { mutableStateOf(AdminView.CENTROS) }
    var selectedCentro by remember { mutableStateOf<Centro?>(null) }
    var selectedTipoCurso by remember { mutableStateOf<String?>(null) }
    var selectedCursoBaseName by remember { mutableStateOf<String?>(null) }
    var selectedCurso by remember { mutableStateOf<Curso?>(null) }
    var selectedTurno by remember { mutableStateOf<String?>(null) }
    var selectedCiclo by remember { mutableStateOf<String?>(null) } // NUEVO: Estado para el ciclo selecciona

    LaunchedEffect(Unit) {
        adminViewModel.cargarCentros()
    }

    val onBack = {
        when (currentView) {
            AdminView.HORARIOS -> currentView = AdminView.CICLOS
            AdminView.ASIGNATURAS -> currentView = AdminView.CICLOS
            AdminView.CICLOS -> currentView = AdminView.TURNOS
            AdminView.TURNOS -> currentView = AdminView.CURSOS
            AdminView.CURSOS -> currentView = AdminView.TIPOS_CURSO
            AdminView.TIPOS_CURSO -> currentView = AdminView.CENTROS
            else -> { /* Do nothing */ }
        }
    }

    BackHandler(enabled = currentView != AdminView.CENTROS) {
        onBack()
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                if (currentView != AdminView.CENTROS) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = textColor
                        )
                    }
                }
                Text(
                    text = when (currentView) {
                        AdminView.CENTROS -> "Gestión de Centros"
                        AdminView.TIPOS_CURSO -> "Tipos de Curso en ${selectedCentro?.nombre}"
                        AdminView.CURSOS -> "Cursos de $selectedTipoCurso"
                        AdminView.TURNOS -> "Turnos de ${selectedCurso?.acronimo}"
                        AdminView.CICLOS -> "Ciclos de ${selectedCurso?.acronimo} ($selectedTurno)"
                        AdminView.ASIGNATURAS -> "Asignaturas de ${selectedCurso?.acronimo} - $selectedCiclo"
                        AdminView.HORARIOS -> "Horario de ${selectedCurso?.acronimo} - $selectedCiclo"
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }

            if (adminState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    when (currentView) {
                        AdminView.CENTROS -> {
                            item {
                                Button(
                                    onClick = { adminViewModel.cargarDatosDesdeJsonl(context) },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Importar y limpiar IES Comercio")
                                } 
                            }
                            items(adminState.centros) { centro ->
                                CentroCard(
                                    centro = centro,
                                    onClick = {
                                        selectedCentro = centro
                                        adminViewModel.cargarCursosPorCentro(centro.id)
                                        currentView = AdminView.TIPOS_CURSO
                                    },
                                    onEdit = {
                                        onNavigate(Routes.HomeRoutes.EditCentro(centro))
                                    }
                                )
                            }
                        }
                        AdminView.TIPOS_CURSO -> {
                            // Evita crasheos si "it.tipo" es null o lanza excepción
                            val tiposUnicos = adminState.cursos.mapNotNull {
                                try { it.tipo } catch (e: Exception) { "Desconocido" }
                            }.distinct().sorted()

                            items(tiposUnicos) { tipo ->
                                TipoCursoCard(tipo) {
                                    selectedTipoCurso = tipo
                                    currentView = AdminView.CURSOS
                                }
                            }
                        }
                        AdminView.CURSOS -> {
                            val cursosPorTipo = adminState.cursos.filter { it.tipo == selectedTipoCurso }

                            items(cursosPorTipo) { curso ->
                                CursoCard(
                                    curso = curso,
                                    onClick = {
                                        selectedCurso = curso
                                        selectedCursoBaseName = curso.nombre
                                        currentView = AdminView.TURNOS
                                    },
                                    onEdit = {
                                        onNavigate(Routes.HomeRoutes.EditCurso(
                                            curso = curso,
                                            centroId = selectedCentro?.id ?: ""
                                        ))
                                    }
                                )
                            }
                        }
                        AdminView.TURNOS -> {
                            val turnos = selectedCurso?.turnosDisponibles ?: emptyList()
                            items(turnos) { turno ->
                                TipoCursoCard(turno.replaceFirstChar { it.uppercase() }) {
                                    selectedTurno = turno
                                    adminViewModel.cargarAsignaturasPorCurso(selectedCurso!!.id, turno)
                                    currentView = AdminView.CICLOS
                                }
                            }
                        }
                        AdminView.CICLOS -> {
                            // Evita crasheos si "it.ciclo" es null o lanza excepción
                            val ciclos = adminState.asignaturas.mapNotNull {
                                try { it.ciclo } catch (e: Exception) { null }
                            }.distinct().sorted()

                            items(ciclos) { ciclo ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "Ciclo: $ciclo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    selectedCiclo = ciclo
                                                    currentView = AdminView.ASIGNATURAS
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Asignaturas")
                                            }
                                            Button(
                                                onClick = {
                                                    selectedCiclo = ciclo
                                                    val cicloNum = ciclo.trim().firstOrNull()?.digitToIntOrNull() ?: 1
                                                    adminViewModel.cargarHorariosPorCursoYCiclo(selectedCurso!!.id, cicloNum, selectedTurno!!)
                                                    currentView = AdminView.HORARIOS
                                                },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Horario")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        AdminView.ASIGNATURAS -> {
                            // Filtramos las asignaturas en local según el ciclo seleccionado
                            val asignaturasDelCiclo = adminState.asignaturas.filter { it.ciclo == selectedCiclo }
                            items(asignaturasDelCiclo) { asignatura ->
                                AsignaturaCard(
                                    asignatura = asignatura,
                                    onClick = {
                                        onOpenDialog(DialogState.AsignarProfesor(asignatura))
                                    },
                                    onEdit = {
                                        onNavigate(Routes.HomeRoutes.EditAsignatura(
                                            asignatura = asignatura,
                                            cursoId = selectedCurso?.id ?: "",
                                            centroId = selectedCentro?.id ?: ""
                                        ))
                                    }
                                )
                            }
                        }
                        AdminView.HORARIOS -> {
                            val slots = if (selectedTurno == "matutino") samf.gestorestudiantil.data.models.Horario.HORAS_MATUTINO else samf.gestorestudiantil.data.models.Horario.HORAS_VESPERTINO
                            val dias = samf.gestorestudiantil.data.models.Horario.DIAS_SEMANA
                            val cicloNum = selectedCiclo?.trim()?.firstOrNull()?.digitToIntOrNull() ?: 1

                            items(slots) { slot ->
                                Text(
                                    text = slot,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = primaryColor
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    dias.forEach { dia ->
                                        val h = adminState.horarios.find { it.dia == dia && "${it.horaInicio} - ${it.horaFin}" == slot }
                                        val asig = adminState.asignaturas.find { it.idFirestore == h?.asignaturaId }

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(60.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Card(
                                                modifier = Modifier.fillMaxSize(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (asig != null) Color(android.graphics.Color.parseColor(asig.colorFondoHex)) else Color.LightGray.copy(alpha = 0.3f)
                                                ),
                                                onClick = {
                                                    val nuevoHorario = h ?: samf.gestorestudiantil.data.models.Horario(
                                                        cursoId = selectedCurso!!.id,
                                                        cicloNum = cicloNum,
                                                        turno = selectedTurno!!,
                                                        dia = dia,
                                                        horaInicio = slot.split(" - ")[0],
                                                        horaFin = slot.split(" - ")[1]
                                                    )
                                                    onOpenDialog(DialogState.EditHorario(
                                                        horario = nuevoHorario,
                                                        asignaturasDisponibles = adminState.asignaturas.filter { it.ciclo == selectedCiclo },
                                                        onSave = { adminViewModel.guardarHorario(it) }
                                                    ))
                                                }
                                            ) {
                                                Column(
                                                    modifier = Modifier.fillMaxSize(),
                                                    verticalArrangement = Arrangement.Center,
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = dia.take(1),
                                                        fontSize = 10.sp,
                                                        color = Color.Gray
                                                    )
                                                    Text(
                                                        text = h?.asignaturaAcronimo ?: "-",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = textColor
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = {
                when (currentView) {
                    AdminView.CENTROS -> {
                        onNavigate(Routes.HomeRoutes.EditCentro())
                    }
                    AdminView.CURSOS -> {
                        onNavigate(Routes.HomeRoutes.EditCurso(
                            centroId = selectedCentro?.id ?: ""
                        ))
                    }
                    AdminView.ASIGNATURAS -> {
                        onNavigate(Routes.HomeRoutes.EditAsignatura(
                            cursoId = selectedCurso?.id ?: "",
                            centroId = selectedCentro?.id ?: ""
                        ))
                    }
                    else -> {}
                }
            },
            containerColor = primaryColor,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir", tint = textColor)
        }
    }
}


@Composable
fun CentroCard(centro: Centro, onClick: () -> Unit, onEdit: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = centro.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                // Se previene un posible null al leer dirección en caso de que el modelo haya variado
                val addressStr = try { centro.direccion } catch (e: Exception) { "" }
                if (addressStr.isNotEmpty()) {
                    Text(text = addressStr, fontSize = 12.sp, color = Color.Gray)
                }
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun TipoCursoCard(tipo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Category, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = tipo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
        }
    }
}

// CursoCard simplificado: Al haber solo un documento por curso, la distinción "isBase" ya no es necesaria.
@Composable
fun CursoCard(curso: Curso, onClick: () -> Unit, onEdit: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val tituloStr = try { curso.acronimo } catch (e: Exception) { "Nombre desconocido" }
                val subtituloStr = try { curso.nombre } catch (e: Exception) { "" }

                Text(text = tituloStr, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                if (subtituloStr.isNotEmpty()) {
                    Text(text = subtituloStr, fontSize = 12.sp, color = Color.Gray)
                }
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
        }
    }
}
