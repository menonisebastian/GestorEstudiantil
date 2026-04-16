package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : UserRepository {
    
    override suspend fun saveUser(user: User) {
        db.collection("usuarios").document(user.id).set(user).await()
    }

    override suspend fun getUser(uid: String): User? {
        val doc = db.collection("usuarios").document(uid).get().await()
        
        if (!doc.exists()) return null

        val rol = doc.getString("rol")

        return when (rol) {
            "ESTUDIANTE" -> doc.toObject(User.Estudiante::class.java)
            "PROFESOR" -> doc.toObject(User.Profesor::class.java)
            "ADMIN" -> doc.toObject(User.Admin::class.java)
            else -> doc.toObject(User.Incompleto::class.java)
        }
    }

    override fun getUserFlow(uid: String): Flow<User?> = callbackFlow {
        val registration = db.collection("usuarios").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val rol = snapshot.getString("rol")
                    val user = when (rol) {
                        "ESTUDIANTE" -> snapshot.toObject(User.Estudiante::class.java)
                        "PROFESOR" -> snapshot.toObject(User.Profesor::class.java)
                        "ADMIN" -> snapshot.toObject(User.Admin::class.java)
                        else -> snapshot.toObject(User.Incompleto::class.java)
                    }
                    trySend(user)
                } else {
                    trySend(null)
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun checkAdminsInCenter(centroId: String): Boolean {
        val admins = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .whereEqualTo("rol", "ADMIN")
            .get().await()
        return !admins.isEmpty
    }

    override suspend fun getAdminsInCenter(centroId: String): List<User.Admin> {
        val admins = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .whereEqualTo("rol", "ADMIN")
            .get().await()
        return admins.toObjects(User.Admin::class.java)
    }

    override suspend fun updateFcmToken(uid: String, token: String) {
        db.collection("usuarios").document(uid).update("fcmToken", token).await()
    }

    override suspend fun updateProfileImage(uid: String, imageUrl: String) {
        db.collection("usuarios").document(uid).update(
            "imgUrl", imageUrl
        ).await()
    }

    override suspend fun updateName(uid: String, name: String) {
        db.collection("usuarios").document(uid).update("nombre", name).await()
    }
}
