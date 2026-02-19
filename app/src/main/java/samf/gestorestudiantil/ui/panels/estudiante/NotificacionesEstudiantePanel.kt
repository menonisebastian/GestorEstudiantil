package samf.gestorestudiantil.ui.panels.estudiante

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.ui.components.CustomNotificationCard
import samf.gestorestudiantil.ui.components.tipoNotificacion
import samf.gestorestudiantil.ui.theme.textColor

@Composable
fun NotificacionesEstudiantePanel(paddingValues: PaddingValues)
{
    Column(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // BLOQUE 1: Contenido con márgenes (Agrupado)
        // Aquí metemos todo lo que SÍ necesita márgenes
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp) // <--- Un solo padding para todo este bloque
        ) {

            Spacer(modifier = Modifier.height(24.dp))

            // Título (ya no necesita padding individual)
            Text(
                text = "Mis Notificaciones",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomNotificationCard(tipoNotificacion.EXAMEN)

            Spacer(modifier = Modifier.height(16.dp))

            CustomNotificationCard(tipoNotificacion.TAREA)

            Spacer(modifier = Modifier.height(16.dp))

            CustomNotificationCard(tipoNotificacion.EVENTO)
        }
    }
}