package samf.gestorestudiantil.domain.notifications

import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import samf.gestorestudiantil.MainActivity
import samf.gestorestudiantil.R
import samf.gestorestudiantil.domain.repositories.SettingsRepository
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCM_Service", "Mensaje recibido de: ${remoteMessage.from}")
        scope.launch {
            val enabled = settingsRepository.notificationsEnabled.first()
            if (enabled) {
                val data = remoteMessage.data
                Log.d("FCM_Service", "Datos del mensaje: $data")
                
                val senderId = data["sender_id"]
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                if (senderId != null && senderId == currentUserId) {
                    Log.d("FCM_Service", "El emisor es el usuario actual, ignorando.")
                    return@launch
                }

                val title = remoteMessage.notification?.title ?: data["title"]
                val body = remoteMessage.notification?.body ?: data["body"]

                if (title != null || body != null) {
                    Log.d("FCM_Service", "Mostrando notificación: $title")
                    showNotification(title, body, data)
                } else {
                    Log.d("FCM_Service", "El mensaje no tiene título ni cuerpo.")
                }
            } else {
                Log.d("FCM_Service", "Las notificaciones están desactivadas en los ajustes.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun showNotification(title: String?, message: String?, data: Map<String, String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, Random.nextInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = NotificationConstants.CHANNEL_POSTS_ID
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.newicononexoplus)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                NotificationConstants.CHANNEL_POSTS_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
        manager.notify(Random.nextInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
    }
}
