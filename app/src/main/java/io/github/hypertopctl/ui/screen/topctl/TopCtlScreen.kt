package io.github.hypertopctl.ui.screen.topctl

import androidx.compose.runtime.Composable
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

@Composable
fun TopCtlScreen(
    state: MainUiState,
    onGlobalEnabledChange: (Boolean) -> Unit,
    onListModeChange: (ListMode) -> Unit,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> TopCtlMiuix(state, onGlobalEnabledChange, onListModeChange)
        UiMode.Material -> TopCtlMaterial(state, onGlobalEnabledChange, onListModeChange)
    }
}
