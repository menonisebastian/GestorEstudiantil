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
        cursoNombre: String, turno: String, ciclo: Int, imgUrl: String
    ): User {
        // 1. LÓGICA DE NEGOCIO: Determinar rol, estado y área
        var finalRol = rolSeleccionado
        var estadoInicial = "ACTIVO"
        var cursoGenerado = ""
        val departamento = "Sin asignar"

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

        // 2. CREACIÓN EN AUTH
        val uid = authRepository.registerUser(email, pass)

        // 3. CREAR OBJETO USUARIO
        val newUser: User = when (finalRol) {
            "ESTUDIANTE" -> User.Estudiante(
                id = uid, nombre = name, email = email, centroId = centroId,
                estado = estadoInicial, imgUrl = imgUrl,
                cursoId = cursoId, curso = cursoGenerado, turno = turno.lowercase().trim(), cicloNum = ciclo
            )
            "PROFESOR" -> User.Profesor(
                id = uid, nombre = name, email = email, centroId = centroId,
                estado = estadoInicial, imgUrl = imgUrl, departamento = departamento,
                turno = turno.lowercase().trim()
            )
            "ADMIN" -> User.Admin(
                id = uid, nombre = name, email = email, centroId = centroId,
                estado = estadoInicial, imgUrl = imgUrl
            )
            else -> throw IllegalArgumentException("Rol no válido")
        }

        // 4. GUARDAR USUARIO
        userRepository.saveUser(newUser)

        // 5. LÓGICA DE NEGOCIO: Incrementar contadores si es estudiante
        if (finalRol == "ESTUDIANTE" && cursoId.isNotEmpty()) {
            courseRepository.incrementStudentCount(cursoId, turno, ciclo)
        }

        return newUser
    }
}
