package io.github.hypertopctl.ui.screen.home

import androidx.compose.runtime.Composable
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

@Composable
fun HomeScreen(state: MainUiState) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> HomeMiuix(state)
        UiMode.Material -> HomeMaterial(state)
    }
}
