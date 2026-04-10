package samf.gestorestudiantil.domain.repositories

import samf.gestorestudiantil.data.models.User

interface UserRepository {
    suspend fun saveUser(user: User)
    suspend fun getUser(uid: String): User?
    suspend fun checkAdminsInCenter(centroId: String): Boolean
}
