package samf.gestorestudiantil.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun <T> CardItem(
    item: T,
    getIcono: (T) -> ImageVector,
    getColorFondo: (T) -> Color,
    getColorIcono: (T) -> Color,
    getAcron: (T) -> String,       // <-- Nuevo parámetro para el título
    getNombre: (T) -> String     // <-- Nuevo parámetro para el subtítulo (horas, créditos, etc.)
) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = {}),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = getColorFondo(item)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(getColorIcono(item)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getIcono(item),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Usamos la función getTitulo() en lugar de curso.nombre
            Text(
                text = getAcron(item),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Usamos la función getSubtitulo() en lugar de curso.horas
            Text(
                text = getNombre(item),
                fontSize = 10.sp,
                color = Color.Gray.copy(alpha = 0.8f)
            )
        }
    }
}