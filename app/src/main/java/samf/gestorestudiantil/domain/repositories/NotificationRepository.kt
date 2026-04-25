package samf.gestorestudiantil.domain.repositories

interface NotificationRepository {
    suspend fun sendTopicNotification(
        topic: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    )

    suspend fun sendTokenNotification(
        token: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    )
}
