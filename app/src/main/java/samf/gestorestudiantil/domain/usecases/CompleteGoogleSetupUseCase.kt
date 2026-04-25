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
        ciclo: Int,
        name: String,
        email: String,
        imgUrl: String,
        departamento: String = "Sin asignar"
    ): User {
        authRepository.updatePassword(password)

        var finalRol = rolSeleccionado
        var estadoInicial = "ACTIVO"
        var cursoGenerado = ""
        val finalDepartamento = if (departamento.isBlank()) "Sin asignar" else departamento

        if (rolSeleccionado == "ESTUDIANTE") {
            estadoInicial = "PENDIENTE"
            val letraTurno = if (turno.lowercase().contains("matutino")) "M" else "V"
            cursoGenerado = "${cursoNombre}${letraTurno}${ciclo}"
        } else if (rolSeleccionado == "PROFESOR") {
            val hasAdmins = userRepository.checkAdminsInCenter(centroId)
            if (!hasAdmins) {
                finalRol = "ADMIN"
            }
        }

        val uid = authRepository.getCurrentUserUid() ?: throw Exception("Usuario no autenticado")

        val finalName = if (name.isBlank()) authRepository.getCurrentUserName() ?: "Usuario Google" else name
        val finalEmail = if (email.isBlank()) authRepository.getCurrentUserEmail() ?: "" else email
        val finalImgUrl = if (imgUrl.isBlank()) authRepository.getCurrentUserPhotoUrl() ?: "" else imgUrl

        val newUser: User = when (finalRol) {
            "ESTUDIANTE" -> User.Estudiante(
                id = uid, nombre = finalName, email = finalEmail, centroId = centroId,
                estado = estadoInicial, imgUrl = finalImgUrl,
                cursoId = cursoId, curso = cursoGenerado, turno = turno.lowercase().trim(), cicloNum = ciclo
            )
            "PROFESOR" -> User.Profesor(
                id = uid, nombre = finalName, email = finalEmail, centroId = centroId,
                estado = estadoInicial, imgUrl = finalImgUrl, departamento = finalDepartamento,
                turno = turno.lowercase().trim()
            )
            "ADMIN" -> User.Admin(
                id = uid, nombre = finalName, email = finalEmail, centroId = centroId,
                estado = estadoInicial, imgUrl = finalImgUrl
            )
            else -> throw IllegalArgumentException("Rol no válido")
        }

        userRepository.saveUser(newUser)

        if (finalRol == "ESTUDIANTE" && cursoId.isNotEmpty()) {
            courseRepository.incrementStudentCount(cursoId, turno, ciclo)
        }

        return newUser
    }
}
