package io.github.hypertopctl.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Material 3 theme, aligned with ReSukiSU Manager: MaterialExpressiveTheme + materialkolor seed
 * color scheme + animated cross-fade on change.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MaterialAppTheme(
    settings: AppThemeSettings,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val systemDark = isSystemInDarkTheme()
    val darkTheme = settings.colorMode.isDark || (settings.colorMode.isSystem && systemDark)
    val amoled = settings.colorMode.isAmoled

    // dynamicColor ON  -> seed from system primary (Material You).
    // dynamicColor OFF -> use the custom key color (or default sentinel).
    val useSystemSeed = settings.dynamicColor || settings.keyColor == 0
    val seedColor = if (useSystemSeed && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Color.Unspecified
    } else if (settings.keyColor != 0) {
        Color(settings.keyColor)
    } else {
        Color.Unspecified
    }

    val colorScheme = rememberAppColorScheme(
        seedColor = seedColor,
        isDark = darkTheme,
        isAmoled = amoled,
        paletteStyle = settings.paletteStyle,
        colorSpec = settings.colorSpec,
    ).animateAsState()

    LaunchedEffect(darkTheme) {
        val window = (context as? Activity)?.window ?: return@LaunchedEffect
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}
