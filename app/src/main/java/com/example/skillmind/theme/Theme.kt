package com.example.skillmind.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkOnPrimary,
    onSecondary = DarkOnSecondary,
    onTertiary = DarkOnTertiary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightOnPrimary,
    onSecondary = LightOnSecondary,
    onTertiary = LightOnTertiary,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface
)

@Composable
fun SkillMindTextFieldColors(isDarkTheme: Boolean = isSystemInDarkTheme()): TextFieldColors {
    return if (isDarkTheme) {
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
            cursorColor = MaterialTheme.colorScheme.tertiary,
            focusedTextColor = MaterialTheme.colorScheme.onBackground,
            unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
        )
    } else {
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            cursorColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun SkillMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
