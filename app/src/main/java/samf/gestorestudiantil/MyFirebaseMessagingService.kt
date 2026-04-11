package samf.gestorestudiantil

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Solo mostramos notificación manual si la app está en primer plano
        // Si está en segundo plano, el sistema usa el campo 'notification' del JSON
        remoteMessage.notification?.let {
            showNotification(it.title, it.body, remoteMessage.data)
        }
    }

    private fun showNotification(title: String?, message: String?, data: Map<String, String>) {
        val asignaturaId = data["target_asignatura_id"] // Coincide con lo enviado desde ProfesorViewModel
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            // Pasamos el ID de la asignatura para que MainActivity sepa a dónde ir
            putExtra("target_asignatura_id", asignaturaId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "posts_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.icononexopluscut) // Usando un icono existente
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Nuevos Posts", NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(channel)
        }
        manager.notify(Random.nextInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        // El token se actualizará desde MainActivity cuando el usuario inicie sesión
        // o mediante un Worker si es necesario. Por ahora lo dejamos listo para ser consultado.
    }
}
