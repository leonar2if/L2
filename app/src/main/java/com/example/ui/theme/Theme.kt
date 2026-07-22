package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PosDarkColorScheme = darkColorScheme(
    primary = NeonGreen,
    onPrimary = PureBlack,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = PureWhite,
    secondary = TransferBlue,
    onSecondary = PureBlack,
    error = CrimsonRed,
    onError = PureWhite,
    background = DarkBackground,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = PureWhite
)

@Composable
fun PosTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PosDarkColorScheme,
        typography = Typography,
        content = content
    )
}
