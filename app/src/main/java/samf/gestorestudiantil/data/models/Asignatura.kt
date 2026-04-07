package samf.gestorestudiantil.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Asignatura(
    var id: String = "",
    var acronimo: String = "",          // "PMM", "PSP", "AD", "DWEC" ...
    var nombre: String = "",            // Nombre completo de la asignatura
    var descripcion: String = "",
    var profesorId: String = "",
    var profesorNombre: String = "",
    var cursoId: String = "",
    var centroId: String = "",
    var ciclo: String = "",             // "1", "2", "único", "2 (DAW)", "2 (DAM)"
    var cicloNum: Int = 1,              // Int para ordenar/filtrar: 1 o 2
    var turno: String = "",             // "matutino", "vespertino"
    var horasTotales: Int = 0,
    var horasSemanales: Int = 0,
    // Estilos visuales almacenados como String en Firestore
    var iconoName: String = "Class",
    var colorFondoHex: String = "#E8E8E8",
    var colorIconoHex: String = "#6B7280"
)

// Datos de ejemplo — DAM ciclo 2 completo con estilos reales
val listaAsignaturas = listOf(
    Asignatura("1", "AD",   "Acceso a Datos",                                  "", "prof_1", "Nombre Prof 1", "dam_id", "ies_comercio", "2", 2, "matutino", 125, 6,  "DataObject",        "#D0F5E1", "#16A34A"),
    Asignatura("2", "DI",   "Desarrollo de Interfaces",                        "", "prof_2", "Nombre Prof 2", "dam_id", "ies_comercio", "2", 2, "matutino", 140, 7,  "Brush",             "#FFE0F0", "#DB2777"),
    Asignatura("3", "PMM",  "Programación Multimedia y Dispositivos Móviles",  "", "prof_3", "Nombre Prof 3", "dam_id", "ies_comercio", "2", 2, "matutino", 85,  4,  "PhoneIphone",       "#D0E1FF", "#2563EB"),
    Asignatura("4", "PSP",  "Programación de Servicios y Procesos",            "", "prof_3", "Nombre Prof 3", "dam_id", "ies_comercio", "2", 2, "matutino", 70,  4,  "Build",             "#FFF0D0", "#D97706"),
    Asignatura("5", "SGE",  "Sistemas de Gestión Empresarial",                 "", "prof_4", "Nombre Prof 4", "dam_id", "ies_comercio", "2", 2, "matutino", 120, 6,  "BusinessCenter",    "#FFFBD0", "#CA8A04"),
    Asignatura("6", "EIE",  "Empresa e Iniciativa Emprendedora",               "", "prof_5", "Nombre Prof 5", "dam_id", "ies_comercio", "2", 2, "matutino", 60,  3,  "LightbulbOutlined", "#FFF0D0", "#D97706"),
    Asignatura("7", "PDAM", "Proyecto de Desarrollo de Aplicaciones Multiplataforma", "", "prof_1", "Nombre Prof 1", "dam_id", "ies_comercio", "2", 2, "matutino", 30, 0, "AppsOutlined",  "#D0E1FF", "#2563EB"),
    Asignatura("8", "FCT",  "Formación en Centros de Trabajo",                 "", "",       "",              "dam_id", "ies_comercio", "2", 2, "matutino", 400, 0,  "ApartmentOutlined", "#E8E8E8", "#6B7280"),
)