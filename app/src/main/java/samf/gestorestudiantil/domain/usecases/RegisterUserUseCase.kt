package samf.gestorestudiantil.domain.usecases

import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AuthRepository
import samf.gestorestudiantil.domain.repositories.NotificationRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) {
    suspend operator fun invoke(
        email: String, pass: String, name: String,
        rolSeleccionado: String, centroId: String, cursoId: String,
        cursoNombre: String, turno: String, ciclo: Int, imgUrl: String,
        departamento: String = "",
    ): User {
        var finalRol = rolSeleccionado
        var estadoInicial = "ACTIVO"
        var cursoGenerado = ""
        val finalDepartamento = departamento.ifEmpty { "Sin asignar" }

        if (rolSeleccionado == "ESTUDIANTE") {
            estadoInicial = "PENDIENTE"
            val letraTurno = if (turno.lowercase().contains("matutino")) "M" else "V"
            cursoGenerado = "$cursoNombre$letraTurno$ciclo"
        } else if (rolSeleccionado == "PROFESOR") {
            val hasAdmins = userRepository.checkAdminsInCenter(centroId)
            if (!hasAdmins) {
                finalRol = "ADMIN"
            }
        }

        val uid = authRepository.registerUser(email, pass)

        val newUser: User = when (finalRol) {
            "ESTUDIANTE" -> User.Estudiante(
                id = uid, nombre = name, email = email, centroId = centroId,
                estado = estadoInicial, imgUrl = imgUrl,
                cursoId = cursoId, curso = cursoGenerado, turno = turno.lowercase().trim(), cicloNum = ciclo
            )
            "PROFESOR" -> User.Profesor(
                id = uid, nombre = name, email = email, centroId = centroId,
                estado = estadoInicial, imgUrl = imgUrl, departamento = finalDepartamento,
                turno = turno.lowercase().trim()
            )
            "ADMIN" -> User.Admin(
                id = uid, nombre = name, email = email, centroId = centroId,
                estado = estadoInicial, imgUrl = imgUrl
            )
            else -> throw IllegalArgumentException("Rol no válido")
        }

        userRepository.saveUser(newUser)

        notificarAdminNuevoRegistro(newUser)

        return newUser
    }

    private suspend fun notificarAdminNuevoRegistro(nuevoUsuario: User) {
        val admins = userRepository.getAdminsInCenter(nuevoUsuario.centroId)
        val tokens = admins.asSequence().map { it.fcmToken }.filter { it.isNotEmpty() }.toList()
        
        if (tokens.isEmpty()) return

        val notificationTitle = "Nuevo registro de usuario"
        val notificationBody = "${nuevoUsuario.nombre} se ha registrado como ${nuevoUsuario.rol}"
        val data = mapOf(
            "type" to "nuevo_registro",
            "usuarioId" to nuevoUsuario.id
        )

        for (token in tokens) {
            notificationRepository.sendTokenNotification(token, notificationTitle, notificationBody, data)
        }
    }
}
