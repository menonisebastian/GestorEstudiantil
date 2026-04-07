package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel

@Composable
fun AsignarAsignaturasDialog(
    state: DialogState.AsignarAsignaturas,
    onDismissRequest: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val adminState by adminViewModel.adminState.collectAsState()
    
    // Al abrir, cargamos las asignaturas del profesor y las que no tienen profesor
    LaunchedEffect(state.profesor.id, state.profesor.turno) {
        adminViewModel.cargarAsignaturasSinProfesor(state.profesor.turno)
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

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onDismissRequest) { Text("Cerrar") }
        },
        title = {
            Text(text = "Asignaturas de ${state.profesor.nombre}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.height(450.dp)) {
                if (asignaturasProfesor.isNotEmpty()) {
                    Text("Asignadas", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(asignaturasProfesor) { asig ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(asig.acronimo, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                        Text(asig.nombre, fontSize = 10.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                    }
                                    IconButton(onClick = { 
                                        adminViewModel.desasignarAsignatura(asig.idFirestore, state.profesor.id)
                                        // Actualizar lista local para feedback inmediato
                                        asignaturasProfesor = asignaturasProfesor.filter { it.idFirestore != asig.idFirestore }
                                    }) {
                                        Icon(Icons.Default.Remove, contentDescription = "Desasignar", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Disponibles (Sin Profesor)", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                
                if (adminState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                        items(adminState.asignaturas) { asig ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(asig.acronimo, fontWeight = FontWeight.Bold)
                                        Text(asig.nombre, fontSize = 10.sp)
                                    }
                                    IconButton(onClick = { 
                                        adminViewModel.asignarAsignaturaAProfesor(asig.idFirestore, state.profesor.id)
                                        // Actualizar lista local
                                        asignaturasProfesor = asignaturasProfesor + asig
                                    }) {
                                        Icon(Icons.Default.Add, contentDescription = "Asignar")
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
