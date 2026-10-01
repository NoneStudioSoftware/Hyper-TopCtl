package io.github.hypertopctl.ui.screen.applist

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.hypertopctl.R
import io.github.hypertopctl.lsp.ListMode
import io.github.hypertopctl.ui.MainUiState
import io.github.hypertopctl.ui.component.miuix.SearchBarFake
import io.github.hypertopctl.ui.component.miuix.SearchBox
import io.github.hypertopctl.ui.component.miuix.SearchPager
import io.github.hypertopctl.ui.component.miuix.SearchStatus
import io.github.hypertopctl.ui.util.DrawablePainter
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownImpl
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.basic.Close
import top.yukonga.miuix.kmp.icon.extended.MoreCircle
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.overlay.OverlayListPopup
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.colorScheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

/**
 * Per-app list in Miuix style, following KernelSU Manager's SuperUser page: a collapsing
 * top bar with a fake search box that expands into a full-screen search overlay, sort and
 * more popups in the top-right, and app rows grouped by checked state. Design reference:
 * this project's MD3 implementation and KSU.
 */
@Composable
fun AppListMiuix(
    state: MainUiState,
    onTogglePackage: (String) -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    // When the master switch is off the per-app list has no effect: show a centered
    // unavailable placeholder instead of loading the list.
    if (!state.globalEnabled) {
        return AppListDisabledMiuix()
    }

    var showSystem by rememberSaveable { mutableStateOf(false) }
    var sortType by rememberSaveable { mutableStateOf(AppSortType.NAME) }
    var reverseOrder by rememberSaveable { mutableStateOf(false) }
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(showSystem) {
        loading = true
        apps = loadAppEntries(context, showSystem)
        loading = false
    }

    val scrollBehavior = MiuixScrollBehavior()
    val dynamicTopPadding by remember {
        derivedStateOf { 12.dp * (1f - scrollBehavior.state.collapsedFraction) }
    }

    var searchStatus by remember {
        mutableStateOf(SearchStatus(label = context.getString(R.string.applist_search_hint)))
    }
    // The search overlay writes into searchStatus.searchText; filtering reads it directly so
    // the two can never drift apart. resultStatus follows the text: KSU's pager only renders
    // the results block in SHOW state.
    fun updateSearchStatus(next: SearchStatus) {
        searchStatus = next.copy(
            resultStatus = if (next.searchText.isEmpty()) {
                SearchStatus.ResultStatus.DEFAULT
            } else {
                SearchStatus.ResultStatus.SHOW
            }
        )
    }
    val query = searchStatus.searchText

    val sorted = remember(apps, sortType, reverseOrder) {
        val comparator = when (sortType) {
            AppSortType.NAME -> compareBy<AppEntry> { it.label.lowercase() }.thenBy { it.packageName }
            AppSortType.INSTALL_TIME -> compareBy { it.firstInstallTime }
            AppSortType.UPDATE_TIME -> compareBy { it.lastUpdateTime }
        }
        val result = apps.sortedWith(comparator)
        if (reverseOrder) result.asReversed() else result
    }
    // Checked apps form their own group pinned to the top of the list.
    val checkedEntries = sorted.filter { it.packageName in state.packages }
    val uncheckedEntries = sorted.filterNot { it.packageName in state.packages }
    val filteredChecked = remember(checkedEntries, query) { filterEntries(checkedEntries, query) }
    val filteredUnchecked = remember(uncheckedEntries, query) { filterEntries(uncheckedEntries, query) }

    Scaffold(
        topBar = {
            searchStatus.TopAppBarAnim {
                TopAppBar(
                    color = colorScheme.surface,
                    title = stringResource(R.string.applist_title),
                    actions = {
                        Box {
                            val showSortPopup = remember { mutableStateOf(false) }
                            OverlayListPopup(
                                show = showSortPopup.value,
                                popupPositionProvider = ListPopupDefaults.DropdownPositionProvider,
                                alignment = PopupPositionProvider.Align.TopEnd,
                                onDismissRequest = { showSortPopup.value = false },
                                content = {
                                    ListPopupColumn {
                                        val sortEntries = listOf(
                                            AppSortType.NAME to R.string.applist_sort_name,
                                            AppSortType.INSTALL_TIME to R.string.applist_sort_install_time,
                                            AppSortType.UPDATE_TIME to R.string.applist_sort_update_time,
                                        )
                                        val sortGroupSize = sortEntries.size + 1

                                        sortEntries.forEachIndexed { index, (type, resId) ->
                                            DropdownImpl(
                                                text = stringResource(resId),
                                                optionSize = sortGroupSize,
                                                isSelected = sortType == type,
                                                index = index,
                                                onSelectedIndexChange = {
                                                    sortType = type
                                                    showSortPopup.value = false
                                                }
                                            )
                                        }

                                        HorizontalDivider(
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                                            thickness = 1.5.dp,
                                        )

                                        DropdownImpl(
                                            text = stringResource(R.string.applist_reverse_order),
                                            optionSize = sortGroupSize,
                                            isSelected = reverseOrder,
                                            index = sortEntries.size,
                                            onSelectedIndexChange = {
                                                reverseOrder = !reverseOrder
                                                showSortPopup.value = false
                                            }
                                        )
                                    }
                                }
                            )

                            IconButton(
                                onClick = { showSortPopup.value = true },
                                holdDownState = showSortPopup.value,
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.Sort,
                                    tint = colorScheme.onSurface,
                                    contentDescription = stringResource(R.string.applist_sort_name)
                                )
                            }
                        }

                        Box {
                            val showMorePopup = remember { mutableStateOf(false) }
                            OverlayListPopup(
                                show = showMorePopup.value,
                                popupPositionProvider = ListPopupDefaults.DropdownPositionProvider,
                                alignment = PopupPositionProvider.Align.TopEnd,
                                onDismissRequest = {
                                    showMorePopup.value = false
                                },
                                content = {
                                    ListPopupColumn {
                                        DropdownImpl(
                                            text = stringResource(R.string.applist_show_system),
                                            isSelected = showSystem,
                                            optionSize = 1,
                                            onSelectedIndexChange = {
                                                showSystem = !showSystem
                                                showMorePopup.value = false
                                            },
                                            index = 0
                                        )
                                    }
                                }
                            )
                            IconButton(
                                onClick = {
                                    showMorePopup.value = true
                                },
                                holdDownState = showMorePopup.value
                            ) {
                                Icon(
                                    imageVector = MiuixIcons.MoreCircle,
                                    tint = colorScheme.onSurface,
                                    contentDescription = null
                                )
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    bottomContent = {
                        Box(
                            modifier = Modifier
                                .alpha(if (searchStatus.isCollapsed()) 1f else 0f)
                                .onGloballyPositioned { coordinates ->
                                    with(density) {
                                        val newOffsetY = coordinates.positionInWindow().y.toDp()
                                        if (searchStatus.offsetY != newOffsetY) {
                                            updateSearchStatus(searchStatus.copy(offsetY = newOffsetY))
                                        }
                                    }
                                }
                                .then(
                                    if (searchStatus.isCollapsed()) {
                                        Modifier.pointerInput(Unit) {
                                            detectTapGestures {
                                                updateSearchStatus(searchStatus.copy(current = SearchStatus.Status.EXPANDING))
                                            }
                                        }
                                    } else Modifier,
                                ),
                        ) {
                            SearchBarFake(searchStatus.label, dynamicTopPadding)
                        }
                    }
                )
            }
        },
        popupHost = {
            searchStatus.SearchPager(
                onSearchStatusChange = { updateSearchStatus(it) },
                searchBarTopPadding = dynamicTopPadding,
                defaultResult = {},
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(
                        top = 6.dp,
                        start = 12.dp,
                        end = 12.dp,
                    ),
                ) {
                    if (filteredChecked.isNotEmpty()) {
                        item(key = "checked_header") {
                            GroupTitle(stringResource(R.string.applist_group_checked, filteredChecked.size))
                        }
                        items(filteredChecked, key = { "checked_${it.packageName}" }) { app ->
                            AppCard(
                                app = app,
                                checked = true,
                                onToggle = { onTogglePackage(app.packageName) },
                            )
                        }
                    }
                    if (filteredUnchecked.isNotEmpty()) {
                        item(key = "unchecked_header") {
                            GroupTitle(stringResource(R.string.applist_group_unchecked, filteredUnchecked.size))
                        }
                        items(filteredUnchecked, key = { "unchecked_${it.packageName}" }) { app ->
                            AppCard(
                                app = app,
                                checked = false,
                                onToggle = { onTogglePackage(app.packageName) },
                            )
                        }
                    }
                    if (filteredChecked.isEmpty() && filteredUnchecked.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(R.string.applist_empty_result),
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        searchStatus.SearchBox {
            val lazyListState = rememberLazyListState()
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxHeight()
                    .scrollEndHaptic()
                    .overScrollVertical()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding() + 6.dp,
                    start = 12.dp,
                    end = 12.dp,
                ),
                overscrollEffect = null,
            ) {
                if (loading) {
                    item(key = "loading") {
                        Text(
                            text = stringResource(R.string.applist_loading),
                            fontSize = 14.sp,
                            color = colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp),
                        )
                    }
                } else {
                    item(key = "mode_hint") {
                        Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = stringResource(
                                        when (state.listMode) {
                                            ListMode.BLACKLIST -> R.string.applist_mode_hint_blacklist
                                            ListMode.WHITELIST -> R.string.applist_mode_hint_whitelist
                                        }
                                    ),
                                    fontSize = 14.sp,
                                    color = colorScheme.onSurface,
                                )
                                Text(
                                    text = stringResource(R.string.applist_selected_count, state.packages.size),
                                    fontSize = 13.sp,
                                    color = colorScheme.onSurfaceVariantSummary,
                                )
                            }
                        }
                    }
                    if (filteredChecked.isNotEmpty()) {
                        item(key = "checked_header") {
                            GroupTitle(stringResource(R.string.applist_group_checked, filteredChecked.size))
                        }
                        items(filteredChecked, key = { "checked_${it.packageName}" }) { app ->
                            AppCard(
                                app = app,
                                checked = true,
                                onToggle = { onTogglePackage(app.packageName) },
                            )
                        }
                    }
                    if (filteredUnchecked.isNotEmpty()) {
                        item(key = "unchecked_header") {
                            GroupTitle(stringResource(R.string.applist_group_unchecked, filteredUnchecked.size))
                        }
                        items(filteredUnchecked, key = { "unchecked_${it.packageName}" }) { app ->
                            AppCard(
                                app = app,
                                checked = false,
                                onToggle = { onTogglePackage(app.packageName) },
                            )
                        }
                    }
                    if (filteredChecked.isEmpty() && filteredUnchecked.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = stringResource(R.string.applist_empty_result),
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 24.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun filterEntries(entries: List<AppEntry>, query: String): List<AppEntry> {
    val q = query.trim()
    if (q.isEmpty()) return entries
    return entries.filter {
        it.label.contains(q, ignoreCase = true) || it.packageName.contains(q, ignoreCase = true)
    }
}

/** Centered unavailable placeholder shown when the master switch is off (Miuix style). */
@Composable
private fun AppListDisabledMiuix() {
    Scaffold(
        topBar = {
            TopAppBar(
                color = colorScheme.surface,
                title = stringResource(R.string.applist_title),
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
                    imageVector = MiuixIcons.Basic.Close,
                    contentDescription = null,
                    tint = colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(72.dp),
                )
                Text(
                    text = stringResource(R.string.applist_disabled_title),
                    fontSize = MiuixTheme.textStyles.title2.fontSize,
                    color = colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.applist_disabled_desc),
                    fontSize = 14.sp,
                    color = colorScheme.onSurfaceVariantSummary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
    }
}

/** Section header styled like the "界面风格" label: plain onBackground text with 12dp inset. */
@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text,
        color = colorScheme.onBackground,
        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 6.dp),
    )
}

@Composable
private fun AppCard(
    app: AppEntry,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        showIndication = true,
        insideMargin = PaddingValues(0.dp)
    ) {
        BasicComponent(
            title = app.label,
            summary = if (app.isSystem) {
                app.packageName + " · " + stringResource(R.string.applist_system_badge)
            } else {
                app.packageName
            },
            startAction = {
                val icon = app.icon
                if (icon != null) {
                    Image(
                        painter = remember(app.packageName) { DrawablePainter(icon) },
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .size(40.dp),
                    )
                } else {
                    Spacer(
                        Modifier
                            .padding(end = 2.dp)
                            .size(40.dp)
                    )
                }
            },
            endActions = {
                Checkbox(
                    state = if (checked) ToggleableState.On else ToggleableState.Off,
                    onClick = null,
                )
            },
            onClick = onToggle,
            insideMargin = PaddingValues(start = 9.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        )
    }
}
