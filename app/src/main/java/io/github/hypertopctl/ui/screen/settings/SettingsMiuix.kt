package io.github.hypertopctl.ui.screen.settings

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.hypertopctl.R
import io.github.hypertopctl.ui.MainUiState
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun SettingsMiuix(
    @Suppress("UNUSED_PARAMETER") state: MainUiState,
    onOpenTheme: () -> Unit,
    onOpenAbout: () -> Unit,
    onOpenLog: () -> Unit,
) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            TopAppBar(
                color = colorScheme.surface,
                title = stringResource(R.string.nav_settings),
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(horizontal = 12.dp),
            contentPadding = innerPadding,
            overscrollEffect = null,
        ) {
            item {
                Text(
                    text = stringResource(R.string.settings_category_appearance),
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, bottom = 6.dp),
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = stringResource(R.string.settings_theme_entry),
                        summary = stringResource(R.string.settings_theme_entry_desc),
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Theme,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = stringResource(R.string.settings_theme_entry),
                                tint = colorScheme.onBackground,
                            )
                        },
                        onClick = onOpenTheme,
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.settings_category_diagnostics),
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, bottom = 6.dp),
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = stringResource(R.string.settings_log_entry),
                        summary = stringResource(R.string.settings_log_entry_desc),
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Info,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = stringResource(R.string.settings_log_entry),
                                tint = colorScheme.onBackground,
                            )
                        },
                        onClick = onOpenLog,
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.settings_category_about),
                    color = colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp, start = 12.dp, bottom = 6.dp),
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ArrowPreference(
                        title = stringResource(R.string.about),
                        summary = stringResource(R.string.about_entry_desc),
                        startAction = {
                            Icon(
                                imageVector = MiuixIcons.Info,
                                modifier = Modifier.padding(end = 6.dp),
                                contentDescription = stringResource(R.string.about),
                                tint = colorScheme.onBackground,
                            )
                        },
                        onClick = onOpenAbout,
                    )
                }
            }
        }
    }
}
