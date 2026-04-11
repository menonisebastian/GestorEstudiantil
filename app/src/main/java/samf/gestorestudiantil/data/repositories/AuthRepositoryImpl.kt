package samf.gestorestudiantil.data.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override suspend fun registerUser(email: String, pass: String): String {
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        return result.user?.uid ?: throw Exception("Auth error")
    }

    override suspend fun loginWithEmail(email: String, pass: String): String {
        val result = auth.signInWithEmailAndPassword(email, pass).await()
        return result.user?.uid ?: throw Exception("Login error")
    }

    override suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user?.uid ?: throw Exception("Google Sign-In error")
    }

    override suspend fun updatePassword(password: String) {
        auth.currentUser?.updatePassword(password)?.await()
    }

    override fun getCurrentUserUid(): String? {
        return auth.currentUser?.uid
    }

    override fun getCurrentUserName(): String? {
        return auth.currentUser?.displayName
    }

    override fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    override fun getCurrentUserPhotoUrl(): String? {
        return auth.currentUser?.photoUrl?.toString()
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }
}
