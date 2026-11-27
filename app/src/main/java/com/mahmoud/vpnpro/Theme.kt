package com.mahmoud.vpnpro

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Cyan = Color(0xFF00E5FF)
private val Dark = darkColorScheme(primary = Cyan, onPrimary = Color.Black, background = Color(0xFF05080D),
    surface = Color(0xFF0D141C), onSurface = Color.White, surfaceVariant = Color(0xFF16212B))
private val Light = lightColorScheme(primary = Color(0xFF00839A))

@Composable fun AppTheme(dark: Boolean, content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
