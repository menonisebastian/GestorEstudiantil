package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Asignatura(
    var id: String = "",
    var nombre: String = "",
    var descripcion: String = "",
    var horas: String = "",
    var profesorId: String = "", // Guardamos el ID del profe en Firestore
    var cursoId: String = "",
    var centroId: String = "",
    var ciclo: String = "", // "1", "2", "único", "2 (DAW)"
    var cicloNum: Int = 1,
    var horasTotales: Int = 0,
    var horasSemanales: Int = 0,
    // Guardamos los estilos como String en Firebase
    var iconoName: String = "PhoneIphone",
    var colorFondoHex: String = "#D0E1FF",
    var colorIconoHex: String = "#4A90E2"
)

// Datos de ejemplo
val listaAsignaturas = listOf(
    Asignatura("1", "PMM","Programación de Multimedia y Dispositivos Móviles", "5 horas", "prof_1", "1", "centro_1", "1", 1, 100, 5, "PhoneIphone", "#D0E1FF", "#4A90E2"),
    Asignatura("2", "PSP","Programación de Servicios y Procesos", "5 horas", "prof_1", "1", "centro_1", "1", 1, 100, 5, "Build", "#FFF0D0", "#F5A623"),
    Asignatura("3", "AD","Acceso a Datos", "6 horas", "prof_2", "1", "centro_1", "1", 1, 120, 6, "DataObject", "#D0F5E1", "#27AE60"),
    Asignatura("4", "DCKR","Docker", "2 horas", "prof_3", "1", "centro_1", "único", 1, 40, 2, "AllInbox", "#F5D0FF", "#9B51E0")
)
