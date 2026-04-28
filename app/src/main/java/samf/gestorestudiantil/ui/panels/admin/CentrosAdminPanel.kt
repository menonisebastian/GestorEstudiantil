package samf.gestorestudiantil.ui.panels.admin

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import samf.gestorestudiantil.R
import samf.gestorestudiantil.data.models.User
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import samf.gestorestudiantil.ui.components.AsignaturaCard
import java.util.Locale
import androidx.core.graphics.toColorInt
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.AsignaturaCard
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminState
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel
import samf.gestorestudiantil.ui.components.AccImg
import samf.gestorestudiantil.ui.components.CustomSearchBar
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.domain.capitalize

@Composable
fun AdminHeader(titulo: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    ) {
        Text(
            text = titulo,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        )
    }
}

@Composable
fun CentrosListScreen(
    adminState: AdminState,
    adminViewModel: AdminViewModel,
    onCentroClick: (Centro) -> Unit,
    onEditCentro: (Centro) -> Unit
) {
    var searchTextRaw by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(searchTextRaw) {
        delay(300)
        searchText = searchTextRaw
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (adminState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            val filteredCentros by remember(searchText, adminState.centros) {
                derivedStateOf { adminState.centros.filter { it.nombre.contains(searchText, ignoreCase = true) } }
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 160.dp, bottom = 160.dp, start = 16.dp, end = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = filteredCentros,
                    key = { it.id }
                ) { centro ->
                    CentroCard(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(150),
                            fadeOutSpec = tween(150),
                            placementSpec = tween(150)
                        ),
                        centro = centro, 
                        onClick = { onCentroClick(centro) }, 
                        onEdit = { onEditCentro(centro) }
                    )
                }
            }
        }

        // Header Flotante
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.admin_management_centers), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                CustomSearchBar(textoBusqueda = searchTextRaw, onValueChange = { searchTextRaw = it }, onFilterClick = {})
            }
        }
    }
}

