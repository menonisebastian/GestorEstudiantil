package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Centro(
    var id: String = "",
    var nombre: String = "",
    var direccion: String = "",
    var tipo: String = ""
)

// Datos de ejemplo para pruebas en UI
val listaCentros = listOf(
    Centro(id = "ies_comercio", nombre = "IES Comercio", direccion = "Logroño"),
    Centro(id = "ies_batalla", nombre = "IES Batalla de Clavijo", direccion = "Logroño"),
    Centro(id = "ies_hermanos", nombre = "IES Hermanos D'Elhuyar", direccion = "Logroño")
)
