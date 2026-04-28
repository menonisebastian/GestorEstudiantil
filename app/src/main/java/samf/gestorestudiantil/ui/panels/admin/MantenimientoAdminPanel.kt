package samf.gestorestudiantil.ui.panels.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.R
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel

@Composable
fun MantenimientoAdminPanel(
    adminViewModel: AdminViewModel
) {
    val adminState by adminViewModel.adminState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Mantenimiento del Sistema",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            MantenimientoCard(
                titulo = "Gestión de Clases",
                descripcion = "Genera automáticamente los grupos (clases) basados en la configuración de cursos y asignaturas."
            ) {
                Button(
                    onClick = {
                        val id = adminState.centros.firstOrNull()?.id ?: "ies_comercio"
                        adminViewModel.generarClasesPorDefecto(id)
                    },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) {
                    Text(stringResource(R.string.admin_generate_classes_massively))
                }
            }
        }

        item {
            MantenimientoCard(
                titulo = "Generación de Datos de Prueba (Seeding)",
                descripcion = "Crea usuarios falsos en Authentication y Firestore para pruebas de desarrollo."
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { adminViewModel.generarAlumnosDummy() },
                        enabled = false,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Generar 40 Alumnos (DAM)")
                    }
                    Button(
                        onClick = { adminViewModel.generarProfesoresDummy() },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Generar 20 Profesores")
                    }
                }
            }
        }
    }
}

@Composable
fun MantenimientoCard(
    titulo: String,
    descripcion: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = titulo, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text(text = descripcion, fontSize = 14.sp, color = textColor.copy(alpha = 0.7f))
            content()
        }
    }
}
