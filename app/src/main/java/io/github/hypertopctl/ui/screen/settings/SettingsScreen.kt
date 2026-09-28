package io.github.hypertopctl.ui.screen.settings

import androidx.compose.runtime.Composable
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

@Composable
fun SettingsScreen(
    state: MainUiState,
    onUiFrameworkChange: (String) -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsMiuix(state, onUiFrameworkChange)
        UiMode.Material -> SettingsMaterial(state, onUiFrameworkChange)
    }
}
