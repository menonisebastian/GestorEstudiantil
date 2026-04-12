package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.enums.tipoEvaluacion
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.EvaluacionCard
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun CalificacionesAsignaturaPanel(
    asignatura: Asignatura,
    evaluaciones: List<Evaluacion>,
    paddingValues: PaddingValues,
    onBackClick: () -> Unit
) {
    val notaMedia = if (evaluaciones.isNotEmpty()) {
        evaluaciones.sumOf { it.nota } / evaluaciones.size
    } else 0.0

    Column(modifier = Modifier
        .padding(horizontal = 20.dp)
        .fillMaxSize()
    )
    {
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically)
        {
            Spacer(modifier = Modifier.weight(1f))

            Icon(imageVector = asignatura.iconoName.toComposeIcon(), contentDescription = "Asignatura", tint = textColor, modifier = Modifier.padding(end = 8.dp))
            Text(asignatura.nombre, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        )
        {
            items(evaluaciones)
            {
                    modulo ->
                EvaluacionCard(modulo)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 120.dp), // Espacio para que el BottomBar no lo tape
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = String.format(java.util.Locale.getDefault(), "Nota media: %.2f", notaMedia),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}
