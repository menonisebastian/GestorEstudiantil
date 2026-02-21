package samf.gestorestudiantil.data.models

data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val centroId: String
)
