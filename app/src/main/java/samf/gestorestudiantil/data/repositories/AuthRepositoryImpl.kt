package samf.gestorestudiantil.data.repositories

import android.app.Activity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth
) : AuthRepository {

    override fun getAuthStateFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun registerUser(email: String, pass: String): String {
        val result = auth.createUserWithEmailAndPassword(email, pass).await()
        return result.user?.uid ?: throw Exception("Auth error")
    }

    override suspend fun loginWithEmail(email: String, pass: String): String {
        val credential = EmailAuthProvider.getCredential(email, pass)
        val result = if (auth.currentUser != null) {
            auth.currentUser?.linkWithCredential(credential)?.await()
        } else {
            auth.signInWithEmailAndPassword(email, pass).await()
        }
        return result?.user?.uid ?: throw Exception("Login error")
    }

    override suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            val result = if (auth.currentUser != null) {
                auth.currentUser?.linkWithCredential(credential)?.await()
            } else {
                auth.signInWithCredential(credential).await()
            }
            result?.user?.uid ?: throw Exception("Google Sign-In error")
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            // El email ya existe con otro proveedor. Usamos la credencial de la excepción para entrar.
            val result = e.updatedCredential?.let { auth.signInWithCredential(it).await() }
            result?.user?.uid ?: throw e
        }
    }

    override suspend fun signInWithGithub(activity: Activity): String {
        val provider = OAuthProvider.newBuilder("github.com")

        val pendingResultTask = auth.pendingAuthResult
        return try {
            val result = if (pendingResultTask != null) {
                pendingResultTask.await()
            } else {
                auth.startActivityForSignInWithProvider(activity, provider.build()).await()
            }
            result?.user?.uid ?: throw Exception("Github Sign-In error")
        } catch (e: com.google.firebase.auth.FirebaseAuthUserCollisionException) {
            // El email ya existe con otro proveedor. Usamos la credencial de la excepción para entrar.
            val result = e.updatedCredential?.let { auth.signInWithCredential(it).await() }
            result?.user?.uid ?: throw e
        }
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

    override fun signOut() {
        auth.signOut()
    }
}
