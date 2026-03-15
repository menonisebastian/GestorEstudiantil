package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.navigation.Routes
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStep2Screen(
    route: Routes.RegisterStep2,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()
    val isLoading = authState.isLoading
    val context = LocalContext.current

    var rolSeleccionado by remember { mutableStateOf("Seleccionar Rol...") }
    val roles = listOf("ESTUDIANTE", "PROFESOR")

    var centroNombre by remember { mutableStateOf("Seleccionar Instituto...") }
    var centroId by remember { mutableStateOf("") }

    var cursoNombre by remember { mutableStateOf("Seleccionar Curso...") }
    var cursoId by remember { mutableStateOf("") }

    var turno by remember { mutableStateOf("Seleccionar Turno...") }

    val centrosList by authViewModel.centros.collectAsState()
    val cursosList by authViewModel.cursos.collectAsState()

    val turnosDisponibles = remember(cursoId, cursosList) {
        cursosList.find { it.id == cursoId }?.turnos ?: emptyList()
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Paso 2", fontWeight = FontWeight.ExtraBold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                val (headerRef, inputsRef, footerRef) = createRefs()

                Column(
                    modifier = Modifier
                        .constrainAs(inputsRef) {
                            centerTo(parent)
                        }
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CustomOptionsTextField(
                        texto = rolSeleccionado,
                        onValueChange = { rolSeleccionado = it },
                        opciones = roles,
                        icon = Icons.Outlined.Person,
                        label = "¿Eres Estudiante o Profesor?"
                    )

                    CustomOptionsTextField(
                        texto = centroNombre,
                        onValueChange = { nombreSeleccionado ->
                            centroNombre = nombreSeleccionado
                            val centroSel = centrosList.find { it.nombre == nombreSeleccionado }
                            if (centroSel != null) {
                                centroId = centroSel.id
                                authViewModel.loadCursosPorCentro(centroSel.id)
                                cursoNombre = "Seleccionar Curso..."
                                cursoId = ""
                                turno = "Seleccionar Turno..."
                            }
                        },
                        opciones = centrosList.map { it.nombre },
                        icon = Icons.Default.Business,
                        label = "Instituto"
                    )

                    if (rolSeleccionado == "ESTUDIANTE" && centroId.isNotEmpty()) {
                        CustomOptionsTextField(
                            texto = cursoNombre,
                            onValueChange = { nombreSeleccionado ->
                                cursoNombre = nombreSeleccionado
                                val cursoSel = cursosList.find { it.nombre == nombreSeleccionado }
                                if (cursoSel != null) {
                                    cursoId = cursoSel.id
                                    turno = "Seleccionar Turno..."
                                }
                            },
                            opciones = cursosList.map { it.nombre },
                            icon = Icons.Default.Class,
                            label = "Curso a matricular"
                        )

                        if (cursoId.isNotEmpty() && turnosDisponibles.isNotEmpty()) {
                            CustomOptionsTextField(
                                texto = turno,
                                onValueChange = { turno = it },
                                opciones = turnosDisponibles,
                                icon = Icons.Default.Schedule,
                                label = "Turno"
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .constrainAs(headerRef) {
                            bottom.linkTo(inputsRef.top, margin = 32.dp)
                            centerHorizontallyTo(parent)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Crea tu cuenta", color = textColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Paso 2 de 2: Datos académicos", color = surfaceDimColor, fontSize = 14.sp)
                }

                Row(
                    modifier = Modifier
                        .constrainAs(footerRef) {
                            top.linkTo(inputsRef.bottom, margin = 32.dp)
                            centerHorizontallyTo(parent)
                        }
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading
                    ) {
                        Text("Atrás", color = textColor)
                    }

                    Button(
                        onClick = {
                            if (rolSeleccionado == "Seleccionar Rol..." || centroId.isEmpty()) {
                                Toast.makeText(context, "Por favor selecciona rol e instituto", Toast.LENGTH_SHORT).show()
                            } else if (rolSeleccionado == "ESTUDIANTE") {
                                if (cursoId.isEmpty()) {
                                    Toast.makeText(context, "Debes seleccionar un curso", Toast.LENGTH_SHORT).show()
                                } else if (turnosDisponibles.isNotEmpty() && turno == "Seleccionar Turno...") {
                                    Toast.makeText(context, "Debes seleccionar un turno", Toast.LENGTH_SHORT).show()
                                } else {
                                    authViewModel.registerWithEmail(
                                        email = route.email,
                                        pass = route.pass,
                                        name = route.name,
                                        rolSeleccionado = rolSeleccionado,
                                        centroId = centroId,
                                        cursoId = cursoId,
                                        cursoNombre = cursoNombre,
                                        turno = turno,
                                        imgUrl = route.fotoUrl
                                    )
                                }
                            } else {
                                authViewModel.registerWithEmail(
                                    email = route.email,
                                    pass = route.pass,
                                    name = route.name,
                                    rolSeleccionado = rolSeleccionado,
                                    centroId = centroId,
                                    cursoId = cursoId,
                                    cursoNombre = cursoNombre,
                                    turno = turno,
                                    imgUrl = route.fotoUrl
                                )
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(if (rolSeleccionado == "ESTUDIANTE") "Matricularme" else "Finalizar")
                    }
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
        }
    }
}