package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.ProfileImagePicker
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    usuario: User?,
    onBack: () -> Unit,
    onProfileUpdated: (User) -> Unit = {}
)
{
    val context = LocalContext.current

    // Estado local para actualizar la UI inmediatamente tras la subida
    var currentPhotoUrl by remember { mutableStateOf(usuario?.imgUrl ?: "") }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = textColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Atrás", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // === AVATAR ===
            ProfileImagePicker(
                currentPhotoUrl = currentPhotoUrl,
                onPhotoUploaded = { secureUrl ->
                    // 1. Actualizamos la imagen localmente (lo que ya hacías)
                    currentPhotoUrl = secureUrl

                    // 2. Guardar en Firestore
                    usuario?.id?.let { uid ->
                        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                            .update(
                                "imgUrl", secureUrl,
                                "fotoUrl", secureUrl
                            )
                            .addOnSuccessListener {
                                Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()

                                // 3. CLAVE: Notificar a la App del cambio para que el Home se entere
                                val usuarioActualizado = usuario.copy(imgUrl = secureUrl, fotoUrl = secureUrl)
                                onProfileUpdated(usuarioActualizado)
                            }
                    }
                }
            )

            Spacer(modifier = Modifier.height(40.dp))

            // === DATOS DEL USUARIO ===
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surfaceColor)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Nombre: ${usuario?.nombre ?: ""}", color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text("Email: ${usuario?.email ?: ""}", color = surfaceDimColor, fontSize = 14.sp)
                    Text("Rol: ${usuario?.rol ?: ""}", color = surfaceDimColor, fontSize = 14.sp)
                    Text("Curso/Área: ${usuario?.cursoOArea ?: ""}", color = surfaceDimColor, fontSize = 14.sp)
                }
            }
        }
    }
}