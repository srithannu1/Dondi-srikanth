package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RoseDarkColorScheme = darkColorScheme(
  primary = RosePrimary,
  onPrimary = Color(0xFF690005),
  primaryContainer = RoseContainer,
  onPrimaryContainer = OnRoseContainer,
  secondary = RoseSecondary,
  onSecondary = Color(0xFF441917),
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = TextPrimary,
  tertiary = RoseTertiary,
  onTertiary = Color(0xFF382E2D),
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  surfaceContainer = DarkSurfaceContainer,
  surfaceContainerHigh = DarkSurfaceElevated,
  outline = BorderSubtle,
  outlineVariant = DarkSurfaceVariant
)

@Composable
fun RoseGalleryTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = RoseDarkColorScheme,
    typography = Typography,
    content = content
  )
}

// Alias for backwards compatibility if referenced
@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  RoseGalleryTheme(content = content)
}
