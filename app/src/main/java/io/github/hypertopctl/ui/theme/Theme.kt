package io.github.hypertopctl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

/**
 * Unified theme entry point. Dispatches to Miuix or Material 3 based on [uiMode].
 * Mirrors KernelSU Manager's `KernelSUTheme` dispatch.
 */
@Composable
fun HyperTopCtlTheme(
    uiMode: UiMode,
    content: @Composable () -> Unit,
) {
    when (uiMode) {
        UiMode.Miuix -> MiuixAppTheme(content)
        UiMode.Material -> MaterialAppTheme(content)
    }
}

@Composable
private fun MiuixAppTheme(content: @Composable () -> Unit) {
    val controller = ThemeController(ColorSchemeMode.System)
    MiuixTheme(controller = controller, content = content)
}

@Composable
private fun MaterialAppTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        dark -> darkColorScheme()
        else -> lightColorScheme()
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
