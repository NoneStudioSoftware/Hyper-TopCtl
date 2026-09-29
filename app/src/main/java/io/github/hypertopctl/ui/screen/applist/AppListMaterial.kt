package io.github.hypertopctl.ui.screen.applist

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Block
import androidx.compose.material.icons.twotone.Info
import androidx.compose.material.icons.twotone.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.hypertopctl.R
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.component.SearchAppBar
import io.github.hypertopctl.ui.component.rememberSearchAppBarScrollBehavior
import io.github.hypertopctl.ui.component.settings.SegmentedColumn
import io.github.hypertopctl.ui.component.settings.SettingsBaseWidget
import io.github.hypertopctl.ui.util.DrawablePainter

/**
 * Per-app list in Material 3 Expressive style, mirroring ReSukiSU's SuperUser page: the search
 * bar collapses into a title-bar icon while scrolling, and sorting / system-app visibility live
 * in the overflow menu. Toggling an app writes it into the hook's package set; the hint card
 * explains what a checked entry means under the current black/whitelist mode.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppListMaterial(
    state: MainUiState,
    onTogglePackage: (String) -> Unit,
) {
    val context = LocalContext.current
    val topAppBarScrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val scrollBehavior = rememberSearchAppBarScrollBehavior(topAppBarScrollBehavior)

    // When the master switch is off the per-app list has no effect: show a centered
    // unavailable placeholder instead of loading the list.
    if (!state.globalEnabled) {
        return AppListDisabledMaterial()
    }

    var query by rememberSaveable { mutableStateOf("") }
    var showSystem by rememberSaveable { mutableStateOf(false) }
    var sortType by rememberSaveable { mutableStateOf(AppSortType.NAME) }
    var reverseOrder by rememberSaveable { mutableStateOf(false) }
    var showDropdown by remember { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(showSystem) {
        loading = true
        apps = loadAppEntries(context, showSystem)
        loading = false
    }

    val sorted = remember(apps, query, sortType, reverseOrder) {
        val q = query.trim()
        val base = if (q.isEmpty()) {
            apps
        } else {
            apps.filter {
                it.label.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true)
            }
        }
        val comparator = when (sortType) {
            AppSortType.NAME -> compareBy<AppEntry> { it.label.lowercase() }.thenBy { it.packageName }
            AppSortType.INSTALL_TIME -> compareBy { it.firstInstallTime }
            AppSortType.UPDATE_TIME -> compareBy { it.lastUpdateTime }
        }
        val result = base.sortedWith(comparator)
        if (reverseOrder) result.asReversed() else result
    }
    // Checked apps form their own group pinned to the top of the list.
    val checkedEntries = sorted.filter { it.packageName in state.packages }
    val uncheckedEntries = sorted.filterNot { it.packageName in state.packages }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            SearchAppBar(
                title = stringResource(R.string.applist_title),
                searchText = query,
                onSearchTextChange = { query = it },
                searchBarPlaceHolderText = stringResource(R.string.applist_search_hint),
                scrollBehavior = scrollBehavior,
                dropdownContent = {
                    IconButton(onClick = { showDropdown = true }) {
                        Icon(
                            imageVector = Icons.TwoTone.MoreVert,
                            contentDescription = null,
                        )
                    }
                    AppListDropdown(
                        expanded = showDropdown,
                        onDismissRequest = { showDropdown = false },
                        currentSortType = sortType,
                        reverseOrder = reverseOrder,
                        showSystem = showSystem,
                        onSelectSort = { sortType = it },
                        onToggleReverse = { reverseOrder = !reverseOrder },
                        onToggleShowSystem = { showSystem = !showSystem },
                    )
                },
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
                SegmentedColumn {
                    item {
                        SettingsBaseWidget(
                            icon = Icons.TwoTone.Info,
                            title = stringResource(
                                when (state.listMode) {
                                    ListMode.BLACKLIST -> R.string.applist_mode_hint_blacklist
                                    ListMode.WHITELIST -> R.string.applist_mode_hint_whitelist
                                }
                            ),
                            description = stringResource(R.string.applist_selected_count, state.packages.size),
                        )
                    }
                }
            }
            when {
                loading -> item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }

                sorted.isEmpty() -> item {
                    Text(
                        text = stringResource(R.string.applist_empty_result),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                    )
                }

                else -> {
                    if (checkedEntries.isNotEmpty()) {
                        item(key = "checked_header") {
                            GroupTitle(stringResource(R.string.applist_group_checked, checkedEntries.size))
                        }
                        items(checkedEntries, key = { "checked_${it.packageName}" }) { app ->
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                AppRow(
                                    app = app,
                                    checked = true,
                                    onToggle = { onTogglePackage(app.packageName) },
                                )
                            }
                        }
                    }
                    if (uncheckedEntries.isNotEmpty()) {
                        item(key = "unchecked_header") {
                            GroupTitle(stringResource(R.string.applist_group_unchecked, uncheckedEntries.size))
                        }
                        items(uncheckedEntries, key = { "unchecked_${it.packageName}" }) { app ->
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                                AppRow(
                                    app = app,
                                    checked = false,
                                    onToggle = { onTogglePackage(app.packageName) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Centered unavailable placeholder shown when the master switch is off (Material style). */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AppListDisabledMaterial() {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.applist_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(32.dp),
            ) {
                Icon(
                    imageVector = Icons.TwoTone.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(72.dp),
                )
                Text(
                    text = stringResource(R.string.applist_disabled_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(R.string.applist_disabled_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/** Section header mirroring SegmentedColumn's title styling (titleSmall / primary). */
@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
    )
}

@Composable
private fun AppListDropdown(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    currentSortType: AppSortType,
    reverseOrder: Boolean,
    showSystem: Boolean,
    onSelectSort: (AppSortType) -> Unit,
    onToggleReverse: () -> Unit,
    onToggleShowSystem: () -> Unit,
) {
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes(),
        ) {
            AppSortType.entries.forEachIndexed { index, sortType ->
                SelectableDropdownMenuItem(
                    selected = currentSortType == sortType,
                    onClick = { onSelectSort(sortType) },
                    text = { Text(stringResource(sortType.displayNameRes)) },
                    shapes = MenuDefaults.itemShape(
                        index = index,
                        count = AppSortType.entries.size,
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        DropdownMenuGroup(
            shapes = MenuDefaults.groupShapes(),
        ) {
            val toggles = listOf(
                Triple(reverseOrder, R.string.applist_reverse_order, onToggleReverse),
                Triple(showSystem, R.string.applist_show_system, onToggleShowSystem),
            )
            toggles.forEachIndexed { index, (checked, titleRes, onClick) ->
                SelectableDropdownMenuItem(
                    selected = checked,
                    onClick = onClick,
                    text = { Text(stringResource(titleRes)) },
                    shapes = MenuDefaults.itemShape(
                        index = index,
                        count = toggles.size,
                    ),
                )
            }
        }
    }
}

@Composable
private fun AppRow(
    app: AppEntry,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    SettingsBaseWidget(
        title = app.label,
        description = if (app.isSystem) {
            app.packageName + " · " + stringResource(R.string.applist_system_badge)
        } else {
            app.packageName
        },
        onClick = { onToggle() },
        leadingContent = {
            val icon = app.icon
            if (icon != null) {
                Image(
                    painter = remember(app.packageName) { DrawablePainter(icon) },
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                )
            } else {
                Spacer(Modifier.size(28.dp))
            }
        },
        trailingContent = {
            Checkbox(checked = checked, onCheckedChange = null)
        },
    )
}
