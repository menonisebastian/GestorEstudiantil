package samf.gestorestudiantil.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.ui.components.BottomBar
import samf.gestorestudiantil.ui.components.TopBarRow
import samf.gestorestudiantil.ui.components.itemsEstudiante
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.layouts.estudiante.EstudianteLayout

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {

    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    name = "Sebastian"      //Obtiene el nombre de usuario de Firebase
    role = "Estudiante"     //Obtiene el rol del usuario de Firebase

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    TopBarRow(name, role)
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
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                BottomBar(itemsEstudiante)
            }
        }
    ) { paddingValues ->
        // Columna Principal (Sin padding horizontal global)

        when (role) {
            "Estudiante" -> EstudianteLayout(paddingValues)         // Layout de Estudiantes
            "Profesor" -> EstudianteLayout(paddingValues)           // Layout de Profesores
            "Administrador" -> EstudianteLayout(paddingValues)      // Layout de Administradores
        }
    }
}