package samf.gestorestudiantil.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Grading
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.ui.components.CursoCardItem
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.models.listaCursos
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var textoBusqueda by remember { mutableStateOf("") }

    var name by remember { mutableStateOf("") }

    var role by remember { mutableStateOf("") }

    val itemsEstudiante: Map<String, ImageVector> = mapOf(
        "Materias" to Icons.Default.Class,
        "Horarios" to Icons.Default.Schedule,
        "Notas" to Icons.AutoMirrored.Filled.Grading,
        "Alarmas" to Icons.Default.Notifications
    )

    val itemsProfesor: Map<String, ImageVector> = mapOf(
        "Materias" to Icons.Default.Class,
        "Horarios" to Icons.Default.Schedule,
        "Alarmas" to Icons.Default.Notifications
    )

    val itemsAdmin: Map<String, ImageVector> = mapOf(
        "Usuarios" to Icons.Outlined.Person,
        "Centros" to Icons.Default.Business,
        "Cursos" to Icons.AutoMirrored.Filled.List,
        "Alarmas" to Icons.Outlined.Notifications
    )

    val cursosFiltrados = remember(textoBusqueda) {
        if (textoBusqueda.isBlank()) listaCursos
        else listaCursos.filter { it.nombre.contains(textoBusqueda, ignoreCase = true) }
    }

    name = "Sebastian"
    role = "Estudiante"


    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {

                },
                navigationIcon = {
                    AccBox(name, role)
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.MoreHoriz,
                        "Opciones",
                        tint = textColor
                    ) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                BottomBar(itemsEstudiante)
            }
        }
    ) { paddingValues ->
        // Columna Principal (Sin padding horizontal global)
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // BLOQUE 1: Contenido con márgenes (Agrupado)
            // Aquí metemos todo lo que SÍ necesita márgenes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp) // <--- Un solo padding para todo este bloque
            ) {
                // Barra de Búsqueda (ya no necesita padding individual)
                OutlinedTextField(
                    value = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    placeholder = { Text("Search", color = surfaceDimColor) },
                    leadingIcon = { Icon(Icons.Default.Search, "Buscar", tint = Color.Gray) },
                    trailingIcon = { Icon(Icons.Outlined.FilterList, "Filtrar", tint = Color.Gray) },
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = surfaceColor,
                        unfocusedContainerColor = surfaceColor,
                        disabledContainerColor = surfaceColor,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Título (ya no necesita padding individual)
                Text(
                    text = "Mis cursos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // BLOQUE 2: Contenido Borde a Borde (Fuera del bloque con padding)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 20.dp), // Mantiene el alineamiento visual
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cursosFiltrados) { curso ->
                    CursoCardItem(curso)
                }
            }

            // Mensaje vacío (reutilizando el padding del bloque 1 si quisiéramos,
            // o aplicándolo aquí si es un caso especial)
            if (cursosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No se encontraron cursos", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun BottomBar(items: Map<String, ImageVector>) {

    var selectedItem by remember { mutableStateOf(items.keys.first()) }

    // Usamos Row para distribuir los botones horizontalmente
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor), // Asegúrate de que esta variable sea accesible o usa MaterialTheme.colorScheme.background
        horizontalArrangement = Arrangement.SpaceEvenly, // Distribuye el espacio equitativamente
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Recorremos el mapa (key = nombre, value = icono)
        items.forEach { (label, icon) ->
            BottomBarButton(
                icon = icon,
                label = label,
                selected = selectedItem == label,
                onClick = {
                    // Aquí iría la lógica al pulsar cada botón
                    // Ejemplo: navController.navigate(route)
                }
            )
        }
    }
}

@Composable
fun BottomBarButton(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .clickable(onClick = { onClick() }),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally)
        {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) textColor else surfaceDimColor,
                //modifier = Modifier.size(24.dp)
            )
            Text(label, color = if (selected) textColor else surfaceDimColor, fontSize = 12.sp)
        }
    }
}


@Composable
fun AccBox(name: String, role: String) {
    Row (horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)) {
        AccImg()
        Spacer(modifier = Modifier.width(16.dp))
        Column{
            Text("Hola, $name", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = textColor)
            Text(role, fontSize = 12.sp, color = surfaceDimColor)
        }
    }
}

@Composable
fun AccImg() {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Blue), // El color oscuro del icono
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}