package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FirebaseFirestore
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
        return doc.toObject(User::class.java)
    }

    override suspend fun checkAdminsInCenter(centroId: String): Boolean {
        val admins = db.collection("usuarios")
            .whereEqualTo("centroId", centroId)
            .whereEqualTo("rol", "ADMIN")
            .get().await()
        return !admins.isEmpty
    }
}
