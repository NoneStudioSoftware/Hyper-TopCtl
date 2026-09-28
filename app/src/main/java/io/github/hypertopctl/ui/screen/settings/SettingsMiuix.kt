package io.github.hypertopctl.ui.screen.settings

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
import io.github.hypertopctl.BuildConfig
import io.github.hypertopctl.R
import io.github.hypertopctl.lsp.UiFrameworkValue
import io.github.hypertopctl.ui.MainUiState
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingsMiuix(
    state: MainUiState,
    onUiFrameworkChange: (String) -> Unit,
) {
    Scaffold(
        topBar = { SmallTopAppBar(title = stringResource(R.string.nav_settings)) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SmallTitle(stringResource(R.string.settings_category_appearance))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.ui_framework))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            text = stringResource(R.string.ui_framework_miuix),
                            onClick = { onUiFrameworkChange(UiFrameworkValue.MIUIX) },
                            modifier = Modifier.weight(1f),
                            colors = if (state.uiFramework == UiFrameworkValue.MIUIX) {
                                ButtonDefaults.textButtonColorsPrimary()
                            } else {
                                ButtonDefaults.textButtonColors()
                            },
                        )
                        TextButton(
                            text = stringResource(R.string.ui_framework_material),
                            onClick = { onUiFrameworkChange(UiFrameworkValue.MATERIAL) },
                            modifier = Modifier.weight(1f),
                            colors = if (state.uiFramework == UiFrameworkValue.MATERIAL) {
                                ButtonDefaults.textButtonColorsPrimary()
                            } else {
                                ButtonDefaults.textButtonColors()
                            },
                        )
                    }
                }
            }

            SmallTitle(stringResource(R.string.settings_category_about))
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.settings_version))
                        Text(
                            "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                    Text(
                        stringResource(R.string.app_description),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    Text(
                        stringResource(R.string.about_desc),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            }
        }
    }
}
