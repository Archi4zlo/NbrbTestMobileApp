package com.archi.tmpnces.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
	primary = Brand,
	onPrimary = Color.White,
	primaryContainer = Brand,
	onPrimaryContainer = Color.White,
	secondary = BrandDark,
	onSecondary = Color.White,
	secondaryContainer = BrandSoft,
	onSecondaryContainer = Brand,
	background = Color.White,
	onBackground = Color(0xFF1C1B1F),
	surface = Color.White,
	onSurface = Color(0xFF1C1B1F),
	surfaceVariant = Color(0xFFF7F2F3),
	onSurfaceVariant = Color(0xFF4A4247),
	surfaceContainer = Color(0xFFF9F4F5),
)

private val DarkColors = darkColorScheme(
	primary = BrandLight,
	onPrimary = Color(0xFF3A0A16),
	primaryContainer = BrandDark,
	onPrimaryContainer = Color.White,
	secondary = BrandLight,
	onSecondary = Color(0xFF3A0A16),
	secondaryContainer = Color(0xFF4A1524),
	onSecondaryContainer = BrandLight,
	surfaceContainer = Color(0xFF241A1D)
)

@Composable
fun TmpNcesTheme(
	darkTheme: Boolean = isSystemInDarkTheme(),
	content: @Composable () -> Unit,
) {
	MaterialTheme(
		colorScheme = if (darkTheme) DarkColors else LightColors,
		typography = Typography,
		content = content,
	)
}