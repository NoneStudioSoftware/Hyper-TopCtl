package io.github.hypertopctl.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import com.materialkolor.dynamiccolor.ColorSpec
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

/**
 * Miuix theme, aligned with KernelSU Manager: builds a Miuix [ThemeController] from the app's
 * ColorMode + optional Monet seed color, palette style and color spec.
 */
@Composable
fun MiuixAppTheme(
    settings: AppThemeSettings,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val darkTheme = settings.colorMode.isDark || (settings.colorMode.isSystem && systemDark)

    val miuixPaletteStyle = runCatching {
        ThemePaletteStyle.valueOf(settings.paletteStyle.name)
    }.getOrDefault(ThemePaletteStyle.TonalSpot)

    val miuixColorSpec =
        if (settings.colorSpec.effectiveFor(settings.paletteStyle) == ColorSpec.SpecVersion.SPEC_2025) {
            ThemeColorSpec.Spec2025
        } else {
            ThemeColorSpec.Spec2021
        }

    // Seed: explicit key color wins; otherwise fall back to system dynamic primary when in Monet.
    val resolvedKeyColor: Color? = when {
        settings.keyColor != 0 -> Color(settings.keyColor)
        settings.colorMode.isMonet ->
            if (darkTheme) dynamicDarkColorScheme(context).primary
            else dynamicLightColorScheme(context).primary
        else -> null
    }

    val controller = ThemeController(
        colorSchemeMode = when (settings.colorMode) {
            ColorMode.SYSTEM -> ColorSchemeMode.System
            ColorMode.LIGHT -> ColorSchemeMode.Light
            ColorMode.DARK, ColorMode.DARK_AMOLED -> ColorSchemeMode.Dark
            ColorMode.MONET_SYSTEM -> ColorSchemeMode.MonetSystem
            ColorMode.MONET_LIGHT -> ColorSchemeMode.MonetLight
            ColorMode.MONET_DARK -> ColorSchemeMode.MonetDark
        },
        keyColor = resolvedKeyColor,
        colorSpec = miuixColorSpec,
        paletteStyle = miuixPaletteStyle,
        isDark = darkTheme,
    )

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MiuixTheme(controller = controller, content = content)
}
