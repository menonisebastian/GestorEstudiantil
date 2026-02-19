package samf.gestorestudiantil.models

data class Profesor(
    val id: Int,
    val nombre: String,
    val apellidos: String,
    val email: String,
    val centro: String
)

val listaProfesores = listOf(
    Profesor(1, "Eduardo", "Del Olmo", "edelolmo@iescomercio.com", "IES Comercio"),
    Profesor(2, "Oscar", "Jimenez", "oscarjimenez@iescomercio.com", "IES Comercio"),
    Profesor(3, "Miguel", "Osorio", "miguelsorio@iescomercio.com", "IES Comercio"),
    Profesor(4, "Ruben", "Almas", "rubenalmas@iescomercio.com", "IES Comercio")
)
