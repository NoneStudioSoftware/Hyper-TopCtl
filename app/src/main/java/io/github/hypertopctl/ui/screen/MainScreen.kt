package io.github.hypertopctl.ui.screen

import androidx.compose.runtime.Composable
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.theme.UiMode

/**
 * Dispatches to the Miuix or Material implementation of the main screen based on [uiMode].
 * Keeping the state + callbacks identical means the two implementations stay behaviorally in sync.
 */
@Composable
fun MainScreen(
    uiMode: UiMode,
    state: MainUiState,
    onGlobalEnabledChange: (Boolean) -> Unit,
    onListModeChange: (ListMode) -> Unit,
    onUiFrameworkChange: (String) -> Unit,
) {
    when (uiMode) {
        UiMode.Miuix -> MiuixMainScreen(
            state = state,
            onGlobalEnabledChange = onGlobalEnabledChange,
            onListModeChange = onListModeChange,
            onUiFrameworkChange = onUiFrameworkChange,
        )

        UiMode.Material -> MaterialMainScreen(
            state = state,
            onGlobalEnabledChange = onGlobalEnabledChange,
            onListModeChange = onListModeChange,
            onUiFrameworkChange = onUiFrameworkChange,
        )
    }
}
