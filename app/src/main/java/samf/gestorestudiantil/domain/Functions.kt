package samf.gestorestudiantil.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ============ FORMATEO DE FECHA ============ //
fun formatearFechaParaMostrar(fechaIso: String): String {
    if (fechaIso.isBlank()) return ""
    return try {
        // Divide "2025/01/01" y reordena
        val partes = fechaIso.split("/")
        "${partes[2]}/${partes[1]}/${partes[0]}" // Retorna "01/01/2025"
    } catch (e: Exception)
    {
        e.printStackTrace()
        fechaIso // Si falla, devuelve la original
    }
}

fun compararFechaActual(fecha: String): Boolean {
    val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")
    val fechaActual = LocalDate.now()
    return LocalDate.parse(fecha, formatter) <= fechaActual
}

fun formatearStatsPokemon(stats: String): String
{
    val statSlot = stats.split("|")
    var stat = ""

    statSlot.forEach { item ->
        val slot = item.split(": ")
        stat += slot[1].trim() +"\n"
    }
    return stat
}