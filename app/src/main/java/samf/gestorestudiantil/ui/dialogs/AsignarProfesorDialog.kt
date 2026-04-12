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
    adminViewModel: AdminViewModel = viewModel()
) {
    val adminState by adminViewModel.adminState.collectAsState()
    
    // Al abrir, cargamos todos los profesores del centro
    LaunchedEffect(Unit) {
        // Asumiendo que podemos filtrar usuarios por rol PROFESOR desde el estado general o cargarlos
        // Para este ejemplo, usaremos los usuarios ya cargados en adminState que sean profesores
    }

    val profesores = remember(adminState.usuarios, state.asignatura.turno) {
        adminState.usuarios.filter { 
            it.rol == "PROFESOR" && it.turno.equals(state.asignatura.turno, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Cerrar") }
        },
        containerColor = backgroundColor,
        title = {
            Text(text = "Asignar Profesor a ${state.asignatura.acronimo}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                if (state.asignatura.profesorId.isNotEmpty()) {
                    Text("Profesor Actual:", fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
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
                    Text("No hay profesores registrados en este turno", color = errorColor, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(profesores) { profe ->
                        // No mostrar si ya es el profesor asignado
                        if (profe.id != state.asignatura.profesorId) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = surfaceColor),
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
                                        Text(profe.email, fontSize = 10.sp, color = surfaceDimColor)
                                    }
                                    Icon(Icons.Default.Add, contentDescription = "Asignar", tint = tertiaryColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
