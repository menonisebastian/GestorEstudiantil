package samf.gestorestudiantil.domain.repositories

import kotlinx.coroutines.flow.Flow
import samf.gestorestudiantil.data.models.User

interface UserRepository {
    suspend fun saveUser(user: User)
    suspend fun getUser(uid: String): User?
    fun getUserFlow(uid: String): Flow<User?>
    suspend fun checkAdminsInCenter(centroId: String): Boolean
    suspend fun updateFcmToken(uid: String, token: String)
    suspend fun updateProfileImage(uid: String, imageUrl: String)
    suspend fun updateName(uid: String, name: String)
}
