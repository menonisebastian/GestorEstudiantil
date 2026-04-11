package samf.gestorestudiantil.domain.repositories

interface AuthRepository {
    suspend fun registerUser(email: String, pass: String): String
    suspend fun loginWithEmail(email: String, pass: String): String
    suspend fun signInWithGoogle(idToken: String): String
    suspend fun updatePassword(password: String)
    fun getCurrentUserUid(): String?
    fun getCurrentUserName(): String?
    fun getCurrentUserEmail(): String?
    fun getCurrentUserPhotoUrl(): String?
    suspend fun sendPasswordResetEmail(email: String)
}
