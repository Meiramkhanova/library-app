package com.adina.libraryapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Ink = Color(0xFF1B2A4A)
private val Brass = Color(0xFFB7791F)
private val Paper = Color(0xFFFFF8EC)
private val PaperSurface = Color(0xFFFFFDF7)
private val InkText = Color(0xFF22252B)

private val LibraryColorScheme = lightColorScheme(
    primary = Ink,
    onPrimary = Color.White,
    secondary = Brass,
    onSecondary = Color.White,
    background = Paper,
    onBackground = InkText,
    surface = PaperSurface,
    onSurface = InkText
)

@Composable
fun LibraryAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LibraryColorScheme,
        content = content
    )
}
