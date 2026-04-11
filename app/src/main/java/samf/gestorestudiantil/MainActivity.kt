package samf.gestorestudiantil

import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.cloudinary.android.MediaManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import samf.gestorestudiantil.ui.navigation.AppNavigation
import samf.gestorestudiantil.ui.theme.GestorEstudiantilTheme
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var targetAsignaturaId by mutableStateOf<String?>(null)
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)
        enableEdgeToEdge()
        try {
            val config = mapOf("cloud_name" to "dywawleqm")
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Ignorar si ya está inicializado
        }
        setContent {
            GestorEstudiantilTheme {
                RequestNotificationPermission()
                RegisterFcmToken()
                AppNavigation(
                    targetAsignaturaId = targetAsignaturaId,
                    onNotificationHandled = { targetAsignaturaId = null }
                )
            }
        }
    }

    @Composable
    private fun RegisterFcmToken() {
        LaunchedEffect(Unit) {
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser != null) {
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            authViewModel.updateFcmToken(userId, token)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun RequestNotificationPermission() {
        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* Permiso manejado */ }

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        val asignaturaId = intent?.getStringExtra("target_asignatura_id")
        if (asignaturaId != null) {
            targetAsignaturaId = asignaturaId
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview()
{

}
