package io.github.hypertopctl.ui.screen.topctl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.FilterList
import androidx.compose.material.icons.twotone.PowerSettingsNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.hypertopctl.R
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.component.settings.SegmentedColumn
import io.github.hypertopctl.ui.component.settings.SettingsDropdownWidget
import io.github.hypertopctl.ui.component.settings.SettingsSwitchWidget

/**
 * TopCtl control screen in Material 3 Expressive style, strictly aligned with ReSukiSU Manager's SettingsPage.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TopCtlMaterial(
    state: MainUiState,
    onGlobalEnabledChange: (Boolean) -> Unit,
    onListModeChange: (ListMode) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.nav_topctl)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                windowInsets = TopAppBarDefaults.windowInsets.add(WindowInsets(left = 12.dp)),
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
            }
            item {
                SegmentedColumn(
                    title = stringResource(R.string.nav_topctl),
                ) {
                    // List-mode selection collapses with ReSukiSU's expandable animation
                    // whenever the master switch is off.
                    expandableItem(
                        expanded = state.globalEnabled,
                        topContent = {
                            SettingsSwitchWidget(
                                icon = Icons.TwoTone.PowerSettingsNew,
                                title = stringResource(R.string.global_enable),
                                description = stringResource(R.string.global_enable_desc),
                                checked = state.globalEnabled,
                                onCheckedChange = onGlobalEnabledChange,
                            )
                        },
                    ) {
                        item {
                            SettingsDropdownWidget(
                                icon = Icons.TwoTone.FilterList,
                                title = stringResource(R.string.list_mode),
                                description = when (state.listMode) {
                                    ListMode.WHITELIST -> stringResource(R.string.list_mode_whitelist)
                                    ListMode.BLACKLIST -> stringResource(R.string.list_mode_blacklist)
                                },
                                data = listOf(
                                    stringResource(R.string.list_mode_whitelist),
                                    stringResource(R.string.list_mode_blacklist),
                                ),
                                choice = if (state.listMode == ListMode.WHITELIST) 0 else 1,
                                onChoiceChange = { index ->
                                    onListModeChange(if (index == 0) ListMode.WHITELIST else ListMode.BLACKLIST)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
