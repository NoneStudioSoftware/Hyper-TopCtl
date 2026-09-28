package io.github.hypertopctl.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Android
import androidx.compose.material.icons.twotone.DeveloperBoard
import androidx.compose.material.icons.twotone.Security
import androidx.compose.material.icons.twotone.Smartphone
import androidx.compose.material.icons.twotone.Tag
import androidx.compose.material.icons.twotone.TaskAlt
import androidx.compose.material.icons.twotone.Tune
import androidx.compose.material.icons.twotone.Warning
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
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.component.settings.SegmentedColumn
import io.github.hypertopctl.ui.component.settings.SettingsBaseWidget

/**
 * Home screen in Material 3 Expressive style, strictly aligned with ReSukiSU Manager's HomePage.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeMaterial(state: MainUiState) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                },
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
        ) {
            item {
                Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))
            }

            // Status Card (ReSukiSU style SettingsBaseWidget)
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    SettingsBaseWidget(
                        icon = if (state.active) Icons.TwoTone.TaskAlt else Icons.TwoTone.Warning,
                        iconPlaceholder = true,
                        title = stringResource(if (state.active) R.string.status_active else R.string.status_inactive),
                        description = state.frameworkName?.let { "$it · " }.orEmpty() +
                            stringResource(if (state.active) R.string.status_active_desc else R.string.status_inactive_desc),
                        containerColor = if (state.active) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        },
                        isError = !state.active,
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Device Info (ReSukiSU InfoCard style)
            item {
                SegmentedColumn(
                    title = stringResource(R.string.home_device_info),
                ) {
                    item {
                        SettingsBaseWidget(
                            icon = Icons.TwoTone.Smartphone,
                            iconPlaceholder = false,
                            title = stringResource(R.string.home_device_model),
                            description = state.device.model,
                        )
                    }

                    item {
                        SettingsBaseWidget(
                            icon = Icons.TwoTone.Android,
                            iconPlaceholder = false,
                            title = stringResource(R.string.home_device_android),
                            description = state.device.androidRelease,
                        )
                    }

                    if (state.device.hyperOsVersion != null) {
                        item {
                            SettingsBaseWidget(
                                icon = Icons.TwoTone.Tag,
                                iconPlaceholder = false,
                                title = stringResource(R.string.home_device_hyperos),
                                description = state.device.hyperOsVersion,
                            )
                        }
                    }

                    item {
                        SettingsBaseWidget(
                            icon = Icons.TwoTone.DeveloperBoard,
                            iconPlaceholder = false,
                            title = stringResource(R.string.home_device_sdk),
                            description = state.device.sdk.toString(),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Hook Info (ReSukiSU style)
            item {
                SegmentedColumn(
                    title = stringResource(R.string.home_hook_info),
                ) {
                    item {
                        SettingsBaseWidget(
                            icon = Icons.TwoTone.Security,
                            iconPlaceholder = false,
                            title = stringResource(R.string.home_hook_scope),
                            description = "com.android.systemui",
                        )
                    }

                    item {
                        SettingsBaseWidget(
                            icon = Icons.TwoTone.Tune,
                            iconPlaceholder = false,
                            title = stringResource(R.string.home_hook_target),
                            description = "MiuiInputManager.scrollToTop()",
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
