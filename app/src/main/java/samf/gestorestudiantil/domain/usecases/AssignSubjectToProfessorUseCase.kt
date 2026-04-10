package samf.gestorestudiantil.domain.usecases

import samf.gestorestudiantil.domain.repositories.AdminRepository
import javax.inject.Inject

class AssignSubjectToProfessorUseCase @Inject constructor(
    private val adminRepository: AdminRepository
) {
    suspend operator fun invoke(asignaturaId: String, profesorId: String) {
        adminRepository.asignarAsignaturaAProfesor(asignaturaId, profesorId)
    }
}
