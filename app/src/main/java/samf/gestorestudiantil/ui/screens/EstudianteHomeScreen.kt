package samf.gestorestudiantil.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Class
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Recordatorio
import samf.gestorestudiantil.data.models.listaRecordatorios
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.components.TopBarRow
import samf.gestorestudiantil.ui.dialogs.DialogOrchestrator
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.panels.estudiante.AsignaturasEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesAsignaturaPanel
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.HorariosEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.RecordatoriosEstudiantePanel
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudianteHomeScreen() {

    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }

    // 1. ESTADO CENTRALIZADO DE DIÁLOGOS
    var dialogState by remember { mutableStateOf<DialogState>(DialogState.None) }

    var asignaturaSeleccionada by remember { mutableStateOf<Asignatura?>(null) }
    var recordatorios by remember { mutableStateOf(listaRecordatorios) }

    val itemsEstudiante: Map<String, ImageVector> = mapOf(
        "Asignaturas" to Icons.Outlined.Class,
        "Horarios" to Icons.Default.Schedule,
        "Calificaciones" to Icons.AutoMirrored.Filled.Grading,
        "Recordatorios" to Icons.Outlined.Notifications
    )

    // Convertimos las claves del mapa a una lista para tener un orden fijo
    val tabs = remember { itemsEstudiante.keys.toList() }

    // Estado del Pager
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    val showFab = tabs[pagerState.currentPage] == "Recordatorios"

    name = "Sebastian"      // Datos simulados
    role = "Estudiante"
    curso = "DAMV2"

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    TopBarRow(name, role, curso, onNavigateProfile = {}, onNavigateSettings = {}, onLogout = {})
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                BottomNavBar(
                    items = itemsEstudiante,
                    selectedItem = tabs[pagerState.currentPage],
                    onItemSelected = { selectedKey ->
                        val index = tabs.indexOf(selectedKey)
                        if (index != -1) {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = {
                        // 2. ABRIR DIÁLOGO DE AÑADIR USANDO EL ESTADO
                        dialogState = DialogState.AddRecordatorio(
                            onSave = { titulo, descripcion, fecha, hora, tipo ->
                                val nuevoRecordatorio = Recordatorio(
                                    id = UUID.randomUUID().toString(),
                                    usuarioId = "user_simulado",
                                    titulo = titulo,
                                    descripcion = descripcion,
                                    fecha = fecha,
                                    hora = hora,
                                    tipo = tipo
                                )
                                recordatorios = recordatorios + nuevoRecordatorio
                            }
                        )
                    },
                    containerColor = primaryColor
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        tint = textColor,
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            userScrollEnabled = true
        ) { page ->

            when (tabs[page]) {
                "Asignaturas" -> AsignaturasEstudiantePanel(PaddingValues(0.dp))
                "Horarios" -> HorariosEstudiantePanel(PaddingValues(0.dp))
                "Recordatorios" -> RecordatoriosEstudiantePanel(
                    paddingValues = PaddingValues(0.dp),
                    // 3. PASAR CALLBACK PARA ABRIR FILTROS
                    onOpenDialog = { newState -> dialogState = newState }
                )
                "Calificaciones" -> {
                    if (asignaturaSeleccionada != null) {
                        CalificacionesAsignaturaPanel(
                            onBackClick = { asignaturaSeleccionada = null },
                            paddingValues = PaddingValues(0.dp),
                            asignatura = asignaturaSeleccionada!!
                        )
                        BackHandler {
                            asignaturaSeleccionada = null
                        }
                    } else {
                        CalificacionesEstudiantePanel(
                            paddingValues = PaddingValues(0.dp),
                            onAsignaturaClick = { asignatura ->
                                asignaturaSeleccionada = asignatura
                            }
                        )
                    }
                }
            }
        }
    }

    // 4. EL ORQUESTADOR MANEJA LOS DIÁLOGOS
    DialogOrchestrator(
        state = dialogState,
        onDismiss = { dialogState = DialogState.None }
    )
}