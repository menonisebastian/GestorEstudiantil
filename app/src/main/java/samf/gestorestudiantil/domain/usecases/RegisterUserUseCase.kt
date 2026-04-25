package samf.gestorestudiantil.domain.usecases

import android.content.Context
import com.google.auth.oauth2.GoogleCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.domain.repositories.AuthRepository
import samf.gestorestudiantil.domain.repositories.CourseRepository
import samf.gestorestudiantil.domain.repositories.UserRepository
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val courseRepository: CourseRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        email: String, pass: String, name: String,
        rolSeleccionado: String, centroId: String, cursoId: String,
        cursoNombre: String, turno: String, ciclo: Int, imgUrl: String,
        departamento: String = ""
    ): User {
        // 1. LÓGICA DE NEGOCIO: Determinar rol, estado y área
        var finalRol = rolSeleccionado
        var estadoInicial = "ACTIVO"
        var cursoGenerado = ""
        val finalDepartamento = departamento.ifEmpty { "Sin asignar" }

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
                estado = estadoInicial, imgUrl = imgUrl, departamento = finalDepartamento,
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

        // 5. NOTIFICAR AL ADMIN
        notificarAdminNuevoRegistro(newUser)

        return newUser
    }

    private suspend fun notificarAdminNuevoRegistro(nuevoUsuario: User)
    {
        withContext(Dispatchers.IO) {
            try {
                val admins = userRepository.getAdminsInCenter(nuevoUsuario.centroId)
                val tokens = admins.map { it.fcmToken }.filter { it.isNotEmpty() }
                
                if (tokens.isEmpty()) return@withContext

                val accessToken = getAccessToken() ?: return@withContext
                val client = OkHttpClient()

                val notificationTitle = "Nuevo registro de usuario"
                val notificationBody = "${nuevoUsuario.nombre} se ha registrado como ${nuevoUsuario.rol}"

                for (token in tokens) {
                    val json = JSONObject().apply {
                        put("message", JSONObject().apply {
                            put("token", token)
                            put("notification", JSONObject().apply {
                                put("title", notificationTitle)
                                put("body", notificationBody)
                            })
                            put("data", JSONObject().apply {
                                put("type", "nuevo_registro")
                                put("usuarioId", nuevoUsuario.id)
                            })
                        })
                    }

                    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    val request = Request.Builder()
                        .url("https://fcm.googleapis.com/v1/projects/gestorinstituto-tfg/messages:send")
                        .post(body)
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (!response.isSuccessful) {
                            println("Error enviando notificación al admin: ${response.body?.string()}")
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getAccessToken(): String? {
        return try {
            val inputStream = context.assets.open("service-account.json")
            val googleCredentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            googleCredentials.refreshIfExpired()
            googleCredentials.accessToken.tokenValue
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
