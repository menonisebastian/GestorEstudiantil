package samf.gestorestudiantil.ui.theme

import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val textColor: Color
    @Composable
    get() = colorScheme.onSurface

val backgroundColor: Color
    @Composable
    get() = colorScheme.background

val surfaceColor: Color
    @Composable
    get() = colorScheme.surface

val surfaceDimColor: Color
    @Composable
    get() = colorScheme.surfaceDim