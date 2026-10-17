package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = UsnaCyan,
  onPrimary = UsnaDeepNavy,
  secondary = UsnaOrange,
  onSecondary = Color.White,
  tertiary = UsnaGreen,
  onTertiary = UsnaDeepNavy,
  background = UsnaDarkBlue,
  onBackground = UsnaTextWhite,
  surface = UsnaPlatform,
  onSurface = UsnaTextWhite
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
