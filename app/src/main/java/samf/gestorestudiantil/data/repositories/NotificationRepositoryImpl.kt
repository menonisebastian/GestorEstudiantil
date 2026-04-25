package samf.gestorestudiantil.data.repositories

import android.content.Context
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
    @ApplicationContext private val context: Context
) : NotificationRepository {

    private val fcmUrl = "https://fcm.googleapis.com/v1/projects/gestorinstituto-tfg/messages:send"

    override suspend fun sendTopicNotification(
        topic: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val message = JSONObject().apply {
            put("topic", topic)
            put("notification", JSONObject().apply {
                put("title", title)
                put("body", body)
            })
            if (data.isNotEmpty()) {
                put("data", JSONObject(data))
            }
        }
        sendFCMRequest(message)
    }

    override suspend fun sendTokenNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String>
    ) {
        val message = JSONObject().apply {
            put("token", token)
            put("notification", JSONObject().apply {
                put("title", title)
                put("body", body)
            })
            if (data.isNotEmpty()) {
                put("data", JSONObject(data))
            }
        }
        sendFCMRequest(message)
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
                    println("Error enviando notificación FCM: ${response.body?.string()}")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getAccessToken(): String? {
        return try {
            val inputStream = context.assets.open("service-account.json")
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            googleCredentials.refreshIfExpired()
            googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
