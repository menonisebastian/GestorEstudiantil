package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.data.models.Evaluacion
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.ui.components.EvaluacionCard
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.ProfesorViewModel

import samf.gestorestudiantil.ui.viewmodels.EstudianteViewModel

@Composable
fun CalificacionesAsignaturaPanel(
    asignatura: Asignatura,
    evaluaciones: List<Evaluacion>,
    onOpenDialog: (DialogState) -> Unit
) {
    val profesorViewModel: ProfesorViewModel = hiltViewModel()
    val estudianteViewModel: EstudianteViewModel = hiltViewModel()
    val profesor by profesorViewModel.profesor.collectAsState()

    LaunchedEffect(asignatura.profesorId) {
        profesorViewModel.cargarProfesor(asignatura.profesorId)
    }

    val notaMedia = if (evaluaciones.isNotEmpty()) {
        evaluaciones.sumOf { it.nota } / evaluaciones.size
    } else 0.0

    Box(modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 90.dp, bottom = 140.dp)
        ) {
            items(evaluaciones) { modulo ->
                EvaluacionCard(modulo, onClick = { 
                    onOpenDialog(DialogState.VerDetalleEvaluacion(
                        evaluacion = modulo,
                        onAttachmentClick = { path, name ->
                            onOpenDialog(DialogState.AttachmentOptions(
                                supabasePath = path,
                                fileName = name,
                                onOpen = { p, n -> estudianteViewModel.descargarArchivo(p, n, isDirectDownload = false) },
                                onDownload = { p, n -> estudianteViewModel.descargarArchivo(p, n, isDirectDownload = true) }
                            ))
                        }
                    )) 
                })
            }
        }

        // Cabezal Flotante (Similar al del Tutor)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AccImg(
                    userName = profesor?.nombre ?: asignatura.profesorNombre,
                    imgUrl = profesor?.imgUrl ?: "",
                    size = 40.dp,
                    onClick = { profesor?.let { onOpenDialog(DialogState.UserProfile(it)) } }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mis Calificaciones",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    val turnoLetra = if (asignatura.turno.lowercase() == "matutino") "M" else "V"
                    val cursoAcronimo = asignatura.cursoId.substringAfterLast("_").uppercase()
                    Text(
                        text = asignatura.acronimo.uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "$cursoAcronimo$turnoLetra${asignatura.cicloNum}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.End
                    )
                }
            }
        }

        // Nota Media Flotante Inferior
        Card(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = String.format(java.util.Locale.getDefault(), "MEDIA: %.2f", notaMedia).uppercase(),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}
