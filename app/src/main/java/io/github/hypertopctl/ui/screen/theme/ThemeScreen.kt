package io.github.hypertopctl.ui.screen.theme

import androidx.compose.runtime.Composable
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.animation.predictiveback.PredictiveBackAnimation
import io.github.hypertopctl.ui.animation.predictiveback.PredictiveBackExitDirection
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

/**
 * Theme settings screen. Miuix side is aligned with KernelSU Manager's ColorPalette screen;
 * Material side is aligned with ReSukiSU Manager's ThemeSettings screen.
 *
 * The UI framework switch (Miuix / Material) and predictive back animation configuration live here.
 */
@Composable
fun ThemeScreen(
    state: MainUiState,
    onBack: () -> Unit,
    onUiFrameworkChange: (String) -> Unit,
    onThemeModeChange: (Int) -> Unit,
    onMiuixMonetChange: (Boolean) -> Unit,
    onDynamicColorChange: (Boolean) -> Unit,
    onKeyColorChange: (Int) -> Unit,
    onColorStyleChange: (String) -> Unit,
    onColorSpecChange: (String) -> Unit,
    onMiuixTransitionAnimationChange: (Boolean) -> Unit = {},
    onPredictiveBackAnimationChange: (PredictiveBackAnimation) -> Unit = {},
    onPredictiveBackExitDirectionChange: (PredictiveBackExitDirection) -> Unit = {},
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> ThemeMiuix(
            state = state,
            onBack = onBack,
            onUiFrameworkChange = onUiFrameworkChange,
            onThemeModeChange = onThemeModeChange,
            onMiuixMonetChange = onMiuixMonetChange,
            onKeyColorChange = onKeyColorChange,
            onColorStyleChange = onColorStyleChange,
            onColorSpecChange = onColorSpecChange,
            onMiuixTransitionAnimationChange = onMiuixTransitionAnimationChange,
        )

        UiMode.Material -> ThemeMaterial(
            state = state,
            onBack = onBack,
            onUiFrameworkChange = onUiFrameworkChange,
            onThemeModeChange = onThemeModeChange,
            onDynamicColorChange = onDynamicColorChange,
            onKeyColorChange = onKeyColorChange,
            onColorStyleChange = onColorStyleChange,
            onColorSpecChange = onColorSpecChange,
            onPredictiveBackAnimationChange = onPredictiveBackAnimationChange,
            onPredictiveBackExitDirectionChange = onPredictiveBackExitDirectionChange,
        )
    }
}
