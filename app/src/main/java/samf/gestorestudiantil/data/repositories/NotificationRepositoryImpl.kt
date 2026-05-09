package samf.gestorestudiantil.data.repositories

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import samf.gestorestudiantil.domain.repositories.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val client: OkHttpClient,
    @param:ApplicationContext private val context: Context
) : NotificationRepository {

    private val fcmUrl = "https://fcm.googleapis.com/v1/projects/gestorinstituto-tfg/messages:send"
    private var googleCredentials: GoogleCredentials? = null

    override suspend fun sendTopicNotification(
        topic: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        try {
            val message = JSONObject().apply {
                put("topic", topic)
                val dataJson = JSONObject(data).apply {
                    put("title", title)
                    put("body", body)
                }
                put("android", JSONObject().apply {
                    put("priority", "high")
                })
                put("data", dataJson)
            }
            Log.d("NotificationRepo", "Enviando notificación al topic: $topic. Titulo: $title")
            sendFCMRequest(message)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error al construir JSON de notificación: ${e.message}")
        }
    }

    override suspend fun sendTokenNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        try {
            val message = JSONObject().apply {
                put("token", token)
                put("notification", JSONObject().apply {
                    put("title", title)
                    put("body", body)
                })
                if (data.isNotEmpty()) {
                    put("data", JSONObject(data))
                }
                put("android", JSONObject().apply {
                    put("priority", "high")
                })
            }
            Log.d("NotificationRepo", "Enviando notificación al token: ${token.take(10)}... Titulo: $title")
            sendFCMRequest(message)
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error al construir JSON de notificación por token: ${e.message}")
        }
    }

    private suspend fun sendFCMRequest(messageJson: JSONObject) = withContext(Dispatchers.IO) {
        val accessToken = getAccessToken() ?: return@withContext

        val payload = JSONObject().apply {
            put("message", messageJson)
        }

        val requestBody = payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(fcmUrl)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string()
                    Log.e("NotificationRepo", "Error enviando notificación FCM. Código: ${response.code}, Body: $errorBody")
                } else {
                    Log.d("NotificationRepo", "Notificación enviada con éxito a FCM")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error de red al enviar a FCM: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun getAccessToken(): String? {
        return try {
            if (googleCredentials == null) {
                val inputStream = context.assets.open("service-account.json")
                googleCredentials = GoogleCredentials.fromStream(inputStream)
                    .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            }
            googleCredentials?.refreshIfExpired()
            googleCredentials?.accessToken?.tokenValue
        } catch (e: Exception) {
            Log.e("NotificationRepo", "Error al obtener Access Token: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}
