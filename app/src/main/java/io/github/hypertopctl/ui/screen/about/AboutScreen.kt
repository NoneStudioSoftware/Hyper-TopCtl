package io.github.hypertopctl.ui.screen.about

import androidx.compose.runtime.Composable
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

@Composable
fun AboutScreen(onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> AboutMiuix(onBack)
        UiMode.Material -> AboutMaterial(onBack)
    }
}
