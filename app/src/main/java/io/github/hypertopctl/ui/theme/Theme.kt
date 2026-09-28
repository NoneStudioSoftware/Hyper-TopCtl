package io.github.hypertopctl.ui.theme

import androidx.compose.runtime.Composable

/**
 * Unified theme entry point. Dispatches to Miuix (aligned with KernelSU Manager) or
 * Material 3 (aligned with ReSukiSU Manager) based on [uiMode].
 */
@Composable
fun HyperTopCtlTheme(
    uiMode: UiMode,
    settings: AppThemeSettings,
    content: @Composable () -> Unit,
) {
    when (uiMode) {
        UiMode.Miuix -> MiuixAppTheme(settings, content)
        UiMode.Material -> MaterialAppTheme(settings, content)
    }
}
