package samf.gestorestudiantil.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalScreen(usuario: User, onLogout: () -> Unit) {
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Matrícula Pendiente", color = textColor) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.HourglassEmpty,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = primaryColor
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "¡Hola, ${usuario.nombre}!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            val infoCursoArea = when (usuario) {
                is User.Estudiante -> usuario.curso
                is User.Profesor -> usuario.departamento
                else -> ""
            }
            Text(
                "Tu solicitud de matrícula para $infoCursoArea ha sido recibida y está en proceso de revisión por parte de la administración de tu instituto.",
                fontSize = 16.sp,
                color = surfaceDimColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}