package io.github.hypertopctl.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.hypertopctl.R
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.lsp.UiFrameworkValue
import io.github.hypertopctl.ui.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialMainScreen(
    state: MainUiState,
    onGlobalEnabledChange: (Boolean) -> Unit,
    onListModeChange: (ListMode) -> Unit,
    onUiFrameworkChange: (String) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Status card
            Card(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        if (state.active) Icons.Filled.CheckCircle else Icons.Filled.Info,
                        contentDescription = null,
                    )
                    Column {
                        Text(
                            stringResource(if (state.active) R.string.status_active else R.string.status_inactive),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            stringResource(if (state.active) R.string.status_active_desc else R.string.status_inactive_desc),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Settings card
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleMedium)

                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.global_enable))
                            Text(
                                stringResource(R.string.global_enable_desc),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        Switch(checked = state.globalEnabled, onCheckedChange = onGlobalEnabledChange)
                    }

                    HorizontalDivider()

                    Text(stringResource(R.string.list_mode))
                    Text(
                        when (state.listMode) {
                            ListMode.WHITELIST -> stringResource(R.string.list_mode_whitelist)
                            ListMode.BLACKLIST -> stringResource(R.string.list_mode_blacklist)
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ListMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = state.listMode == mode,
                                onClick = { onListModeChange(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index, ListMode.entries.size),
                            ) { Text(mode.name) }
                        }
                    }

                    if (state.packages.isEmpty()) {
                        Text(
                            stringResource(R.string.app_list_empty),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        Text("${stringResource(R.string.app_list)} (${state.packages.size})")
                    }
                }
            }

            // UI framework switch
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.ui_framework), style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = state.uiFramework == UiFrameworkValue.MIUIX,
                            onClick = { onUiFrameworkChange(UiFrameworkValue.MIUIX) },
                            label = { Text(stringResource(R.string.ui_framework_miuix)) },
                        )
                        FilterChip(
                            selected = state.uiFramework == UiFrameworkValue.MATERIAL,
                            onClick = { onUiFrameworkChange(UiFrameworkValue.MATERIAL) },
                            label = { Text(stringResource(R.string.ui_framework_material)) },
                        )
                    }
                }
            }

            // About
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.about), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.app_description), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.about_desc), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
