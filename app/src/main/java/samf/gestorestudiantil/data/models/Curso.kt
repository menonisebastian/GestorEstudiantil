package samf.gestorestudiantil.data.models

import com.google.firebase.firestore.PropertyName

data class Curso(
    var id: String = "",
    var acronimo: String = "",          // "DAM", "DAW", "ASIR", "MKT" ...
    var nombre: String = "",            // Nombre completo del curso
    var descripcion: String = "",
    var centroId: String = "",
    var tipo: String = "",              // "FP Grado Superior", "FP Grado Medio", etc.
    var modalidad: String = "",         // "presencial", "dual"
    var turnosDisponibles: List<String> = emptyList(), // ["matutino"], ["vespertino"], ["matutino","vespertino"]
    var urlInfo: String = "",
    var horasTotalesCurso: Int = 0,
    // Estilos visuales almacenados como String en Firestore
    var iconoName: String = "School",
    var colorFondoHex: String = "#D0E1FF",
    var colorIconoHex: String = "#2563EB"
)

// Datos de ejemplo con estilos reales
val listaCursos = listOf(
    Curso("1",  "DAM",  "Desarrollo de Aplicaciones Multiplataforma",      "", "ies_comercio", "FP Grado Superior",          "presencial", listOf("matutino", "vespertino"), "", 2030, "AppsOutlined",          "#D0E1FF", "#2563EB"),
    Curso("2",  "DAW",  "Desarrollo de Aplicaciones Web",                  "", "ies_comercio", "FP Grado Superior",          "presencial", listOf("vespertino"),             "", 2030, "LanguageOutlined",      "#DDE1FF", "#4338CA"),
    Curso("3",  "ASIR", "Administración de Sistemas Informáticos y Redes", "", "ies_comercio", "FP Grado Superior",          "presencial", listOf("matutino"),               "", 2030, "DnsOutlined",           "#D0F4FF", "#0891B2"),
    Curso("4",  "DUAL", "Doble titulación DAW + DAM",                      "", "ies_comercio", "FP Grado Superior",          "dual",       listOf("vespertino"),             "", 0,    "MergeTypeOutlined",     "#DDE1FF", "#4338CA"),
    Curso("5",  "SMR",  "Sistemas Microinformáticos y Redes",              "", "ies_comercio", "FP Grado Medio",             "presencial", listOf("matutino", "vespertino"), "", 2020, "RouterOutlined",        "#D0F4FF", "#0891B2"),
    Curso("6",  "MKT",  "Marketing y Publicidad",                          "", "ies_comercio", "FP Grado Superior",          "presencial", listOf("vespertino"),             "", 2070, "CampaignOutlined",      "#FFE0F0", "#DB2777"),
    Curso("7",  "AF",   "Administración y Finanzas",                       "", "ies_comercio", "FP Grado Superior",          "dual",       listOf("matutino"),               "", 2030, "AccountBalance",        "#FFFBD0", "#CA8A04"),
    Curso("8",  "CI",   "Comercio Internacional",                          "", "ies_comercio", "FP Grado Superior",          "presencial", listOf("matutino"),               "", 2050, "PublicOutlined",        "#D0F5E1", "#16A34A"),
    Curso("9",  "AC",   "Actividades Comerciales",                         "", "ies_comercio", "FP Grado Medio",             "presencial", listOf("matutino"),               "", 2060, "ShoppingCartOutlined",  "#FFE8D8", "#EA580C"),
    Curso("10", "GA",   "Gestión Administrativa",                          "", "ies_comercio", "FP Grado Medio",             "presencial", listOf("matutino"),               "", 2060, "DescriptionOutlined",   "#FFFBD0", "#CA8A04"),
    Curso("11", "IABD", "Inteligencia Artificial y Big Data",              "", "ies_comercio", "Curso de especialización FP","presencial", listOf("vespertino"),             "", 530,  "PsychologyOutlined",    "#EDE0FF", "#7C3AED"),
    Curso("12", "CSI",  "Ciberseguridad en Entornos de las TI",            "", "ies_comercio", "Curso de especialización FP","presencial", listOf("vespertino"),             "", 560,  "GppGoodOutlined",       "#FFD8D8", "#DC2626"),
    Curso("13", "VRG",  "Desarrollo de Videojuegos y Realidad Virtual",    "", "ies_comercio", "Curso de especialización FP","presencial", listOf("vespertino"),             "", 465,  "SportsEsports",         "#EDE0FF", "#7C3AED"),
    Curso("14", "IC",   "Informática y Comunicaciones",                    "", "ies_comercio", "FP Grado Básico",            "presencial", listOf("vespertino"),             "", 2045, "MemoryOutlined",        "#D0E1FF", "#2563EB"),
    Curso("15", "SC",   "Servicios Comerciales",                           "", "ies_comercio", "FP Grado Básico",            "presencial", listOf("matutino"),               "", 2040, "StorefrontOutlined",    "#FFE8D8", "#EA580C"),
    Curso("16", "SA",   "Servicios Administrativos",                       "", "ies_comercio", "FP Grado Básico",            "presencial", listOf("matutino"),               "", 2040, "FolderOpenOutlined",    "#FFFBD0", "#CA8A04"),
)