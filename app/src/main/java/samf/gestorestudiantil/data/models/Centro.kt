package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Centro(
    var id: String = "",
    var nombre: String = "",
    var direccion: String = "",
    var tipo: String = ""
)
