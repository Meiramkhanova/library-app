package com.adina.spendlog.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Nimbus-like finance palette
private val Purple = Color(0xFF635BFF)
private val PurpleDark = Color(0xFF3D36C7)
private val PurpleSoft = Color(0xFFEDEBFF)
private val PurpleContainer = Color(0xFFD9D6FF)
private val Mint = Color(0xFF2EC4B6)
private val MintContainer = Color(0xFFD4F5F1)
private val Coral = Color(0xFFFF6B6B)
private val CoralContainer = Color(0xFFFFE3E3)
private val Ink = Color(0xFF1C1B3A)
private val InkMuted = Color(0xFF6B6A8A)
private val Canvas = Color(0xFFF4F3FB)
private val CardSurface = Color(0xFFFFFFFF)
private val OutlineSoft = Color(0xFFD8D6EA)

private val SpendLogColorScheme = lightColorScheme(
    primary = Purple,
    onPrimary = Color.White,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = PurpleDark,
    secondary = Mint,
    onSecondary = Color.White,
    secondaryContainer = MintContainer,
    onSecondaryContainer = Color(0xFF0A5C55),
    tertiary = PurpleDark,
    onTertiary = Color.White,
    tertiaryContainer = PurpleSoft,
    onTertiaryContainer = PurpleDark,
    background = Canvas,
    onBackground = Ink,
    surface = CardSurface,
    onSurface = Ink,
    surfaceVariant = PurpleSoft,
    onSurfaceVariant = InkMuted,
    outline = OutlineSoft,
    outlineVariant = Color(0xFFE8E6F5),
    error = Coral,
    onError = Color.White,
    errorContainer = CoralContainer,
    onErrorContainer = Color(0xFF8B1E1E)
)

private val SpendLogShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

private val SpendLogTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        color = Ink
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    )
)

@Composable
fun SpendLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpendLogColorScheme,
        shapes = SpendLogShapes,
        typography = SpendLogTypography,
        content = content
    )
}
