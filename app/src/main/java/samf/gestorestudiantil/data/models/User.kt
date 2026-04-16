package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
sealed class User {
    abstract val id: String
    abstract val nombre: String
    abstract val email: String
    abstract val centroId: String
    abstract val estado: String
    abstract val imgUrl: String
    abstract val fcmToken: String
    abstract val rol: String

    @Serializable
    data class Estudiante(
        override val id: String = "",
        override val nombre: String = "",
        override val email: String = "",
        override val centroId: String = "",
        override val estado: String = "ACTIVO",
        override val imgUrl: String = "",
        override val fcmToken: String = "",
        override val rol: String = "ESTUDIANTE",
        val cursoId: String = "",
        val curso: String = "",
        val turno: String = "",
        val cicloNum: Int = 1
    ) : User()

    @Serializable
    data class Profesor(
        override val id: String = "",
        override val nombre: String = "",
        override val email: String = "",
        override val centroId: String = "",
        override val estado: String = "ACTIVO",
        override val imgUrl: String = "",
        override val fcmToken: String = "",
        override val rol: String = "PROFESOR",
        val departamento: String = "",
        val turno: String = "",
        val asignaturasImpartidas: List<String> = emptyList(),
        val ultimaVezAsignaturas: Map<String, Long> = emptyMap()
    ) : User() {
        companion object {
            val DEPARTAMENTOS = listOf(
                "Actividades complementarias y extraescolares",
                "Administración y Gestión",
                "Artes plásticas",
                "Biología y geología",
                "Clásicas",
                "Comercio y marketing",
                "Economía",
                "Educación física",
                "Filosofía",
                "Física y química",
                "Formación y orientación laboral",
                "Francés",
                "Geografía e historia",
                "Informática",
                "Inglés",
                "Lengua y literatura",
                "Matemáticas",
                "Música",
                "Orientación",
                "Tecnología"
            )
        }
    }

    @Serializable
    data class Admin(
        override val id: String = "",
        override val nombre: String = "",
        override val email: String = "",
        override val centroId: String = "",
        override val estado: String = "ACTIVO",
        override val imgUrl: String = "",
        override val fcmToken: String = "",
        override val rol: String = "ADMIN"
    ) : User()

    @Serializable
    data class Incompleto(
        override val id: String = "",
        override val nombre: String = "",
        override val email: String = "",
        override val imgUrl: String = "",
        override val centroId: String = "",
        override val estado: String = "PENDIENTE",
        override val fcmToken: String = "",
        override val rol: String = ""
    ) : User()
}
