package samf.gestorestudiantil.ui.panels.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import samf.gestorestudiantil.data.models.Centro
import samf.gestorestudiantil.data.models.Curso
import samf.gestorestudiantil.ui.components.AsignaturaCard
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AdminViewModel

enum class AdminView {
    CENTROS, TIPOS_CURSO, CURSOS, CICLOS, ASIGNATURAS
}

@Composable
fun CentrosAdminPanel(
    paddingValues: PaddingValues,
    adminViewModel: AdminViewModel = viewModel()
) {
    val adminState by adminViewModel.adminState.collectAsState()
    val context = LocalContext.current
    
    var currentView by remember { mutableStateOf(AdminView.CENTROS) }
    var selectedCentro by remember { mutableStateOf<Centro?>(null) }
    var selectedTipoCurso by remember { mutableStateOf<String?>(null) }
    var selectedCursoBaseName by remember { mutableStateOf<String?>(null) }
    var selectedCurso by remember { mutableStateOf<Curso?>(null) }

    LaunchedEffect(Unit) {
        adminViewModel.cargarCentros()
    }

    BackHandler(enabled = currentView != AdminView.CENTROS) {
        when (currentView) {
            AdminView.ASIGNATURAS -> currentView = AdminView.CICLOS
            AdminView.CICLOS -> currentView = AdminView.CURSOS
            AdminView.CURSOS -> currentView = AdminView.TIPOS_CURSO
            AdminView.TIPOS_CURSO -> currentView = AdminView.CENTROS
            else -> { /* Do nothing */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = when (currentView) {
                    AdminView.CENTROS -> "Gestión de Centros"
                    AdminView.TIPOS_CURSO -> "Tipos de Curso en ${selectedCentro?.nombre}"
                    AdminView.CURSOS -> "Cursos de $selectedTipoCurso"
                    AdminView.CICLOS -> "Ciclos de $selectedCursoBaseName"
                    AdminView.ASIGNATURAS -> "Asignaturas de ${selectedCurso?.nombre}"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                modifier = Modifier.padding(top = 16.dp)
            )

            if (adminState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    when (currentView) {
                        AdminView.CENTROS -> {
                            item {
                                Button(
                                    onClick = { adminViewModel.cargarDatosDesdeJsonl(context) },
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text("Importar y limpiar IES Comercio")
                                } 
                            }
                            items(adminState.centros) { centro ->
                                CentroCard(centro) {
                                    selectedCentro = centro
                                    adminViewModel.cargarCursosPorCentro(centro.id)
                                    currentView = AdminView.TIPOS_CURSO
                                }
                            }
                        }
                        AdminView.TIPOS_CURSO -> {
                            val tiposUnicos = adminState.cursos.map { it.tipo }.distinct().sorted()
                            items(tiposUnicos) { tipo ->
                                TipoCursoCard(tipo) {
                                    selectedTipoCurso = tipo
                                    currentView = AdminView.CURSOS
                                }
                            }
                        }
                        AdminView.CURSOS -> {
                            val cursosPorTipo = adminState.cursos.filter { it.tipo == selectedTipoCurso }
                            val cursosBase = cursosPorTipo.map { it.nombre.replace(Regex(" \\dº"), "") }.distinct().sorted()
                            
                            items(cursosBase) { cursoBaseName ->
                                val cursoEjemplo = cursosPorTipo.first { it.nombre.startsWith(cursoBaseName) }
                                CursoCard(cursoEjemplo, isBase = true) {
                                    selectedCursoBaseName = cursoBaseName
                                    currentView = AdminView.CICLOS
                                }
                            }
                        }
                        AdminView.CICLOS -> {
                            val ciclos = adminState.cursos
                                .filter { it.nombre.startsWith(selectedCursoBaseName ?: "") }
                                .map { it.ciclo }
                                .distinct()
                                .sorted()

                            items(ciclos) { ciclo ->
                                val curso = adminState.cursos.first { it.nombre.startsWith(selectedCursoBaseName ?: "") && it.ciclo == ciclo }
                                CursoCard(curso, isBase = false) {
                                    selectedCurso = curso
                                    adminViewModel.cargarAsignaturasPorCurso(curso.id)
                                    currentView = AdminView.ASIGNATURAS
                                }
                            }
                        }
                        AdminView.ASIGNATURAS -> {
                            items(adminState.asignaturas) { asignatura ->
                                AsignaturaCard(asignatura) {}
                            }
                        }
                    }
                }
            }
        }

        if (currentView == AdminView.CENTROS) {
            FloatingActionButton(
                onClick = { /* TODO: Add Centro Dialog */ },
                containerColor = primaryColor,
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Centro", tint = textColor)
            }
        }
    }
}

@Composable
fun CentroCard(centro: Centro, onClick: () -> Unit) {
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
                Text(text = centro.direccion, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun TipoCursoCard(tipo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Category, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = tipo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor, modifier = Modifier.weight(1f))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun CursoCard(curso: Curso, isBase: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.School, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                val nombre = if (isBase) curso.nombre.replace(Regex(" \\dº"), "") else curso.nombre
                Text(text = nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = textColor)
                if (!isBase) {
                    Text(text = "${curso.modalidad} - ${curso.ciclo}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray)
        }
    }
}
