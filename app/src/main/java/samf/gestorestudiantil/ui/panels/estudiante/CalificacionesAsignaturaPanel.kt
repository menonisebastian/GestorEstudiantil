package samf.gestorestudiantil.ui.panels.estudiante

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
import androidx.compose.material.icons.outlined.ArrowBackIosNew
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
    paddingValues: PaddingValues,
    onBackClick: () -> Unit
) {
    val listaEvaluaciones = listOf(
        Evaluacion("", "UD1", 8.0, "", asignatura.descripcion, tipoEvaluacion.Exposicion),
        Evaluacion("", "UD2", 7.0, "", asignatura.descripcion, tipoEvaluacion.Practica),
        Evaluacion("", "UD3", 9.0, "", asignatura.descripcion, tipoEvaluacion.Examen),
        Evaluacion("", "TFG", 10.0, "", asignatura.descripcion, tipoEvaluacion.Proyecto)
    )

    val notaMedia = listaEvaluaciones.sumOf { it.nota } / listaEvaluaciones.size

    Column(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    )
    {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )
        {
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp))
            {
                IconButton(onClick = {onBackClick()})
                {
                    Icon(imageVector = Icons.Outlined.ArrowBackIosNew, contentDescription = "Ver Calificaciones", tint = textColor)
                }

                Spacer(modifier = Modifier.weight(1f))

                Icon(imageVector = asignatura.iconoName.toComposeIcon(), contentDescription = "Ver Calificaciones", tint = textColor, modifier = Modifier.padding(end = 8.dp))
                Text(asignatura.nombre, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            }

            // BLOQUE 1: Contenido con márgenes
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp))
            {
                items(listaEvaluaciones)
                {
                        modulo ->
                    EvaluacionCard(modulo)
                }
                item{Spacer(modifier = Modifier.height(16.dp))}
            }

            Row(modifier = Modifier.padding(end = 16.dp)) {
                Spacer(modifier = Modifier.weight(1f))
                Text("Nota media: $notaMedia", fontSize = 16.sp, color = textColor)
            }
        }
    }
}