package samf.gestorestudiantil.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import samf.gestorestudiantil.ui.components.TopBarRow
import samf.gestorestudiantil.ui.components.itemsEstudiante
import samf.gestorestudiantil.ui.panels.estudiante.HorariosEstudiantePanel
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.panels.estudiante.MateriasEstudiantePanel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.dialogs.AddRecordatorioDialog
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.RecordatoriosEstudiantePanel
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.textColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudianteHomeScreen() {

    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    // 1. Convertimos las claves del mapa a una lista para tener un orden fijo (0, 1, 2, 3)
    val tabs = remember { itemsEstudiante.keys.toList() }

    // 2. Estado del Pager
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    // 3. CoroutineScope para mover el pager al hacer clic en la barra
    val scope = rememberCoroutineScope()

    val showFab = tabs[pagerState.currentPage] == "Recordatorios"

    var showRecordatorioDialog by remember { mutableStateOf(false) }


    name = "Sebastian"      //Obtiene el nombre de usuario de Firebase
    role = "Estudiante"     //Obtiene el rol del usuario de Firebase
    curso = "DAMV2"

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    TopBarRow(name, role, curso)
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
                        // Al hacer clic, buscamos el índice y movemos el Pager
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
            if (showFab)
            {
                FloatingActionButton(
                    onClick = {
                        showRecordatorioDialog = true
                    },
                    containerColor = primaryColor
                )
                {
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
                .padding(paddingValues), // Aplicamos el padding del Scaffold aquí
            userScrollEnabled = true // Permite deslizar con el dedo
        ) { page ->

            // Renderizamos el contenido según el índice de la página actual
            when (tabs[page]) {
                "Materias" -> MateriasEstudiantePanel(PaddingValues(0.dp)) // Pasamos 0dp porque el padding ya lo tiene el Pager
                "Horarios" -> HorariosEstudiantePanel(PaddingValues(0.dp))
                "Calificaciones"    -> CalificacionesEstudiantePanel(PaddingValues(0.dp))
                "Recordatorios"  -> RecordatoriosEstudiantePanel(PaddingValues(0.dp))
            }
        }
    }

    if (showRecordatorioDialog)
    {
        AddRecordatorioDialog(onDismissRequest = { showRecordatorioDialog = false }, onAddRecordatorio = { titulo, descripcion, fecha, hora, tipo ->
            showRecordatorioDialog = false
        })
    }
}