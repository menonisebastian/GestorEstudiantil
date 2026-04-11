package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.ui.components.AsignaturaCard
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun CalificacionesEstudiantePanel(
    asignaturas: List<Asignatura>,
    paddingValues: PaddingValues,
    onAsignaturaClick: (Asignatura) -> Unit
)
{
    Column(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // BLOQUE 1: Contenido con márgenes (Agrupado)
        // Aquí metemos todo lo que SÍ necesita márgenes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp) // <--- Un solo padding para todo este bloque
        ) {

            // Título (ya no necesita padding individual)
            Text(
                text = "Mis Calificaciones",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(top = 16.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.padding(top = 16.dp))
            {
                items(asignaturas)
                {
                    asignatura ->
                    AsignaturaCard(asignatura, userRole = "ESTUDIANTE", onClick = {
                        onAsignaturaClick(asignatura)
                    })
                }
                item{Spacer(modifier = Modifier.height(16.dp))}
            }
        }
    }
}
