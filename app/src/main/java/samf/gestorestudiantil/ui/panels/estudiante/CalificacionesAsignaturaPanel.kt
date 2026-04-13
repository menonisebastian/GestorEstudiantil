package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.enums.tipoEvaluacion
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.EvaluacionCard
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

@Composable
fun CalificacionesAsignaturaPanel(
    asignatura: Asignatura,
    evaluaciones: List<Evaluacion>,
    paddingValues: PaddingValues,
    onBackClick: () -> Unit,
    onOpenDialog: (DialogState) -> Unit
) {
    val profesorViewModel: ProfesorViewModel = hiltViewModel()
    val profesor by profesorViewModel.profesor.collectAsState()

    LaunchedEffect(asignatura.profesorId) {
        profesorViewModel.cargarProfesor(asignatura.profesorId)
    }

    val notaMedia = if (evaluaciones.isNotEmpty()) {
        evaluaciones.sumOf { it.nota } / evaluaciones.size
    } else 0.0

    Column(modifier = Modifier
        .padding(horizontal = 20.dp)
        .fillMaxSize()
    )
    {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            AccImg(
                userName = profesor?.nombre ?: asignatura.profesorNombre,
                imgUrl = profesor?.imgUrl ?: "",
                size = 40.dp,
                onClick = { profesor?.let { onOpenDialog(DialogState.UserProfile(it)) } }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = String.format(java.util.Locale.getDefault(), "Media: %.2f", notaMedia),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (notaMedia >= 5) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                val turnoLetra = if (asignatura.turno.lowercase() == "matutino") "M" else "V"
                val cursoAcronimo = asignatura.cursoId.substringAfterLast("_").uppercase()
                Text(
                    text = asignatura.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Text(
                    text = "${asignatura.acronimo} $cursoAcronimo$turnoLetra${asignatura.cicloNum}",
                    fontSize = 12.sp,
                    color = surfaceDimColor
                )
            }
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
                EvaluacionCard(modulo, onClick = { onOpenDialog(DialogState.VerDetalleEvaluacion(modulo)) })
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
