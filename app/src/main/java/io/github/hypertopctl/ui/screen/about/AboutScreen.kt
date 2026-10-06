package io.github.hypertopctl.ui.screen.about

import androidx.compose.runtime.Composable
import io.github.hypertopctl.BuildConfig
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

/** Human-facing version label, e.g. "1.0.0（15）". */
internal val appVersionDisplay: String =
    "${BuildConfig.VERSION_BASE}（${BuildConfig.VERSION_CODE}）"

@Composable
fun AboutScreen(onBack: () -> Unit) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> AboutMiuix(onBack)
        UiMode.Material -> AboutMaterial(onBack)
    }
}
