package io.github.hypertopctl.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.hypertopctl.ui.screen.MainScreen
import io.github.hypertopctl.ui.theme.HyperTopCtlTheme
import io.github.hypertopctl.ui.theme.LocalUiMode
import io.github.hypertopctl.ui.theme.UiMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: MainViewModel = viewModel()
            val state by vm.state.collectAsStateWithLifecycle()
            val uiMode = UiMode.fromValue(state.uiFramework)

            CompositionLocalProvider(LocalUiMode provides uiMode) {
                HyperTopCtlTheme(uiMode = uiMode) {
                    MainScreen(
                        uiMode = uiMode,
                        state = state,
                        onGlobalEnabledChange = vm::setGlobalEnabled,
                        onListModeChange = vm::setListMode,
                        onUiFrameworkChange = vm::setUiFramework,
                    )
                }
            }
        }
    }
}
