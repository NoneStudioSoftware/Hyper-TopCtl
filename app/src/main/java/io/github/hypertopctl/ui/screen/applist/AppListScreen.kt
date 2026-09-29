package io.github.hypertopctl.ui.screen.applist

import androidx.compose.runtime.Composable
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

@Composable
fun AppListScreen(
    state: MainUiState,
    onTogglePackage: (String) -> Unit,
) {
    when (LocalUiMode.current) {
        // Miuix implementation lands after the Material one is verified.
        UiMode.Miuix -> AppListMiuix(state)
        UiMode.Material -> AppListMaterial(state, onTogglePackage)
    }
}
