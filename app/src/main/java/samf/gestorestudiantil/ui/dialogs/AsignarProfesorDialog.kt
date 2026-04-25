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
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.errorColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.secondaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.theme.whiteColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel

@Composable
fun AsignarProfesorDialog(
    state: DialogState.AsignarProfesor,
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
            Text(text = "Asignar Profesor a ${state.asignatura.acronimo}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = textColor)
        },
        text = {
            AsignarProfesorContent(
                state = state,
                onDismissRequest = onDismissRequest,
                adminViewModel = adminViewModel
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarProfesorBottomSheet(
    state: DialogState.AsignarProfesor,
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
                text = "Asignar Profesor a ${state.asignatura.acronimo}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            AsignarProfesorContent(
                state = state,
                onDismissRequest = onDismissRequest,
                adminViewModel = adminViewModel
            )
        }
    }
}

@Composable
fun AsignarProfesorContent(
    state: DialogState.AsignarProfesor,
    onDismissRequest: () -> Unit,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val adminState by adminViewModel.adminState.collectAsState()
    
    val profesores = remember(adminState.usuarios, state.asignatura.departamento) {
        adminState.usuarios
            .filterIsInstance<User.Profesor>()
            .sortedByDescending { it.departamento == state.asignatura.departamento }
    }

    Column(modifier = Modifier.heightIn(max = 400.dp)) {
        if (state.asignatura.profesorId.isNotEmpty()) {
            Text("Profesor Actual:", fontWeight = FontWeight.Bold, color = textColor)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(state.asignatura.profesorNombre, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = primaryColor)
                    IconButton(onClick = { 
                        adminViewModel.desasignarAsignatura(state.asignatura.id, state.asignatura.profesorId)
                        onDismissRequest()
                    }, colors = IconButtonDefaults.iconButtonColors(containerColor = errorColor.copy(alpha = 0.1f))) {
                        Icon(Icons.Default.Remove, contentDescription = "Quitar", tint = errorColor)
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = surfaceDimColor.copy(alpha = 0.2f))
        }

        Text("Seleccionar Profesor:", fontWeight = FontWeight.Bold, color = tertiaryColor, modifier = Modifier.padding(bottom = 8.dp))
        
        if (profesores.isEmpty()) {
            Text("No hay profesores registrados en este centro", color = errorColor, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(profesores) { profe ->
                if (profe.id != state.asignatura.profesorId) {
                    val esDelMismoDep = profe.departamento == state.asignatura.departamento
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (esDelMismoDep) primaryColor.copy(alpha = 0.05f) else surfaceColor
                        ),
                        border = if (esDelMismoDep) androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)) else null,
                        onClick = {
                            adminViewModel.asignarAsignaturaAProfesor(state.asignatura.id, profe.id)
                            onDismissRequest()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(profe.nombre, fontWeight = FontWeight.Bold, color = textColor)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(profe.email, fontSize = 10.sp, color = surfaceDimColor)
                                    if (profe.departamento.isNotEmpty()) {
                                        Text(" • ", fontSize = 10.sp, color = surfaceDimColor)
                                        Text(profe.departamento, fontSize = 10.sp, color = if (esDelMismoDep) primaryColor else surfaceDimColor, fontWeight = if (esDelMismoDep) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }
                            Icon(Icons.Default.Add, contentDescription = "Asignar", tint = tertiaryColor)
                        }
                    }
                }
            }
        }
    }
}
