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
import samf.gestorestudiantil.models.Materia
import samf.gestorestudiantil.models.Modulo
import samf.gestorestudiantil.models.tipoEvaluacion
import samf.gestorestudiantil.ui.components.ModuloCard
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun CalificacionesMateriaPanel(
    materia: Materia,
    paddingValues: PaddingValues,
    onBackClick: () -> Unit
) {
    val listaModulos = listOf(
        Modulo(1, "UD1", 8.0, materia.descripcion, tipoEvaluacion.Exposicion),
        Modulo(2, "UD2", 7.0, materia.descripcion, tipoEvaluacion.Practica),
        Modulo(3, "UD3", 9.0, materia.descripcion, tipoEvaluacion.Examen),
        Modulo(4, "TFG", 10.0, materia.descripcion, tipoEvaluacion.Proyecto)
    )

    val notaMedia = listaModulos.sumOf { it.nota } / listaModulos.size

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

                Icon(imageVector = materia.icono, contentDescription = "Ver Calificaciones", tint = textColor, modifier = Modifier.padding(end = 8.dp))
                Text(materia.nombre, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
            }

            // BLOQUE 1: Contenido con márgenes
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp))
            {
                items(listaModulos)
                {
                        modulo ->
                    ModuloCard(modulo)
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