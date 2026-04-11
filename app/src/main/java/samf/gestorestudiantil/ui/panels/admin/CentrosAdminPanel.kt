package samf.gestorestudiantil.ui.panels.admin

import android.graphics.Color.parseColor
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
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ImportContacts
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import samf.gestorestudiantil.data.models.Asignatura
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.data.models.Horario
import samf.gestorestudiantil.domain.toComposeIcon
import samf.gestorestudiantil.ui.components.AsignaturaCard
import samf.gestorestudiantil.ui.dialogs.DialogState
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminState
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel
import androidx.core.graphics.toColorInt

@Composable
fun AdminHeader(titulo: String, onBack: (() -> Unit)? = null) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBackIos, contentDescription = "Volver", tint = textColor)
            }
        }
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
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader("Gestión de Centros")
        if (adminState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                    Button(onClick = { adminViewModel.cargarDatosDesdeJsonl(context) }, modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Importar y limpiar IES Comercio")
                    }
                }
                items(adminState.centros) { centro ->
                    CentroCard(centro = centro, onClick = { onCentroClick(centro) }, onEdit = { onEditCentro(centro) })
                }
            }
        }
    }
}

@Composable
fun TiposCursoScreen(
    centro: Centro,
    adminState: AdminState,
    onTipoClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader("Cursos en ${centro.nombre}", onBack)
        val tipos = adminState.cursos.mapNotNull { it.tipo }.distinct().sorted()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(tipos) { tipo -> TipoCursoCard(tipo) { onTipoClick(tipo) } }
        }
    }
}

@Composable
fun CursosScreen(
    tipo: String,
    centroId: String,
    adminState: AdminState,
    onCursoClick: (Curso) -> Unit,
    onEditCurso: (Curso) -> Unit,
    onBack: () -> Unit
) {
    val headerTitle = if (tipo.contains("Curso", ignoreCase = true)) tipo else "Cursos de $tipo"
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader(headerTitle, onBack)
        val cursos = adminState.cursos.filter { it.tipo == tipo }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(cursos) { curso -> CursoCard(curso = curso, onClick = { onCursoClick(curso) }, onEdit = { onEditCurso(curso) }) }
        }
    }
}

@Composable
fun TurnosScreen(
    curso: Curso,
    onTurnoClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader("Turnos de ${curso.acronimo}", onBack)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(curso.turnosDisponibles) { turno ->
                TipoCursoCard(turno.replaceFirstChar { it.uppercase() }) { onTurnoClick(turno) }
            }
        }
    }
}

@Composable
fun CiclosScreen(
    curso: Curso,
    turno: String,
    adminState: AdminState,
    onVerAsignaturas: (String) -> Unit,
    onVerHorario: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader("Ciclos de ${curso.acronimo} ($turno)", onBack)
        val ciclos = adminState.asignaturas.mapNotNull { it.ciclo }.distinct().sorted()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(ciclos) { ciclo ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = surfaceColor)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Ciclo: $ciclo", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onVerAsignaturas(ciclo) }, modifier = Modifier.weight(1f)) { Text("Asignaturas") }
                            Button(onClick = { onVerHorario(ciclo) }, modifier = Modifier.weight(1f)) { Text("Horario") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AsignaturasScreen(
    curso: Curso,
    ciclo: String,
    adminState: AdminState,
    onAsignaturaClick: (Asignatura) -> Unit,
    onEditAsignatura: (Asignatura) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader("Asignaturas - $ciclo", onBack)
        val asignaturas = adminState.asignaturas.filter { it.ciclo == ciclo }
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(asignaturas) { asignatura ->
                AsignaturaCard(
                    asignatura = asignatura,
                    userRole = "ADMIN",
                    onClick = { onAsignaturaClick(asignatura) },
                    onEdit = { onEditAsignatura(asignatura) }
                )
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
    onBack: () -> Unit
) {
    val slots = if (turno == "matutino") Horario.HORAS_MATUTINO else Horario.HORAS_VESPERTINO
    val dias = Horario.DIAS_SEMANA
    val cicloNum = ciclo.trim().firstOrNull()?.digitToIntOrNull() ?: 1

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AdminHeader("Horario - $ciclo", onBack)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(slots) { slot ->
                val isReceso = slot.contains("11:10 - 11:35") || slot.contains("18:40 - 19:05")
                if (!isReceso) {
                    Text(text = slot, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp), color = primaryColor)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        dias.forEach { dia ->
                            val h = adminState.horarios.find { it.dia == dia && "${it.horaInicio} - ${it.horaFin}" == slot }
                            val asig = adminState.asignaturas.find { it.idFirestore == h?.asignaturaId }
                            Box(modifier = Modifier.weight(1f).height(60.dp), contentAlignment = Alignment.Center) {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    colors = CardDefaults.cardColors(containerColor = if (asig != null) Color(asig.colorFondoHex.toColorInt()) else Color.LightGray.copy(alpha = 0.3f)),
                                    onClick = {
                                        val nuevoHorario = h ?: Horario(cursoId = curso.id, cicloNum = cicloNum, turno = turno, dia = dia, horaInicio = slot.split(" - ")[0], horaFin = slot.split(" - ")[1])
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
fun CentroCard(centro: Centro, onClick: () -> Unit, onEdit: (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
