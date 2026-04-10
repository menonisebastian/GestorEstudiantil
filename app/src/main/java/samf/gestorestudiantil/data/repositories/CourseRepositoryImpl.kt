package samf.gestorestudiantil.data.repositories

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.domain.repositories.CourseRepository
import javax.inject.Inject

class CourseRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : CourseRepository {

    override suspend fun incrementStudentCount(cursoId: String, turno: String, ciclo: Int) {
        // Incremento en curso
        db.collection("cursos").document(cursoId)
            .update("numEstudiantes", FieldValue.increment(1))
                    
        // Incremento en asignaturas
        val snapshot = db.collection("asignaturas")
            .whereEqualTo("cursoId", cursoId)
            .whereEqualTo("ciclo", ciclo) 
            .whereEqualTo("turno", turno)
            .get().await()
            
        for (doc in snapshot.documents) {
            doc.reference.update("numEstudiantesCurso", FieldValue.increment(1))
        }
    }

    override suspend fun getCentros(): List<Centro> {
        val snapshot = db.collection("centros").get().await()
        return snapshot.toObjects(Centro::class.java)
    }

    override suspend fun getCursosPorCentro(centroId: String): List<Curso> {
        val snapshot = db.collection("cursos")
            .whereEqualTo("centroId", centroId)
            .get().await()
        return snapshot.toObjects(Curso::class.java)
    }
}
