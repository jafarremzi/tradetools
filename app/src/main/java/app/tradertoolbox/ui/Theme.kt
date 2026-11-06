package app.tradertoolbox.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** رنگ‌های طراحی مدرن (تم تیره با یک رنگ تأکید نعنایی). */
object Palette {
    val Bg = Color(0xFF0B1118)
    val Surface = Color(0xFF121B25)
    val Raised = Color(0xFF1A2633)
    val Border = Color(0xFF243342)
    val BorderStrong = Color(0xFF2A3A4A)
    val Text = Color(0xFFE8EEF3)
    val TextSoft = Color(0xFFB7C5D0)
    val Muted = Color(0xFF8FA1B0)
    val Accent = Color(0xFF3DDBC0)
    val OnAccent = Color(0xFF04211D)
    val HeroBg = Color(0xFF0F2A2A)
    val HeroBorder = Color(0xFF1E4A45)
    val Track = Color(0xFF1F2F3E)
    val Danger = Color(0xFFF87171)
    val Warn = Color(0xFFFBBF24)
    val Good = Color(0xFF34D399)
}

private val AppColors = darkColorScheme(
    primary = Palette.Accent,
    onPrimary = Palette.OnAccent,
    primaryContainer = Palette.HeroBg,
    onPrimaryContainer = Palette.Accent,
    secondary = Palette.Accent,
    onSecondary = Palette.OnAccent,
    secondaryContainer = Palette.Raised,
    onSecondaryContainer = Palette.Text,
    background = Palette.Bg,
    onBackground = Palette.Text,
    surface = Palette.Surface,
    onSurface = Palette.Text,
    surfaceVariant = Palette.Raised,
    onSurfaceVariant = Palette.Muted,
    surfaceContainer = Palette.Surface,
    surfaceContainerHigh = Palette.Raised,
    surfaceContainerHighest = Palette.Raised,
    outline = Palette.BorderStrong,
    outlineVariant = Palette.Border,
    error = Palette.Danger,
    onError = Palette.OnAccent,
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun TraderToolboxTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, shapes = AppShapes, content = content)
}
