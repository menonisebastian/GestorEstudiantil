package samf.gestorestudiantil.ui.panels.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.ui.components.AsignaturaCard
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel

@Composable
fun AsignarProfesorPanel(
    paddingValues: PaddingValues,
    adminViewModel: AdminViewModel = viewModel(),
    onOpenDialog: (DialogState) -> Unit
) {
    val adminState by adminViewModel.adminState.collectAsState()

    var currentView by remember { mutableStateOf(AdminView.CENTROS) }
    var selectedCentro by remember { mutableStateOf<Centro?>(null) }
    var selectedTipoCurso by remember { mutableStateOf<String?>(null) }
    var selectedCurso by remember { mutableStateOf<Curso?>(null) }
    var selectedTurno by remember { mutableStateOf<String?>(null) }
    var selectedCiclo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        adminViewModel.cargarCentros()
    }

    val onBack = {
        when (currentView) {
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
                        AdminView.CENTROS -> "Asignar: Centros"
                        AdminView.TIPOS_CURSO -> "Tipo de Curso"
                        AdminView.CURSOS -> "Seleccionar Curso"
                        AdminView.TURNOS -> "Seleccionar Turno"
                        AdminView.CICLOS -> "Seleccionar Ciclo"
                        AdminView.ASIGNATURAS -> "Elegir Asignatura"
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
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    when (currentView) {
                        AdminView.CENTROS -> {
                            items(adminState.centros) { centro ->
                                CentroCard(
                                    centro = centro,
                                    onClick = {
                                        selectedCentro = centro
                                        adminViewModel.cargarCursosPorCentro(centro.id)
                                        currentView = AdminView.TIPOS_CURSO
                                    }
                                )
                            }
                        }
                        AdminView.TIPOS_CURSO -> {
                            val tiposUnicos = adminState.cursos.map { it.tipo }.distinct().sorted()
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
                                        currentView = AdminView.TURNOS
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
                            val ciclos = adminState.asignaturas
                                .filter { it.turno == selectedTurno }
                                .mapNotNull { it.ciclo }.distinct().sorted()
                            items(ciclos) { ciclo ->
                                TipoCursoCard("Ciclo: $ciclo") {
                                    selectedCiclo = ciclo
                                    currentView = AdminView.ASIGNATURAS
                                }
                            }
                        }
                        AdminView.ASIGNATURAS -> {
                            val asignaturasDelCiclo = adminState.asignaturas.filter { 
                                it.ciclo == selectedCiclo && it.turno == selectedTurno 
                            }
                            items(asignaturasDelCiclo) { asignatura ->
                                AsignaturaCard(
                                    asignatura = asignatura,
                                    onClick = {
                                        onOpenDialog(DialogState.AsignarProfesor(asignatura))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
