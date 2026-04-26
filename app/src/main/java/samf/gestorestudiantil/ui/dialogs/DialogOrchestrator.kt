package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import samf.gestorestudiantil.data.models.AsistenciaEstado

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import samf.gestorestudiantil.ui.viewmodels.AsistenciaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogOrchestrator(
    states: List<DialogState>,
    onShowDialog: (DialogState) -> Unit,
    onDismiss: (DialogState) -> Unit
) {
    fun DialogState.isOverlay(): Boolean = 
        this is DialogState.DatePicker || 
        this is DialogState.TimePicker || 
        this is DialogState.Confirmation ||
        this is DialogState.NotificationPermissionRationale
    
    val visibleStates = remember(states.size, states.lastOrNull()) {
        if (states.isEmpty()) return@remember emptyList()
        
        val result = mutableListOf<DialogState>()
        
        for (i in states.indices.reversed()) {
            val s = states[i]
            result.add(0, s)
 
            if (!s.isOverlay()) break
        }
        result
    }

    visibleStates.forEach { state ->
        val dismissAction = { onDismiss(state) }
        key(state) {
            when (state) {
                is DialogState.None -> Unit

                is DialogState.Confirmation -> {
                    ConfirmDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.Recordatorio -> {
                    RecordatorioBottomSheet(
                        state = state,
                        onShowDialog = onShowDialog,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.Filter -> {
                    FilterByBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.UserProfile -> {
                    UserProfileDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AsignarAsignaturas -> {
                    AsignarAsignaturasBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AsignarProfesor -> {
                    AsignarProfesorBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AsignarTutor -> {
                    AsignarTutorBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AddUnidad -> {
                    AddUnidadBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AddPost -> {
                    AddPostBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.EditHorario -> {
                    EditHorarioBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.EditUser -> {
                    EditUserBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.EditSelfProfile -> {
                    EditSelfProfileDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.EditCentro,
                is DialogState.EditCurso,
                is DialogState.EditAsignatura -> Unit

                is DialogState.DatePicker -> {
                    CustomDatePickerDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.TimePicker -> {
                    CustomTimePickerDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.AddTarea -> {
                    AddTareaBottomSheet(
                        state = state,
                        onShowDialog = onShowDialog,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.TareaDetalleEstudiante -> {
                    TareaDetalleEstudianteBottomSheet(
                        state = state,
                        estudianteId = state.estudianteId,
                        estudianteNombre = state.estudianteNombre,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.VerEntregasProfesor -> {
                    VerEntregasProfesorBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction,
                        onOpenDialog = onShowDialog
                    )
                }

                is DialogState.VerDetalleEvaluacion -> {
                    VerDetalleEvaluacionBottomSheet(
                        state = state,
                        onDismiss = dismissAction
                    )
                }

                is DialogState.AddEditCalificacion -> {
                    AddEditCalificacionBottomSheet(
                        state = state,
                        onDismiss = dismissAction,
                    )
                }

                is DialogState.AttachmentOptions -> {
                    AttachmentOptionsBottomSheet(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.NotificationPermissionRationale -> {
                    NotificationPermissionDialog(
                        state = state,
                        onDismissRequest = dismissAction
                    )
                }

                is DialogState.SelectAsistencia -> {
                    ModalBottomSheet(
                        onDismissRequest = dismissAction,
                        containerColor = samf.gestorestudiantil.ui.theme.backgroundColor,
                        dragHandle = { BottomSheetDefaults.DragHandle(color = samf.gestorestudiantil.ui.theme.surfaceDimColor) }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Marcar Asistencia",
                                fontSize = 14.sp,
                                color = samf.gestorestudiantil.ui.theme.surfaceDimColor
                            )
                            Text(
                                text = state.estudianteNombre,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = samf.gestorestudiantil.ui.theme.textColor,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                AsistenciaSelectionChip(
                                    text = "PRESENTE",
                                    iconText = "P",
                                    color = Color(0xFF4CAF50),
                                    isSelected = state.estadoActual == AsistenciaEstado.PRESENTE,
                                    onClick = {
                                        state.onEstadoSelected(AsistenciaEstado.PRESENTE)
                                        dismissAction()
                                    }
                                )
                                AsistenciaSelectionChip(
                                    text = "AUSENTE",
                                    iconText = "A",
                                    color = Color(0xFFF44336),
                                    isSelected = state.estadoActual == AsistenciaEstado.AUSENTE,
                                    onClick = {
                                        state.onEstadoSelected(AsistenciaEstado.AUSENTE)
                                        dismissAction()
                                    }
                                )
                                AsistenciaSelectionChip(
                                    text = "TARDE",
                                    iconText = "T",
                                    color = Color(0xFFFF9800),
                                    isSelected = state.estadoActual == AsistenciaEstado.TARDE,
                                    onClick = {
                                        state.onEstadoSelected(AsistenciaEstado.TARDE)
                                        dismissAction()
                                    }
                                )
                                AsistenciaSelectionChip(
                                    text = "JUSTIFICADO",
                                    iconText = "J",
                                    color = Color(0xFF2196F3),
                                    isSelected = state.estadoActual == AsistenciaEstado.JUSTIFICADO,
                                    onClick = {
                                        state.onEstadoSelected(AsistenciaEstado.JUSTIFICADO)
                                        dismissAction()
                                    }
                                )
                            }
                            Spacer(modifier = Modifier.height(48.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AsistenciaSelectionChip(
    text: String,
    iconText: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (isSelected) color else color.copy(alpha = 0.1f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconText,
                color = if (isSelected) Color.White else color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) color else samf.gestorestudiantil.ui.theme.surfaceDimColor
        )
    }
}
