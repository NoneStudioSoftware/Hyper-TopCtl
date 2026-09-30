package io.github.hypertopctl.ui.screen.settings

import androidx.compose.runtime.Composable
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

@Composable
fun SettingsScreen(
    state: MainUiState,
    onOpenTheme: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLog: () -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsMiuix(state, onOpenTheme, onOpenAbout, onOpenLog)
        UiMode.Material -> SettingsMaterial(state, onOpenTheme, onOpenAbout, onOpenLog)
    }
}
