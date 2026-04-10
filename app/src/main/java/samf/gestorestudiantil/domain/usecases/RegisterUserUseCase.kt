package samf.gestorestudiantil.domain.usecases

import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AuthRepository
import samf.gestorestudiantil.domain.repositories.CourseRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(
        email: String, pass: String, name: String,
        rolSeleccionado: String, centroId: String, cursoId: String, 
        cursoNombre: String, turno: String, imgUrl: String
    ): User {
        // 1. LÓGICA DE NEGOCIO: Determinar rol, estado y área
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

        // 2. CREACIÓN EN AUTH
        val uid = authRepository.registerUser(email, pass)

        // 3. CREAR OBJETO USUARIO
        val newUser = User(
            id = uid, nombre = name, email = email,
            rol = finalRol, cursoId = if (finalRol == "ESTUDIANTE") cursoId else "", 
            cursoOArea = areaOCurso,
            centroId = centroId, estado = estadoInicial, 
            turno = turno.lowercase().trim(),
            cicloNum = 1, 
            imgUrl = imgUrl 
        )

        // 4. GUARDAR USUARIO
        userRepository.saveUser(newUser)

        // 5. LÓGICA DE NEGOCIO: Incrementar contadores si es estudiante
        if (finalRol == "ESTUDIANTE" && cursoId.isNotEmpty()) {
            courseRepository.incrementStudentCount(cursoId, turno, 1)
        }

        return newUser
    }
}
