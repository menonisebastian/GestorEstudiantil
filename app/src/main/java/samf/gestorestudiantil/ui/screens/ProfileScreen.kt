package samf.gestorestudiantil.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.uploadToCloudinary
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(usuario: User?, onBack: () -> Unit) {
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }

    // Estado local para actualizar la UI inmediatamente tras la subida
    var currentPhotoUrl by remember { mutableStateOf(usuario?.imgUrl ?: "") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            uploadToCloudinary(
                uri = it,
                onStart = { isUploading = true },
                onProgress = { progress -> uploadProgress = progress },
                onSuccess = { secureUrl ->
                    isUploading = false
                    currentPhotoUrl = secureUrl

                    // Guardar la URL en el perfil de Firestore del usuario
                    usuario?.id?.let { uid ->
                        FirebaseFirestore.getInstance().collection("usuarios").document(uid)
                            .update("fotoUrl", secureUrl)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                onError = { error ->
                    isUploading = false
                    Toast.makeText(context, "Error al subir: $error", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = textColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = textColor)
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
            // === AVATAR CON COIL ===
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (currentPhotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = currentPhotoUrl,
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // === BOTÓN DE SUBIDA ===
            Button(
                onClick = { photoPickerLauncher.launch("image/*") },
                enabled = !isUploading
            ) {
                Text(if (isUploading) "Subiendo..." else "Cambiar Foto")
            }

            if (isUploading) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier.fillMaxWidth(0.5f)
                )
            }

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