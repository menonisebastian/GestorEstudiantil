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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.errorColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel

@Composable
fun AsignarTutorDialog(
    state: DialogState.AsignarTutor,
    onDismissRequest: () -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Cerrar", color = textColor) }
        },
        containerColor = backgroundColor,
        title = {
            Text(text = "Asignar Tutor", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        },
        text = {
            AsignarTutorContent(
                state = state,
                onDismissRequest = onDismissRequest,
                adminViewModel = adminViewModel
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarTutorBottomSheet(
    state: DialogState.AsignarTutor,
    onDismissRequest: () -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = backgroundColor
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Asignar Tutor",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            AsignarTutorContent(
                state = state,
                onDismissRequest = onDismissRequest,
                adminViewModel = adminViewModel
            )
        }
    }
}

@Composable
fun AsignarTutorContent(
    state: DialogState.AsignarTutor,
    onDismissRequest: () -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val adminState by adminViewModel.adminState.collectAsState()
    
    // Obtenemos los profesores del centro actual
    val profesores = remember(adminState.usuarios) {
        adminState.usuarios.filterIsInstance<User.Profesor>()
    }

    Column(modifier = Modifier.heightIn(max = 400.dp)) {
        Text("Seleccionar Profesor:", fontWeight = FontWeight.Bold, color = tertiaryColor, modifier = Modifier.padding(bottom = 8.dp))
        
        if (profesores.isEmpty()) {
            Text("No hay profesores registrados en este centro", color = errorColor, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(profesores) { profe ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    onClick = {
                        adminViewModel.asignarTutorAClase(state.claseId, profe.id)
                        onDismissRequest()
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(profe.nombre, fontWeight = FontWeight.Bold, color = textColor)
                            Text(profe.email, fontSize = 10.sp, color = surfaceDimColor)
                        }
                        Icon(Icons.Default.Add, contentDescription = "Asignar", tint = tertiaryColor)
                    }
                }
            }
        }
    }
}
