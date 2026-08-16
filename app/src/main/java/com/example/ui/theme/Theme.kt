package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProCameraColorScheme = darkColorScheme(
    primary = CameraAmberPrimary,
    onPrimary = Color.Black,
    primaryContainer = CameraAmberAccent,
    onPrimaryContainer = Color.Black,
    secondary = CameraCyanAccent,
    onSecondary = Color.Black,
    tertiary = CameraNeonGreen,
    onTertiary = Color.Black,
    background = CameraDarkBackground,
    onBackground = CameraTextPrimary,
    surface = CameraDarkSurface,
    onSurface = CameraTextPrimary,
    surfaceVariant = CameraDarkSurfaceVariant,
    onSurfaceVariant = CameraTextSecondary,
    outline = CameraBorder,
    error = CameraShutterRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProCameraColorScheme,
        typography = Typography,
        content = content
    )
}
