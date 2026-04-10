package samf.gestorestudiantil.domain.usecases

import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AuthRepository
import samf.gestorestudiantil.domain.repositories.CourseRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import javax.inject.Inject

class CompleteGoogleSetupUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(
        password: String,
        rolSeleccionado: String,
        centroId: String,
        cursoId: String,
        cursoNombre: String,
        turno: String,
        name: String,
        email: String,
        imgUrl: String
    ): User {
        // 1. Actualizar contraseña
        authRepository.updatePassword(password)

        // 2. Lógica de Roles y Estados
        var finalRol = rolSeleccionado
        var estadoInicial = "ACTIVO"
        var areaOCurso = cursoNombre

        if (rolSeleccionado == "ESTUDIANTE") {
            estadoInicial = "PENDIENTE"
            val letraTurno = if (turno.lowercase().contains("matutino")) "M" else "V"
            areaOCurso = "${cursoNombre}${letraTurno}1"
        } else if (rolSeleccionado == "PROFESOR") {
            areaOCurso = "Sin asignar"
            val hasAdmins = userRepository.checkAdminsInCenter(centroId)
            if (!hasAdmins) {
                finalRol = "ADMIN"
            }
        }

        val uid = authRepository.getCurrentUserUid() ?: throw Exception("User not logged in")

        // 3. Crear objeto Usuario
        val newUser = User(
            id = uid,
            nombre = name,
            email = email,
            rol = finalRol,
            cursoId = if (rolSeleccionado == "ESTUDIANTE") cursoId else "",
            cursoOArea = areaOCurso,
            centroId = centroId,
            estado = estadoInicial,
            turno = turno.lowercase().trim(),
            cicloNum = 1,
            imgUrl = imgUrl
        )

        // 4. Guardar en Firestore
        userRepository.saveUser(newUser)

        // 5. Incrementar contadores si es estudiante
        if (finalRol == "ESTUDIANTE" && cursoId.isNotEmpty()) {
            courseRepository.incrementStudentCount(cursoId, turno, 1)
        }

        return newUser
    }
}
