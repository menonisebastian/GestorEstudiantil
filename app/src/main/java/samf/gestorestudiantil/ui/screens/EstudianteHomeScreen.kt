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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.ui.components.BottomNavBar
import samf.gestorestudiantil.ui.panels.estudiante.CalificacionesEstudiantePanel
import samf.gestorestudiantil.ui.panels.estudiante.NotificacionesEstudiantePanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EstudianteHomeScreen() {

    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var curso by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Materias") }

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
                BottomNavBar(items = itemsEstudiante,
                        selectedItem = selectedTab,
                        onItemSelected = { nuevaOpcion -> selectedTab = nuevaOpcion }
                    )
            }
        }
    ) { paddingValues ->
        // Columna Principal (Sin padding horizontal global)

        // Usamos AnimatedContent para manejar la transición
        AnimatedContent(
            targetState = selectedTab,
            label = "TabTransition", // Etiqueta para depuración
            modifier = Modifier.fillMaxSize(), // Opcional: para asegurar que ocupe todo el espacio
            transitionSpec = {
                // Definimos la animación: Un "Crossfade" (desvanecimiento cruzado) suave de 300ms
                fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
            }
        ) { target ->
            // IMPORTANTE: Usa la variable 'target' del lambda, NO 'selectedTab' directamente.
            // Esto asegura que Compose sepa qué contenido está saliendo y cuál entrando.

            when (target) {
                "Materias" -> MateriasEstudiantePanel(paddingValues)
                "Horarios" -> HorariosEstudiantePanel(paddingValues)
                "Calificaciones"    -> CalificacionesEstudiantePanel(paddingValues)
                "Notificaciones"  -> NotificacionesEstudiantePanel(paddingValues)
            }
        }
    }
}