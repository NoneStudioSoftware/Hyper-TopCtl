package io.github.hypertopctl.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.hypertopctl.R
import io.github.hypertopctl.ui.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeMaterial(state: MainUiState) {
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
            // Activation status
            Card(
                Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (state.active) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    },
                ),
            ) {
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
                            state.frameworkName?.let { "$it · " }.orEmpty() +
                                stringResource(if (state.active) R.string.status_active_desc else R.string.status_inactive_desc),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }

            // Device info
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.home_device_info), style = MaterialTheme.typography.titleMedium)
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
                    Text(stringResource(R.string.home_hook_info), style = MaterialTheme.typography.titleMedium)
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
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
