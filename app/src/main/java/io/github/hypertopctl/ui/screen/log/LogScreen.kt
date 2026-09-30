package io.github.hypertopctl.ui.screen.log

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.hypertopctl.data.AppLogger
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

@Composable
fun LogScreen(onBack: () -> Unit) {
    val entries = AppLogger.entries.collectAsStateWithLifecycle().value
    val buildType = AppLogger.buildTypeLabel()
    when (LocalUiMode.current) {
        UiMode.Miuix -> LogMiuix(
            entries = entries,
            buildType = buildType,
            onBack = onBack,
            onClear = AppLogger::clear,
        )
        UiMode.Material -> LogMaterial(
            entries = entries,
            buildType = buildType,
            onBack = onBack,
            onClear = AppLogger::clear,
        )
    }
}