@Composable
fun TiposCursoScreen(
    centro: Centro,
    adminState: AdminState,
    onTipoClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val tipos by remember(adminState.cursos) {
            derivedStateOf { adminState.cursos.mapNotNull { it.tipo }.distinct().sorted() }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 100.dp, bottom = 160.dp, start = 16.dp, end = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tipos) { tipo -> TipoCursoCard(tipo) { onTipoClick(tipo) } }
        }

        // Header Flotante
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = stringResource(R.string.admin_courses_in, centro.nombre),
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun CursosScreen(
    tipo: String,
    adminState: AdminState,
    onCursoClick: (Curso) -> Unit,
    onEditCurso: (Curso) -> Unit,
) {
    val headerTitle = if (tipo.contains("Curso", ignoreCase = true)) tipo else stringResource(R.string.admin_courses_of, tipo)
    Box(modifier = Modifier.fillMaxSize()) {
        val cursos by remember(tipo, adminState.cursos) {
            derivedStateOf { adminState.cursos.filter { it.tipo == tipo } }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 100.dp, bottom = 160.dp, start = 16.dp, end = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(cursos) { curso -> CursoCard(curso = curso, onClick = { onCursoClick(curso) }, onEdit = { onEditCurso(curso) }) }
        }

        // Header Flotante
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = headerTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
fun TurnosScreen(
    curso: Curso,
    onTurnoClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader(stringResource(R.string.admin_shifts_of, curso.acronimo))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 160.dp)
        ) {
            items(curso.turnosDisponibles) { turno ->
                TipoCursoCard(turno.capitalize() ) { onTurnoClick(turno) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CiclosScreen(
    curso: Curso,
    turno: String,
    adminState: AdminState,
    onVerHorario: (String) -> Unit,
    onEditAsignatura: (Asignatura) -> Unit,
    onAsignaturaClick: (Asignatura) -> Unit,
    onUserClick: (User) -> Unit,
    onAsignarTutor: (String, String) -> Unit,
) {
    val asignaturasFiltradas = adminState.asignaturas
    val ciclos by remember(asignaturasFiltradas) {
        derivedStateOf { asignaturasFiltradas.map { it.ciclo }.distinct().sortedBy { it } }
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val currentCiclo = ciclos.getOrNull(selectedTabIndex) ?: ""
    val asignaturasDelCiclo by remember(asignaturasFiltradas, currentCiclo) {
        derivedStateOf { asignaturasFiltradas.filter { it.ciclo == currentCiclo } }
    }
    val profesorEjemplo by remember(asignaturasDelCiclo) {
        derivedStateOf { asignaturasDelCiclo.firstOrNull { it.profesorNombre.isNotEmpty() } }
    }
    val currentCicloNum = currentCiclo.trim().firstOrNull()?.toString()?.toIntOrNull() ?: 1
    val claseReal = adminState.clases.find {
        it.cursoGlobalId == curso.id &&
                it.turno.lowercase().trim() == turno.lowercase().trim() &&
                it.cicloNum == currentCicloNum
    }
    val idClaseTitulo = claseReal?.id ?: "${curso.acronimo}${if (turno.lowercase().trim() == "matutino") "M" else "V"}${currentCicloNum}".uppercase()

    Box(modifier = Modifier.fillMaxSize()) {
        if (ciclos.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 220.dp, bottom = 160.dp, start = 16.dp, end = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = asignaturasDelCiclo,
                    key = { it.id.ifEmpty { it.idDocumento } }
                ) { asignatura ->
                    AsignaturaCard(
                        modifier = Modifier.animateItem(
                            fadeInSpec = tween(150),
                            fadeOutSpec = tween(150),
                            placementSpec = tween(150)
                        ),
                        asignatura = asignatura,
                        userRole = "ADMIN",
                        onClick = { onAsignaturaClick(asignatura) },
                        onEdit = { onEditAsignatura(asignatura) }
                    )
                }
            }

            // Cabezal Flotante (Título + Tabs + Tutor)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor.copy(alpha = 0.95f))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(text = stringResource(R.string.admin_cycles_of, idClaseTitulo), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                        
                        // Tarjeta del Tutor dentro del área flotante
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = backgroundColor.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp),
                            onClick = {
                                profesorEjemplo?.let { asig ->
                                    val prof = adminState.usuarios.find { it.id == asig.profesorId } ?: User.Profesor(id = asig.profesorId, nombre = asig.profesorNombre)
                                    onUserClick(prof)
                                }
                            }
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AccImg(userName = profesorEjemplo?.profesorNombre ?: stringResource(R.string.admin_no_tutor_assigned), imgUrl = "", size = 32.dp, onClick = {})
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = profesorEjemplo?.profesorNombre ?: stringResource(R.string.admin_no_tutor_assigned), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                                    Text(text = stringResource(R.string.admin_tutor_of_course), fontSize = 10.sp, color = surfaceDimColor)
                                }
                                IconButton(onClick = { 
                                    val turnoLetra = if (turno.lowercase().trim() == "matutino") "M" else "V"
                                    onAsignarTutor("${curso.acronimo}${turnoLetra}${currentCicloNum}".uppercase(), curso.centroId) 
                                }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                }
                                Button(
                                    onClick = { onVerHorario(currentCiclo) },
                                    contentPadding = PaddingValues(horizontal = 8.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(30.dp),
                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.1f), contentColor = primaryColor)
                                ) {
                                    Icon(Icons.Default.Schedule, null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(stringResource(R.string.admin_schedule), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                SecondaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = textColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ciclos.forEachIndexed { index, ciclo ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(stringResource(R.string.admin_cycle_n, ciclo), fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.admin_no_subjects_configured), color = surfaceDimColor)
            }
        }
    }
}

@Composable
fun AsignaturasScreen(
    ciclo: String,
    adminState: AdminState,
    onAsignaturaClick: (Asignatura) -> Unit,
    onEditAsignatura: (Asignatura) -> Unit,
) {
    var searchTextRaw by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(searchTextRaw) {
        delay(300)
        searchText = searchTextRaw
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val filteredAsignaturas by remember(ciclo, searchText, adminState.asignaturas) {
            derivedStateOf {
                adminState.asignaturas.filter {
                    it.ciclo == ciclo && (it.nombre.contains(searchText, ignoreCase = true) || it.acronimo.contains(searchText, ignoreCase = true))
                }
            }
        }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 160.dp, bottom = 160.dp, start = 16.dp, end = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                items = filteredAsignaturas,
                key = { it.id.ifEmpty { it.idDocumento } }
            ) { asignatura ->
                AsignaturaCard(
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(150),
                        fadeOutSpec = tween(150),
                        placementSpec = tween(150)
                    ),
                    asignatura = asignatura,
                    userRole = "ADMIN",
                    onClick = { onAsignaturaClick(asignatura) },
                    onEdit = { onEditAsignatura(asignatura) }
                )
            }
        }

        // Header Flotante
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.95f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.admin_subjects_cycle, ciclo), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                CustomSearchBar(textoBusqueda = searchTextRaw, onValueChange = { searchTextRaw = it }, onFilterClick = {})
            }
        }
    }
}

