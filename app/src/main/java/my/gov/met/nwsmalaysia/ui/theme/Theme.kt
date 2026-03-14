package my.gov.met.nwsmalaysia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Dark palette — ink/slate tones, not cold navy ──────────────────────────
val Ink950  = Color(0xFF0C1520)   // deepest background
val Ink900  = Color(0xFF111D2B)   // main background
val Ink800  = Color(0xFF192736)   // card surface (elevated above bg)
val Ink700  = Color(0xFF213348)   // surface variant (search bar, chips)
val Ink600  = Color(0xFF2E4560)   // outline / dividers

val Sky400  = Color(0xFF52B8E8)   // primary actions & data highlights (dark)
val Sky200  = Color(0xFFABDAF5)   // primary container text (dark)
val Teal400 = Color(0xFF43A898)   // secondary — teal, visually distinct from blue

val TextPrimary   = Color(0xFFDDE6EF)   // main text on dark (warm off-white, not harsh)
val TextSecondary = Color(0xFF8FA4B8)   // muted labels on dark

// ── Light palette — warm parchment, no pure white ──────────────────────────
val Paper100 = Color(0xFFF5F3EE)   // main background (warm off-white)
val Paper200 = Color(0xFFECE9E2)   // surface variant (search bar, chips)
val Card     = Color(0xFFFCFBF8)   // card surface (barely off-white, warm)

val Sky700   = Color(0xFF1763A0)   // primary actions (light)
val Sky100   = Color(0xFFCDE6F9)   // primary container
val Teal700  = Color(0xFF1E7264)   // secondary (light)

val Slate900 = Color(0xFF192130)   // main text on light (not pure black)

private val DarkColorScheme = darkColorScheme(
    primary              = Sky400,
    onPrimary            = Ink900,
    primaryContainer     = Ink700,
    onPrimaryContainer   = Sky200,
    secondary            = Teal400,
    onSecondary          = Ink900,
    secondaryContainer   = Ink700,
    onSecondaryContainer = Color(0xFFA8DDD7),
    background           = Ink900,
    onBackground         = TextPrimary,
    surface              = Ink800,
    onSurface            = TextPrimary,
    surfaceVariant       = Ink700,
    onSurfaceVariant     = TextSecondary,
    outline              = Ink600,
    outlineVariant       = Color(0xFF243447)
)

private val LightColorScheme = lightColorScheme(
    primary              = Sky700,
    onPrimary            = Color.White,
    primaryContainer     = Sky100,
    onPrimaryContainer   = Color(0xFF003F6E),
    secondary            = Teal700,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFB8EDE7),
    onSecondaryContainer = Color(0xFF00352D),
    background           = Paper100,
    onBackground         = Slate900,
    surface              = Card,
    onSurface            = Slate900,
    surfaceVariant       = Paper200,
    onSurfaceVariant     = Color(0xFF4A5C6E),
    outline              = Color(0xFFB0BECA),
    outlineVariant       = Color(0xFFDDD9D1)
)

@Composable
fun NwsMalaysiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
