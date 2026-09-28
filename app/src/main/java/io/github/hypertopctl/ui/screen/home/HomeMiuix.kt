package io.github.hypertopctl.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.hypertopctl.R
import io.github.hypertopctl.ui.MainUiState
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun HomeMiuix(state: MainUiState) {
    Scaffold(
        topBar = { SmallTopAppBar(title = stringResource(R.string.app_name)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Activation status
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(if (state.active) R.string.status_active else R.string.status_inactive),
                        fontSize = MiuixTheme.textStyles.title3.fontSize,
                        color = if (state.active) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = state.frameworkName?.let { "$it · " }.orEmpty() +
                            stringResource(if (state.active) R.string.status_active_desc else R.string.status_inactive_desc),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }

            // Device info
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.home_device_info))
                    InfoRow(stringResource(R.string.home_device_model), state.device.model)
                    InfoRow(stringResource(R.string.home_device_android), "${state.device.androidRelease} (API ${state.device.sdk})")
                    state.device.hyperOsVersion?.let {
                        InfoRow(stringResource(R.string.home_device_hyperos), it)
                    }
                }
            }

            // Hook info
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.home_hook_info))
                    InfoRow(stringResource(R.string.home_hook_scope), "com.android.systemui")
                    InfoRow(stringResource(R.string.home_hook_target), "MiuiInputManager.scrollToTop()")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        Text(value)
    }
}
