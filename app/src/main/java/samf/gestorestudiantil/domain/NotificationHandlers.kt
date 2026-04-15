package samf.gestorestudiantil.domain

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel

/**
 * Gestiona el registro del token de Firebase Cloud Messaging (FCM) 
 * cuando el usuario inicia sesión.
 */
@Composable
fun FcmTokenManager(authViewModel: AuthViewModel) {
    val authState by authViewModel.authState.collectAsState()
    val userId = authState.user?.id

    LaunchedEffect(userId) {
        if (userId != null) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    authViewModel.updateFcmToken(userId, token)
                }
            }
        }
    }
}

/**
 * Maneja la lógica de solicitud de permisos para notificaciones en Android 13+.
 */
@Composable
fun NotificationPermissionGate(onShowDialog: (DialogState) -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && activity != null) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(
                    context,
                    "No recibirás notificaciones de nuevas tareas",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        LaunchedEffect(Unit) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val hasPermission = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            
            if (!hasPermission) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    onShowDialog(
                        DialogState.NotificationPermissionRationale(
                            onConfirm = { launcher.launch(permission) }
                        )
                    )
                } else {
                    launcher.launch(permission)
                }
            }
        }
    }
}
