package io.github.hypertopctl.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.hypertopctl.BuildConfig
import io.github.hypertopctl.R
import io.github.hypertopctl.lsp.UiFrameworkValue
import io.github.hypertopctl.ui.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsMaterial(
    state: MainUiState,
    onUiFrameworkChange: (String) -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryTitle(stringResource(R.string.settings_category_appearance))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.ui_framework))
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

            CategoryTitle(stringResource(R.string.settings_category_about))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.settings_version))
                        Text(
                            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        stringResource(R.string.app_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.about_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 8.dp),
    )
}
