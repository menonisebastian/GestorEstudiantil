package samf.gestorestudiantil.ui.dialogs

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import samf.gestorestudiantil.ui.theme.primaryColor
import samf.gestorestudiantil.ui.theme.secondaryColor
import samf.gestorestudiantil.ui.theme.surfaceColor
import samf.gestorestudiantil.ui.theme.tertiaryColor
import samf.gestorestudiantil.ui.theme.textColor
import samf.gestorestudiantil.ui.theme.whiteColor
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePickerDialog(
    state: DialogState.DatePicker,
    onDismissRequest: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (state.initialDate.isNotBlank()) {
            try {
                // Soporta tanto yyyy/MM/dd como yyyy-MM-dd
                val normalizedDate = state.initialDate.replace("-", "/")
                val millis = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).parse(normalizedDate)?.time
                    ?: System.currentTimeMillis()

                if (!state.allowPastDates && millis < System.currentTimeMillis() - 86400000) {
                    System.currentTimeMillis()
                } else {
                    millis
                }
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } else {
            System.currentTimeMillis()
        },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return if (state.allowPastDates) true else utcTimeMillis >= System.currentTimeMillis() - 86400000
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            IconButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                        state.onDateSelected(sdf.format(millis))
                    }
                    onDismissRequest()
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = primaryColor)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Seleccionar", tint = whiteColor)
            }
        },
        dismissButton = {
            IconButton(
                onClick = onDismissRequest,
                colors = IconButtonDefaults.iconButtonColors(containerColor = secondaryColor.copy(alpha = 0.2f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancelar", tint = secondaryColor)
            }
        },
        colors = DatePickerDefaults.colors(containerColor = surfaceColor)
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    "Seleccionar fecha",
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 12.dp,
                        top = 16.dp,
                        bottom = 12.dp
                    )
                )
            },
            colors = DatePickerDefaults.colors(
                containerColor = surfaceColor,
                titleContentColor = textColor,
                headlineContentColor = textColor,
                subheadContentColor = whiteColor,
                selectedDayContainerColor = primaryColor,
                todayContentColor = tertiaryColor,
                todayDateBorderColor = tertiaryColor,
                navigationContentColor = textColor,
                weekdayContentColor = secondaryColor,
            )
        )
    }
}
