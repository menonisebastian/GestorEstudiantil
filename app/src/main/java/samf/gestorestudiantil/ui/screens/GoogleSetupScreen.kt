package samf.gestorestudiantil.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.data.models.User
import samf.gestorestudiantil.ui.components.CustomOptionsTextField
import samf.gestorestudiantil.ui.components.CustomPasswordTextField
import samf.gestorestudiantil.ui.theme.backgroundColor
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.surfaceDimColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GooglePasswordSetupScreen(
    onBack: () -> Unit,
    onNext: (String) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val passwordState = rememberTextFieldState()
    val confirmPasswordState = rememberTextFieldState()

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Seguridad", fontWeight = FontWeight.ExtraBold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Atrás",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = primaryColor
                )
                Text(
                    text = "Crea una contraseña",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Para poder iniciar sesión con tu email en el futuro.",
                    color = surfaceDimColor,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                CustomPasswordTextField(state = passwordState)
                Spacer(Modifier.height(16.dp))
                CustomPasswordTextField(state = confirmPasswordState)

                Button(
                    onClick = {
                        val password = passwordState.text.toString()
                        val confirm = confirmPasswordState.text.toString()
                        if (password.length < 6) {
                            Toast.makeText(context, "Mínimo 6 caracteres", Toast.LENGTH_SHORT).show()
                        } else if (password != confirm) {
                            Toast.makeText(context, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                        } else {
                            onNext(password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Siguiente")
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAcademicSetupScreen(
    passwordValue: String,
    onBack: () -> Unit,
    onSetupComplete: (User) -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    var rolSeleccionado by remember { mutableStateOf("Seleccionar Rol...") }
    val roles = listOf("ESTUDIANTE", "PROFESOR")

    var centroNombre by remember { mutableStateOf("Seleccionar Instituto...") }
    var centroId by remember { mutableStateOf("") }

    var cursoNombre by remember { mutableStateOf("Seleccionar Curso...") }
    var cursoId by remember { mutableStateOf("") }
    var cursoAcronimo by remember { mutableStateOf("") }

    var turno by remember { mutableStateOf("Seleccionar Turno...") }
    var departamento by remember { mutableStateOf("") }
    var cicloSeleccionado by remember { mutableStateOf("Primer Año") }
    val ciclos = listOf("Primer Año", "Segundo Año")

    val centrosList by authViewModel.centros.collectAsState()
    val cursosList by authViewModel.cursos.collectAsState()
    
    val turnosDisponibles = remember(cursoId, cursosList) {
        cursosList.find { it.id == cursoId }?.turnosDisponibles ?: emptyList()
    }

    LaunchedEffect(authState) {
        if (authState.isSuccess && authState.user != null) {
            onSetupComplete(authState.user!!)
        }
        if (authState.errorMessage != null) {
            Toast.makeText(context, authState.errorMessage, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Datos Académicos", fontWeight = FontWeight.ExtraBold, color = textColor) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Atrás",
                            tint = textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = primaryColor
                    )
                    Text(
                        text = "Casi terminamos",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
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
                                cursoAcronimo = ""
                                turno = "Seleccionar Turno..."
                            }
                        },
                        opciones = centrosList.map { it.nombre },
                        icon = Icons.Default.Business,
                        label = "Instituto"
                    )

                    if (centroId.isNotEmpty()) {
                        if (rolSeleccionado == "ESTUDIANTE") {
                            CustomOptionsTextField(
                                texto = cursoNombre,
                                onValueChange = { nombreSeleccionado ->
                                    cursoNombre = nombreSeleccionado
                                    val cursoSel = cursosList.find { it.nombre == nombreSeleccionado }
                                    if (cursoSel != null) {
                                        cursoId = cursoSel.id
                                        cursoAcronimo = cursoSel.acronimo
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

                                CustomOptionsTextField(
                                    texto = cicloSeleccionado,
                                    onValueChange = { cicloSeleccionado = it },
                                    opciones = ciclos,
                                    label = "Año / Ciclo",
                                    icon = Icons.Default.Class
                                )
                            }
                        } else if (rolSeleccionado == "PROFESOR") {
                            CustomOptionsTextField(
                                texto = departamento,
                                onValueChange = { departamento = it },
                                opciones = listOf("Informática", "Administración", "Comercio", "Sanidad", "Hostelería"),
                                icon = Icons.Default.Business,
                                label = "Departamento"
                            )

                            CustomOptionsTextField(
                                texto = turno,
                                onValueChange = { turno = it },
                                opciones = listOf("matutino", "vespertino"),
                                icon = Icons.Default.Schedule,
                                label = "Turno de trabajo"
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .padding(top = 32.dp, bottom = 16.dp)
                        .fillMaxWidth()
                ) {
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
                                    val cicloNum = if (cicloSeleccionado == "Primer Año") 1 else 2
                                    authViewModel.completeGoogleSetup(
                                        password = passwordValue,
                                        rolSeleccionado = rolSeleccionado,
                                        centroId = centroId,
                                        cursoId = cursoId,
                                        cursoNombre = cursoAcronimo,
                                        turno = turno,
                                        ciclo = cicloNum,
                                        name = authState.user?.nombre ?: "",
                                        email = authState.user?.email ?: "",
                                        imgUrl = authState.user?.imgUrl ?: "",
                                        departamento = ""
                                    )
                                }
                            } else if (rolSeleccionado == "PROFESOR") {
                                if (turno == "Seleccionar Turno...") {
                                    Toast.makeText(context, "Debes seleccionar un turno de trabajo", Toast.LENGTH_SHORT).show()
                                } else if (departamento.isEmpty()) {
                                    Toast.makeText(context, "Debes seleccionar un departamento", Toast.LENGTH_SHORT).show()
                                } else {
                                    authViewModel.completeGoogleSetup(
                                        password = passwordValue,
                                        rolSeleccionado = rolSeleccionado,
                                        centroId = centroId,
                                        cursoId = "",
                                        cursoNombre = "Docente",
                                        turno = turno,
                                        ciclo = 1,
                                        name = authState.user?.nombre ?: "",
                                        email = authState.user?.email ?: "",
                                        imgUrl = authState.user?.imgUrl ?: "",
                                        departamento = departamento
                                    )
                                }
                            }
                        },
                        enabled = !authState.isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (authState.isLoading) {
                            CircularProgressIndicator(color = textColor, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Finalizar Registro")
                        }
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}