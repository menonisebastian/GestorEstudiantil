package samf.gestorestudiantil.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import samf.gestorestudiantil.R // Asegúrate de importar tu R

// 1. Configuración del Proveedor de Google Fonts
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// 2. Definición de la Fuente (Ejemplo: Montserrat)
val fontName = GoogleFont("Poppins") // Nombre exacto en fonts.google.com

val TuFuenteGoogle = FontFamily(
    Font(googleFont = fontName, fontProvider = provider),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
    // Puedes agregar más pesos (Light, ExtraBold) si los necesitas
)

// 3. Tipografía Base de Material
private val defaultTypography = Typography()

// 4. Tipografía Global Personalizada (Sobrescribe todo con tu fuente)
val AppTypography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = TuFuenteGoogle),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = TuFuenteGoogle),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = TuFuenteGoogle),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = TuFuenteGoogle),
    headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = TuFuenteGoogle),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = TuFuenteGoogle),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = TuFuenteGoogle),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = TuFuenteGoogle),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = TuFuenteGoogle),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = TuFuenteGoogle),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = TuFuenteGoogle),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = TuFuenteGoogle),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = TuFuenteGoogle),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = TuFuenteGoogle),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = TuFuenteGoogle)
)