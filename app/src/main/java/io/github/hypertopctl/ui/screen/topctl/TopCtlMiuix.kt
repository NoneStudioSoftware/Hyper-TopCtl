package io.github.hypertopctl.ui.screen.topctl

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
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.ui.MainUiState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TopCtlMiuix(
    state: MainUiState,
    onGlobalEnabledChange: (Boolean) -> Unit,
    onListModeChange: (ListMode) -> Unit,
) {
    Scaffold(
        topBar = { SmallTopAppBar(title = stringResource(R.string.nav_topctl)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(Modifier.fillMaxWidth()) {
                SwitchPreference(
                    checked = state.globalEnabled,
                    onCheckedChange = onGlobalEnabledChange,
                    title = stringResource(R.string.global_enable),
                    summary = stringResource(R.string.global_enable_desc),
                )
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.list_mode))
                    Text(
                        text = when (state.listMode) {
                            ListMode.WHITELIST -> stringResource(R.string.list_mode_whitelist)
                            ListMode.BLACKLIST -> stringResource(R.string.list_mode_blacklist)
                        },
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ListMode.entries.forEach { mode ->
                            TextButton(
                                text = mode.name,
                                onClick = { onListModeChange(mode) },
                                modifier = Modifier.weight(1f),
                                colors = if (state.listMode == mode) {
                                    ButtonDefaults.textButtonColorsPrimary()
                                } else {
                                    ButtonDefaults.textButtonColors()
                                },
                            )
                        }
                    }
                    Text(
                        text = if (state.packages.isEmpty()) {
                            stringResource(R.string.app_list_empty)
                        } else {
                            "${stringResource(R.string.app_list)} (${state.packages.size})"
                        },
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}
