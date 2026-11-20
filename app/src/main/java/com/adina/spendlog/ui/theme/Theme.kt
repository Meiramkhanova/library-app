package com.adina.spendlog.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DeepTeal = Color(0xFF0F4C5C)
private val Leaf = Color(0xFF2A9D8F)
private val Mist = Color(0xFFF2F7F6)
private val Surface = Color(0xFFFBFDFC)
private val Ink = Color(0xFF1A2B2E)

private val SpendLogColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = Color.White,
    secondary = Leaf,
    onSecondary = Color.White,
    background = Mist,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    error = Color(0xFFB3261E)
)

@Composable
fun SpendLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpendLogColorScheme,
        content = content
    )
}
