package samf.gestorestudiantil.data.repositories

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.R
import samf.gestorestudiantil.domain.repositories.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override fun getAuthStateFlow(): Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun registerUser(email: String, pass: String): String {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            result.user?.uid ?: throw Exception(context.getString(R.string.error_auth_failed))
        } catch (e: Exception) {
            if (e is FirebaseAuthUserCollisionException) {
                throw Exception(context.getString(R.string.error_email_already_in_use))
            }
            throw Exception(context.getString(R.string.error_auth_failed))
        }
    }

    override suspend fun loginWithEmail(email: String, pass: String): String {
        return try {
            val credential = EmailAuthProvider.getCredential(email, pass)
            val result = if (auth.currentUser != null) {
                auth.currentUser?.linkWithCredential(credential)?.await()
            } else {
                auth.signInWithEmailAndPassword(email, pass).await()
            }
            result?.user?.uid ?: throw Exception(context.getString(R.string.error_auth_failed))
        } catch (e: Exception) {
            throw Exception(context.getString(R.string.error_auth_failed))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): String {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return try {
            val result = if (auth.currentUser != null) {
                auth.currentUser?.linkWithCredential(credential)?.await()
            } else {
                auth.signInWithCredential(credential).await()
            }
            result?.user?.uid ?: throw Exception(context.getString(R.string.error_google_signin))
        } catch (e: Exception) {
            if (e is FirebaseAuthUserCollisionException) {
                val result = e.updatedCredential?.let { auth.signInWithCredential(it).await() }
                result?.user?.uid ?: throw Exception(context.getString(R.string.error_google_signin))
            } else {
                throw Exception(context.getString(R.string.error_google_signin))
            }
        }
    }

    override suspend fun signInWithGithub(activity: Activity): String {
        val provider = OAuthProvider.newBuilder("github.com")

        return try {
            val pendingResultTask = auth.pendingAuthResult
            val result = if (pendingResultTask != null) {
                pendingResultTask.await()
            } else {
                auth.startActivityForSignInWithProvider(activity, provider.build()).await()
            }

            val credential = result?.credential
            if (auth.currentUser != null && credential != null) {
                try {
                    auth.currentUser?.linkWithCredential(credential)?.await()
                } catch (linkEx: Exception) {
                }
            }

            result?.user?.uid ?: throw Exception(context.getString(R.string.error_github_signin))

        } catch (e: FirebaseAuthUserCollisionException) {
            Log.d("GithubAuth", "=== COLLISION EXCEPTION ===")
            Log.d("GithubAuth", "e.email: ${e.email}")
            Log.d("GithubAuth", "e.updatedCredential: ${e.updatedCredential}")
            Log.d("GithubAuth", "e.message: ${e.message}")

            var email = e.email
            Log.d("GithubAuth", "email es null: ${email == null}")

            if (email != null) {
                val methods = auth.fetchSignInMethodsForEmail(email).await().signInMethods
                Log.d("GithubAuth", "methods: $methods")
            }

            email = e.email
                ?: throw Exception(context.getString(R.string.error_github_signin))

            val githubCredential = e.updatedCredential
                ?: throw Exception(context.getString(R.string.error_github_signin))

            @Suppress("DEPRECATION")
            val methods = auth.fetchSignInMethodsForEmail(email).await().signInMethods
                ?: emptyList()

            when {
                methods.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD) -> {
                    throw Exception("COLLISION_GOOGLE:$email")
                }
                methods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD) -> {
                    throw Exception("COLLISION_EMAIL:$email")
                }
                else -> {
                    val result = auth.signInWithCredential(githubCredential).await()
                    result.user?.uid
                        ?: throw Exception(context.getString(R.string.error_github_signin))
                }
            }
        } catch (e: Exception) {
            if (e.message?.startsWith("COLLISION_") == true) throw e
            throw Exception(context.getString(R.string.error_github_signin))
        }
    }

    override suspend fun linkGithubAfterReauth(activity: Activity, existingUid: String): String {
        val provider = OAuthProvider.newBuilder("github.com")
        val result = auth.currentUser
            ?.startActivityForLinkWithProvider(activity, provider.build())
            ?.await()
            ?: throw Exception(context.getString(R.string.error_github_signin))
        return result.user?.uid ?: existingUid
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