@Composable
fun HorariosAdminScreen(
    curso: Curso,
    ciclo: String,
    turno: String,
    adminState: AdminState,
    onEditHorario: (Horario) -> Unit,
) {
    val slots = if (turno == "matutino") Horario.HORAS_MATUTINO else Horario.HORAS_VESPERTINO
    val dias = Horario.DIAS_SEMANA
    val cicloNumInt = ciclo.trim().firstOrNull()?.toString()?.toIntOrNull() ?: 1

    // Buscamos la clase real en el estado para usar su ID
    val claseReal = adminState.clases.find {
        it.cursoGlobalId == curso.id &&
                it.turno.lowercase().trim() == turno.lowercase().trim() &&
                it.cicloNum == cicloNumInt
    }
    val claseId = claseReal?.id ?: "${curso.acronimo}${if (turno.lowercase().trim().contains("matutino")) "M" else "V"}${cicloNumInt}".uppercase()

    val horarioMap by remember(adminState.horarios, adminState.asignaturas) {
        derivedStateOf {
            adminState.horarios.associateBy { "${it.dia}_${it.horaInicio}_${it.horaFin}" }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader(stringResource(R.string.admin_schedule_title, turno.capitalize(), claseId))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 160.dp)) {
            items(slots) { slot ->
                val isReceso = slot.contains("11:10 - 11:35") || slot.contains("18:40 - 19:05")
                if (!isReceso) {
                    Text(text = slot, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp), color = primaryColor)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        dias.forEach { dia ->
                            val key = "${dia}_${slot.split(" - ")[0]}_${slot.split(" - ")[1]}"
                            val h = horarioMap[key]
                            val asig = adminState.asignaturas.find { it.id == h?.asignaturaId }
                            Box(modifier = Modifier.weight(1f).height(60.dp), contentAlignment = Alignment.Center) {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(containerColor = if (asig != null) Color(asig.colorFondoHex.toColorInt()) else Color.LightGray.copy(alpha = 0.3f)),
                                    onClick = {
                                        val nuevoHorario = h ?: Horario(cursoId = curso.id, cicloNum = cicloNumInt, turno = turno, dia = dia, horaInicio = slot.split(" - ")[0], horaFin = slot.split(" - ")[1])
                                        onEditHorario(nuevoHorario)
                                    }
                                ) {
                                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = dia.take(1), fontSize = 10.sp, color = Color.Gray)
                                        Text(text = h?.asignaturaAcronimo ?: "-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black.copy(alpha = 0.8f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun CentroCard(
    modifier: Modifier = Modifier,
    centro: Centro, 
    onClick: () -> Unit, 
    onEdit: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Business, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = centro.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                val addressStr = try { centro.direccion } catch (e: Exception) { "" }
                if (addressStr.isNotEmpty()) {
                    Text(text = addressStr, fontSize = 12.sp, color = Color.Gray)
                }
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun TipoCursoCard(tipo: String, onClick: () -> Unit) {
    val icon = when {
        tipo.contains("Matutino", ignoreCase = true) -> Icons.Default.LightMode
        tipo.contains("Vespertino", ignoreCase = true) -> Icons.Default.WbTwilight
        tipo.contains("Especialización", ignoreCase = true) -> Icons.Default.AutoAwesome
        tipo.contains("Superior", ignoreCase = true) -> Icons.Default.School
        tipo.contains("Medio", ignoreCase = true) -> Icons.AutoMirrored.Filled.MenuBook
        tipo.contains("Básico", ignoreCase = true) -> Icons.Default.ImportContacts
        else -> Icons.Default.Category
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = tipo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun CursoCard(curso: Curso, onClick: () -> Unit, onEdit: (() -> Unit)? = null) {
    val containerColor = try {
        Color(curso.colorFondoHex.toColorInt())
    } catch (e: Exception) {
        surfaceColor
    }
    val iconColor = try {
        Color(curso.colorIconoHex.toColorInt())
    } catch (e: Exception) {
        primaryColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = curso.iconoName.toComposeIcon(),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = curso.acronimo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                if (curso.nombre.isNotEmpty()) {
                    Text(text = curso.nombre, fontSize = 12.sp, color = Color.Gray)
                }
            }
            if (onEdit != null) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Gray)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.Gray)
        }
    }
}
