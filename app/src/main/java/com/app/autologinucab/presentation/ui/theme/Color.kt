package com.app.autologinucab.presentation.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Paleta principal requerida
val BrandPurple = Color(0xFF7400B8)       // #7400B8 - Violeta profundo
val BrandIndigo = Color(0xFF5E60CE)       // #5E60CE - Índigo / Púrpura eléctrico
val BrandSkyBlue = Color(0xFF4EA8DE)      // #4EA8DE - Azul cielo brillante
val PureBlack = Color(0xFF000000)         // #000000 - Negro absoluto
val PureWhite = Color(0xFFFFFFFF)         // #FFFFFF - Blanco puro

// Superficies y elevaciones para modo oscuro (basadas en PureBlack con acentos de la paleta)
val DarkBackground = PureBlack
val DarkSurface = Color(0xFF090A10)
val DarkSurfaceElevated = Color(0xFF131522)
val DarkSurfaceCard = Color(0xFF1A1C2E)
val DarkSurfaceBorder = Color(0x334EA8DE)

// Superficies para modo claro (basadas en PureWhite con acentos suaves)
val LightBackground = PureWhite
val LightSurface = Color(0xFFF9FAFF)
val LightSurfaceElevated = Color(0xFFF0F3FC)
val LightSurfaceBorder = Color(0x245E60CE)

// Gradientes usando los 3 colores principales
val HeroGradient = Brush.horizontalGradient(
    colors = listOf(BrandPurple, BrandIndigo)
)

val HeroDiagonalGradient = Brush.linearGradient(
    colors = listOf(BrandPurple, BrandIndigo, BrandSkyBlue)
)

val AccentGradient = Brush.horizontalGradient(
    colors = listOf(BrandIndigo, BrandSkyBlue)
)

// Tokens de compatibilidad con Theme
val md_theme_dark_primary = BrandSkyBlue
val md_theme_dark_onPrimary = PureBlack
val md_theme_dark_primaryContainer = BrandPurple
val md_theme_dark_onPrimaryContainer = PureWhite

val md_theme_dark_secondary = BrandIndigo
val md_theme_dark_onSecondary = PureWhite
val md_theme_dark_secondaryContainer = Color(0xFF26284D)
val md_theme_dark_onSecondaryContainer = PureWhite

val md_theme_dark_tertiary = BrandSkyBlue
val md_theme_dark_onTertiary = PureBlack

val md_theme_dark_background = DarkBackground
val md_theme_dark_onBackground = PureWhite

val md_theme_dark_surface = DarkSurface
val md_theme_dark_onSurface = PureWhite
val md_theme_dark_surfaceVariant = DarkSurfaceElevated
val md_theme_dark_onSurfaceVariant = Color(0xFFC7CCE0)

val md_theme_light_primary = BrandPurple
val md_theme_light_onPrimary = PureWhite
val md_theme_light_primaryContainer = Color(0xFFEFE5FD)
val md_theme_light_onPrimaryContainer = Color(0xFF2B0047)

val md_theme_light_secondary = BrandIndigo
val md_theme_light_onSecondary = PureWhite
val md_theme_light_secondaryContainer = Color(0xFFE5E7FD)
val md_theme_light_onSecondaryContainer = Color(0xFF14174D)

val md_theme_light_tertiary = BrandSkyBlue
val md_theme_light_onTertiary = PureBlack

val md_theme_light_background = LightBackground
val md_theme_light_onBackground = PureBlack

val md_theme_light_surface = LightSurface
val md_theme_light_onSurface = PureBlack
val md_theme_light_surfaceVariant = LightSurfaceElevated
val md_theme_light_onSurfaceVariant = Color(0xFF434757)
