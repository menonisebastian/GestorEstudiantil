package samf.gestorestudiantil.data.models

data class Post(
    val id: Int,
    val titulo: String,
    val contenido: String,
    val fecha: String,
    val profesorId: String,
    val materiaId: String
)
