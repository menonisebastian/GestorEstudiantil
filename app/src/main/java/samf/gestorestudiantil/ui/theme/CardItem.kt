package samf.gestorestudiantil.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CursoCardItem(curso: Curso) {
    Card(
        modifier = Modifier
            .width(120.dp) // Ancho fijo similar a la imagen
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = {}),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = curso.colorFondo),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Diseño plano (Flat)

    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono dentro de un círculo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(curso.colorIcono), // El color oscuro del icono
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = curso.icono,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nombre del curso
            Text(
                text = curso.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Horas
            Text(
                text = curso.horas,
                fontSize = 12.sp,
                color = Color.Gray.copy(alpha = 0.8f) // Gris semitransparente
            )
        }
    }
}